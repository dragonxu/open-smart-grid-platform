/**
 * Copyright 2019 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.protocol.iec60870.infra.messaging;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.opensmartgridplatform.adapter.protocol.iec60870.infra.networking.helper.DeviceConnection;
import org.opensmartgridplatform.adapter.protocol.iec60870.infra.networking.helper.RequestMessageData;
import org.opensmartgridplatform.adapter.protocol.iec60870.infra.networking.services.Iec60870DeviceService;
import org.opensmartgridplatform.shared.exceptionhandling.ProtocolAdapterException;
import org.opensmartgridplatform.shared.infra.jms.DeviceMessageMetadata;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;
import org.opensmartgridplatform.shared.infra.jms.MessageProcessor;
import org.opensmartgridplatform.shared.infra.jms.MessageProcessorMap;
import org.opensmartgridplatform.shared.infra.jms.MessageType;
import org.opensmartgridplatform.shared.infra.jms.ProtocolResponseMessage;
import org.opensmartgridplatform.shared.infra.jms.ResponseMessageResultType;
import org.opensmartgridplatform.shared.infra.jms.ResponseMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.support.JmsUtils;

/**
 * Base class for MessageProcessor implementations. Each MessageProcessor
 * implementation should be annotated with @Component. Further the MessageType
 * the MessageProcessor implementation can process should be passed in at
 * construction. The Singleton instance is added to the HashMap of
 * MessageProcessors after dependency injection has completed.
 */
public abstract class BaseMessageProcessor implements MessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMessageProcessor.class);

    @Autowired
    private int maxRedeliveriesForIec60870Requests;

    @Autowired
    private ResponseMessageSender responseMessageSender;

    @Autowired
    @Qualifier("iec60870RequestMessageProcessorMap")
    private MessageProcessorMap iec60870RequestMessageProcessorMap;

    private MessageType messageType;
    private Class<? extends BaseResponseEventListener> responseEventListener;

    @Autowired
    private Iec60870DeviceService iec60870DeviceService;

    @Autowired
    private LogItemRequestMessageSender iec60870LogItemRequestMessageSender;

    /**
     * Each MessageProcessor should register its MessageType at construction.
     *
     * @param messageType
     *            The MessageType the MessageProcessor implementation can
     *            process.
     */
    protected BaseMessageProcessor(final MessageType messageType,
            final Class<? extends BaseResponseEventListener> responseEventListener) {
        this.messageType = messageType;
        this.responseEventListener = responseEventListener;
    }

    protected ResponseMessageSender getResponseMessageSender() {
        return this.responseMessageSender;
    }

    protected Iec60870DeviceService getIec60870DeviceService() {
        return this.iec60870DeviceService;
    }

    protected LogItemRequestMessageSender getLogItemRequestMessageSender() {
        return this.iec60870LogItemRequestMessageSender;
    }

    /**
     * Initialization function executed after dependency injection has finished.
     * The MessageProcessor Singleton is added to the Map of MessageProcessors.
     */
    @PostConstruct
    public void init() {
        this.iec60870RequestMessageProcessorMap.addMessageProcessor(this.messageType, this);
    }

    @Override
    public void processMessage(final ObjectMessage message) throws JMSException {
        LOGGER.info("Processing get health status request message in new code...");

        MessageMetadata messageMetadata = null;
        try {
            messageMetadata = MessageMetadata.fromMessage(message);

            final BaseResponseEventListener eventListener = this.createEventListener(messageMetadata);
            final DeviceConnection deviceConnection = this.iec60870DeviceService.connectToDevice(
                    messageMetadata.getDeviceIdentification(), messageMetadata.getIpAddress(), eventListener);

            this.process(messageMetadata, deviceConnection);
        } catch (final ProtocolAdapterException e) {
            this.handleError(messageMetadata, e);
        } catch (final Exception e) {
            LOGGER.error("UNRECOVERABLE ERROR, unable to read ObjectMessage instance, giving up.", e);
            return;
        }
    }

    /**
     * Create a new instance of the event listener for this message processor.
     *
     * @param messageMetadata
     *            MessageMetaData to pass to the new event listener.
     * @return The created event listener.
     */
    private BaseResponseEventListener createEventListener(final MessageMetadata messageMetadata) {

        Constructor<? extends BaseResponseEventListener> constructor;
        try {
            constructor = this.responseEventListener.getConstructor(MessageMetadata.class, ResponseMessageSender.class,
                    LogItemRequestMessageSender.class);

            return constructor.newInstance(messageMetadata, this.responseMessageSender,
                    this.iec60870LogItemRequestMessageSender);

        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {

            throw new IllegalStateException("Failed to create an instance for " + this.responseEventListener.getName(),
                    e);
        }

    }

    public abstract void process(final MessageMetadata messageMetadata, final DeviceConnection deviceConnection)
            throws ProtocolAdapterException;

    protected void printDomainInfo(final RequestMessageData requestMessageData) {
        LOGGER.info("Calling DeviceService function: {} for domain: {} {}", requestMessageData.getMessageType(),
                requestMessageData.getDomain(), requestMessageData.getDomainVersion());
    }

    protected void handleError(final MessageMetadata messageMetadata, final ProtocolAdapterException e)
            throws JMSException {
        LOGGER.warn("Error while processing message", e);

        final DeviceMessageMetadata deviceMessageMetadata = new DeviceMessageMetadata(messageMetadata);

        final ProtocolResponseMessage protocolResponseMessage = new ProtocolResponseMessage.Builder()
                .domain(messageMetadata.getDomain()).domainVersion(messageMetadata.getDomainVersion())
                .deviceMessageMetadata(deviceMessageMetadata).result(ResponseMessageResultType.NOT_OK).osgpException(e)
                .retryCount(messageMetadata.getRetryCount()).build();

        if (this.hasRemainingRedeliveries(messageMetadata)) {
            this.redeliverMessage(messageMetadata, e);
        } else {
            this.sendErrorResponse(messageMetadata, protocolResponseMessage);
        }

    }

    private boolean hasRemainingRedeliveries(final MessageMetadata messageMetadata) {
        final int jmsxRedeliveryCount = messageMetadata.getJmsxDeliveryCount() - 1;
        LOGGER.info("jmsxDeliveryCount: {}, jmsxRedeliveryCount: {}, maxRedeliveriesForIec60870Requests: {}",
                messageMetadata.getJmsxDeliveryCount(), jmsxRedeliveryCount, this.maxRedeliveriesForIec60870Requests);

        return jmsxRedeliveryCount < this.maxRedeliveriesForIec60870Requests;
    }

    private void redeliverMessage(final MessageMetadata messageMetadata, final ProtocolAdapterException e) {
        final int jmsxRedeliveryCount = messageMetadata.getJmsxDeliveryCount() - 1;

        LOGGER.info(
                "Redelivering message with messageType: {}, correlationUid: {}, for device: {} - jmsxRedeliveryCount: {} is less than maxRedeliveriesForIec60870Requests: {}",
                messageMetadata.getMessageType(), messageMetadata.getCorrelationUid(),
                messageMetadata.getDeviceIdentification(), jmsxRedeliveryCount,
                this.maxRedeliveriesForIec60870Requests);
        final JMSException jmsException = new JMSException(
                e == null ? "redeliverMessage() unknown error: ProtocolAdapterException e is null" : e.getMessage());
        throw JmsUtils.convertJmsAccessException(jmsException);

    }

    private void sendErrorResponse(final MessageMetadata messageMetadata,
            final ProtocolResponseMessage protocolResponseMessage) {
        LOGGER.warn(
                "All redelivery attempts failed for message with messageType: {}, correlationUid: {}, for device: {}",
                messageMetadata.getMessageType(), messageMetadata.getCorrelationUid(),
                messageMetadata.getDeviceIdentification());

        this.responseMessageSender.send(protocolResponseMessage);
    }

}
