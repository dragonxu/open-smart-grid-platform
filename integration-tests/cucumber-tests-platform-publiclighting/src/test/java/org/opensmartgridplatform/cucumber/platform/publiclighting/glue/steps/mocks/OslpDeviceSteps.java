/**
 * Copyright 2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.cucumber.platform.publiclighting.glue.steps.mocks;

import static org.opensmartgridplatform.cucumber.core.ReadSettingsHelper.getBoolean;
import static org.opensmartgridplatform.cucumber.core.ReadSettingsHelper.getDate;
import static org.opensmartgridplatform.cucumber.core.ReadSettingsHelper.getEnum;
import static org.opensmartgridplatform.cucumber.core.ReadSettingsHelper.getInteger;
import static org.opensmartgridplatform.cucumber.core.ReadSettingsHelper.getString;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.opensmartgridplatform.adapter.protocol.oslp.elster.domain.entities.OslpDevice;
import org.opensmartgridplatform.adapter.protocol.oslp.elster.domain.repositories.OslpDeviceRepository;
import org.opensmartgridplatform.cucumber.core.ScenarioContext;
import org.opensmartgridplatform.cucumber.platform.PlatformDefaults;
import org.opensmartgridplatform.cucumber.platform.PlatformKeys;
import org.opensmartgridplatform.cucumber.platform.config.CoreDeviceConfiguration;
import org.opensmartgridplatform.cucumber.platform.publiclighting.PlatformPubliclightingDefaults;
import org.opensmartgridplatform.cucumber.platform.publiclighting.PlatformPubliclightingKeys;
import org.opensmartgridplatform.cucumber.platform.publiclighting.mocks.oslpdevice.DeviceSimulatorException;
import org.opensmartgridplatform.cucumber.platform.publiclighting.mocks.oslpdevice.MockOslpServer;
import org.opensmartgridplatform.domain.core.valueobjects.EventNotificationType;
import org.opensmartgridplatform.domain.core.valueobjects.RelayMap;
import org.opensmartgridplatform.oslp.Oslp;
import org.opensmartgridplatform.oslp.Oslp.ActionTime;
import org.opensmartgridplatform.oslp.Oslp.DaliConfiguration;
import org.opensmartgridplatform.oslp.Oslp.DeviceType;
import org.opensmartgridplatform.oslp.Oslp.Event;
import org.opensmartgridplatform.oslp.Oslp.EventNotification;
import org.opensmartgridplatform.oslp.Oslp.EventNotificationRequest;
import org.opensmartgridplatform.oslp.Oslp.EventNotificationResponse;
import org.opensmartgridplatform.oslp.Oslp.GetPowerUsageHistoryRequest;
import org.opensmartgridplatform.oslp.Oslp.HistoryTermType;
import org.opensmartgridplatform.oslp.Oslp.IndexAddressMap;
import org.opensmartgridplatform.oslp.Oslp.LightType;
import org.opensmartgridplatform.oslp.Oslp.LightValue;
import org.opensmartgridplatform.oslp.Oslp.LinkType;
import org.opensmartgridplatform.oslp.Oslp.LongTermIntervalType;
import org.opensmartgridplatform.oslp.Oslp.Message;
import org.opensmartgridplatform.oslp.Oslp.MeterType;
import org.opensmartgridplatform.oslp.Oslp.RegisterDeviceResponse;
import org.opensmartgridplatform.oslp.Oslp.RelayConfiguration;
import org.opensmartgridplatform.oslp.Oslp.ResumeScheduleRequest;
import org.opensmartgridplatform.oslp.Oslp.Schedule;
import org.opensmartgridplatform.oslp.Oslp.SetConfigurationRequest;
import org.opensmartgridplatform.oslp.Oslp.SetConfigurationResponse;
import org.opensmartgridplatform.oslp.Oslp.SetRebootRequest;
import org.opensmartgridplatform.oslp.Oslp.SetScheduleRequest;
import org.opensmartgridplatform.oslp.Oslp.SetTransitionRequest;
import org.opensmartgridplatform.oslp.Oslp.Status;
import org.opensmartgridplatform.oslp.Oslp.TransitionType;
import org.opensmartgridplatform.oslp.Oslp.TriggerType;
import org.opensmartgridplatform.oslp.Oslp.UpdateFirmwareRequest;
import org.opensmartgridplatform.oslp.Oslp.Weekday;
import org.opensmartgridplatform.oslp.OslpEnvelope;
import org.opensmartgridplatform.oslp.OslpUtils;
import org.opensmartgridplatform.shared.infra.jms.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.google.protobuf.ByteString;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Class which holds all the OSLP device mock steps in order to let the device
 * mock behave correctly for the automatic test.
 */
public class OslpDeviceSteps {

    @Autowired
    private CoreDeviceConfiguration configuration;

    @Autowired
    private MockOslpServer oslpMockServer;

    @Autowired
    private OslpDeviceRepository oslpDeviceRepository;

    /**
     * Verify that a get configuration OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     */
    @Then("^a get configuration \"([^\"]*)\" message is sent to device \"([^\"]*)\"$")
    public void aGetConfigurationOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.GET_CONFIGURATION);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasGetConfigurationRequest());
    }

    /**
     * Verify that a get firmware version OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     */
    @Then("^a get firmware version \"([^\"]*)\" message is sent to device \"([^\"]*)\"$")
    public void aGetFirmwareVersionOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.GET_FIRMWARE_VERSION);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasGetFirmwareVersionRequest());

        message.getGetFirmwareVersionRequest();
    }

    /**
     * Verify that a get power usage history OSLP message is sent to the device.
     *
     * @param expectedParameters
     *            The parameters expected in the message of the device.
     */
    @Then("^a get power usage history \"([^\"]*)\" message is sent to the device$")
    public void aGetPowerUsageHistoryOslpMessageIsSentToTheDevice(final String protocol,
            final Map<String, String> expectedParameters) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.GET_POWER_USAGE_HISTORY);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasGetPowerUsageHistoryRequest());

        final GetPowerUsageHistoryRequest request = message.getGetPowerUsageHistoryRequest();
        Assert.assertEquals(getEnum(expectedParameters, PlatformPubliclightingKeys.HISTORY_TERM_TYPE,
                HistoryTermType.class, PlatformPubliclightingDefaults.DEFAULT_OSLP_HISTORY_TERM_TYPE),
                request.getTermType());
        if (expectedParameters.containsKey(PlatformPubliclightingKeys.KEY_PAGE)
                && !expectedParameters.get(PlatformPubliclightingKeys.KEY_PAGE).isEmpty()) {
            Assert.assertEquals((int) getInteger(expectedParameters, PlatformPubliclightingKeys.KEY_PAGE),
                    request.getPage());
        }
        if (expectedParameters.containsKey(PlatformPubliclightingKeys.START_TIME)
                && !expectedParameters.get(PlatformPubliclightingKeys.START_TIME).isEmpty()
                && expectedParameters.get(PlatformPubliclightingKeys.START_TIME) != null) {
            Assert.assertEquals(getString(expectedParameters, PlatformPubliclightingKeys.START_TIME),
                    request.getTimePeriod().getStartTime());
        }
        if (expectedParameters.containsKey(PlatformPubliclightingKeys.END_TIME)
                && !expectedParameters.get(PlatformPubliclightingKeys.END_TIME).isEmpty()
                && expectedParameters.get(PlatformPubliclightingKeys.END_TIME) != null) {
            Assert.assertEquals(getString(expectedParameters, PlatformPubliclightingKeys.END_TIME),
                    request.getTimePeriod().getEndTime());
        }
    }

    /**
     * Verify that a get status OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     */
    @Then("^a get status \"([^\"]*)\" message is sent to device \"([^\"]*)\"$")
    public void aGetStatusOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.GET_STATUS);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasGetStatusRequest());
    }

    /**
     * Verify that a get firmware version OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     */
    @Then("^an update firmware \"([^\"]*)\" message is sent to the device$")
    public void anUpdateFirmwareOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.UPDATE_FIRMWARE);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasUpdateFirmwareRequest());

        message.getUpdateFirmwareRequest();
    }

    @Then("^an update key \"([^\"]*)\" message is sent to device \"([^\"]*)\"$")
    public void anUpdateKeyOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.UPDATE_KEY);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasSetDeviceVerificationKeyRequest());
    }

    /**
     * Verify that a resume schedule OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     */
    @Then("^a resume schedule \"([^\"]*)\" message is sent to device \"([^\"]*)\"$")
    public void aResumeScheduleOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification,
            final Map<String, String> expectedRequest) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.RESUME_SCHEDULE);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasResumeScheduleRequest());

        final ResumeScheduleRequest request = message.getResumeScheduleRequest();
        /*
         * resumeScheduleRequest { index: "\000" immediate: false }
         */
        Assert.assertEquals(getInteger(expectedRequest, PlatformPubliclightingKeys.KEY_INDEX),
                OslpUtils.byteStringToInteger(request.getIndex()));
        Assert.assertEquals(getBoolean(expectedRequest, PlatformPubliclightingKeys.KEY_ISIMMEDIATE),
                request.getImmediate());
    }

    /**
     * Verify that a set configuration OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     */
    @Then("^a set configuration \"([^\"]*)\" message is sent to device \"([^\"]*)\"$")
    public void aSetConfigurationOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification,
            final Map<String, String> expectedResponseData) {
        final Message receivedMessage = this.oslpMockServer.waitForRequest(MessageType.SET_CONFIGURATION);
        Assert.assertNotNull(receivedMessage);
        Assert.assertTrue(receivedMessage.hasSetConfigurationRequest());

        final SetConfigurationRequest receivedConfiguration = receivedMessage.getSetConfigurationRequest();

        if (!StringUtils.isEmpty(expectedResponseData.get(PlatformKeys.KEY_LIGHTTYPE))
                && receivedConfiguration.getLightType() != null) {
            final LightType expectedLightType = getEnum(expectedResponseData, PlatformKeys.KEY_LIGHTTYPE,
                    LightType.class);
            Assert.assertEquals(expectedLightType, receivedConfiguration.getLightType());

            switch (expectedLightType) {
            case DALI:
                final DaliConfiguration receivedDaliConfiguration = receivedConfiguration.getDaliConfiguration();
                if (receivedDaliConfiguration != null) {
                    if (expectedResponseData.containsKey(PlatformKeys.DC_LIGHTS)
                            && !expectedResponseData.get(PlatformKeys.DC_LIGHTS).isEmpty()) {
                        Assert.assertEquals(getInteger(expectedResponseData, PlatformKeys.DC_LIGHTS),
                                OslpUtils.byteStringToInteger(receivedDaliConfiguration.getNumberOfLights()));
                    }

                    if (expectedResponseData.containsKey(PlatformKeys.DC_MAP)
                            && !expectedResponseData.get(PlatformKeys.DC_MAP).isEmpty()) {
                        Assert.assertNotNull(receivedDaliConfiguration.getAddressMapList());

                        final String[] expectedDcMapArray = getString(expectedResponseData, PlatformKeys.DC_MAP)
                                .split(";");
                        Assert.assertEquals(expectedDcMapArray.length,
                                receivedDaliConfiguration.getAddressMapList().size());

                        final List<IndexAddressMap> receivedIndexAddressMapList = receivedDaliConfiguration
                                .getAddressMapList();
                        for (int i = 0; i < expectedDcMapArray.length; i++) {
                            final String[] expectedDcMapArrayElements = expectedDcMapArray[i].split(",");
                            Assert.assertEquals((Integer) Integer.parseInt(expectedDcMapArrayElements[0]),
                                    OslpUtils.byteStringToInteger(receivedIndexAddressMapList.get(i).getIndex()));
                            Assert.assertEquals((Integer) Integer.parseInt(expectedDcMapArrayElements[1]),
                                    OslpUtils.byteStringToInteger(receivedIndexAddressMapList.get(i).getAddress()));
                        }
                    }
                }
                break;

            case RELAY:
                final RelayConfiguration receivedRelayConfiguration = receivedConfiguration.getRelayConfiguration();
                if (receivedRelayConfiguration != null) {

                    if (!StringUtils.isEmpty(expectedResponseData.get(PlatformKeys.RELAY_CONF))
                            && receivedRelayConfiguration.getAddressMapList() != null) {

                        // Construct sorted list of received relay maps
                        final List<RelayMap> receivedRelayMapList = RelayMapConverter
                                .convertIndexAddressMapListToRelayMapList(
                                        receivedRelayConfiguration.getAddressMapList());
                        Collections.sort(receivedRelayMapList);

                        // Construct sorted list of expected relay maps
                        final String[] expectedRelayMapArray = getString(expectedResponseData, PlatformKeys.RELAY_CONF)
                                .split(";");
                        final List<RelayMap> expectedRelayMapList = RelayMapConverter
                                .convertStringsListToRelayMapList(expectedRelayMapArray);
                        Collections.sort(expectedRelayMapList);

                        Assert.assertEquals("Either the expected or the received relay maps are empty, but not both",
                                CollectionUtils.isEmpty(expectedRelayMapList),
                                CollectionUtils.isEmpty(receivedRelayMapList));

                        if (!CollectionUtils.isEmpty(receivedRelayMapList)
                                && !CollectionUtils.isEmpty(expectedRelayMapList)) {
                            Assert.assertEquals("Size of expected and received relay map list differs",
                                    expectedRelayMapList.size(), receivedRelayMapList.size());
                        }

                        // Compare the contents of each relay map
                        for (int i = 0; i < expectedRelayMapList.size(); i++) {
                            Assert.assertEquals("Expected and received relay map differs for " + i,
                                    expectedRelayMapList.get(i), receivedRelayMapList.get(i));
                        }
                    }
                }
                break;

            case ONE_TO_TEN_VOLT:
            case ONE_TO_TEN_VOLT_REVERSE:
            case LT_NOT_SET:
            default:
                Assert.assertEquals(0, receivedConfiguration.getDaliConfiguration().getAddressMapList().size());
                Assert.assertEquals(0, receivedConfiguration.getRelayConfiguration().getAddressMapList().size());
            }
        }

        if (!StringUtils.isEmpty(expectedResponseData.get(PlatformKeys.KEY_PREFERRED_LINKTYPE))
                && receivedConfiguration.getPreferredLinkType() != null) {
            Assert.assertEquals(getEnum(expectedResponseData, PlatformKeys.KEY_PREFERRED_LINKTYPE, LinkType.class),
                    receivedConfiguration.getPreferredLinkType());
        }

        if (!StringUtils.isEmpty(expectedResponseData.get(PlatformKeys.METER_TYPE))
                && receivedConfiguration.getMeterType() != null) {
            MeterType meterType;
            meterType = getEnum(expectedResponseData, PlatformKeys.METER_TYPE, MeterType.class);
            Assert.assertEquals(meterType, receivedConfiguration.getMeterType());
        }

        if (!StringUtils.isEmpty(expectedResponseData.get(PlatformKeys.SHORT_INTERVAL))
                && receivedConfiguration.getShortTermHistoryIntervalMinutes() != 0) {
            Assert.assertEquals(
                    (int) getInteger(expectedResponseData, PlatformKeys.SHORT_INTERVAL,
                            PlatformDefaults.DEFAULT_SHORT_INTERVAL),
                    receivedConfiguration.getShortTermHistoryIntervalMinutes());
        }

        if (!StringUtils.isEmpty(expectedResponseData.get(PlatformKeys.LONG_INTERVAL))
                && receivedConfiguration.getLongTermHistoryInterval() != 0) {
            Assert.assertEquals(
                    (int) getInteger(expectedResponseData, PlatformKeys.LONG_INTERVAL,
                            PlatformDefaults.DEFAULT_LONG_INTERVAL),
                    receivedConfiguration.getLongTermHistoryInterval());
        }

        if (!StringUtils.isEmpty(expectedResponseData.get(PlatformKeys.INTERVAL_TYPE))
                && receivedConfiguration.getLongTermHistoryIntervalType() != null) {
            Assert.assertEquals(getEnum(expectedResponseData, PlatformKeys.INTERVAL_TYPE, LongTermIntervalType.class),
                    receivedConfiguration.getLongTermHistoryIntervalType());
        }

        if (!StringUtils.isEmpty(expectedResponseData.get(PlatformKeys.OSGP_IP_ADDRESS))) {
            Assert.assertEquals(expectedResponseData.get(PlatformKeys.OSGP_IP_ADDRESS),
                    this.convertIpAddress(receivedConfiguration.getOspgIpAddress()));
        }

        if (!StringUtils.isEmpty(expectedResponseData.get(PlatformKeys.OSGP_PORT))) {
            Assert.assertEquals(Integer.parseInt(expectedResponseData.get(PlatformKeys.OSGP_PORT)),
                    receivedConfiguration.getOsgpPortNumber());
        }

        if (!StringUtils.isEmpty(expectedResponseData.get(PlatformKeys.KEY_ASTRONOMICAL_SUNRISE_OFFSET))) {
            Assert.assertEquals(
                    Integer.parseInt(expectedResponseData.get(PlatformKeys.KEY_ASTRONOMICAL_SUNRISE_OFFSET)),
                    receivedConfiguration.getAstroGateSunRiseOffset());
        }

        if (!StringUtils.isEmpty(expectedResponseData.get(PlatformKeys.KEY_ASTRONOMICAL_SUNSET_OFFSET))) {
            Assert.assertEquals(Integer.parseInt(expectedResponseData.get(PlatformKeys.KEY_ASTRONOMICAL_SUNSET_OFFSET)),
                    receivedConfiguration.getAstroGateSunSetOffset());
        }
    }

    /**
     * Verify that an event notification OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     */
    @Then("^a set event notification \"([^\"]*)\" message is sent to device \"([^\"]*)\"")
    public void aSetEventNotificationOslpMessageIsSentToDevice(final String protocol,
            final String deviceIdentification) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.SET_EVENT_NOTIFICATIONS);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasSetEventNotificationsRequest());
    }

    /**
     * Verify that a set light OSLP message is sent to the device.
     *
     * @param nofLightValues
     *            The parameters expected in the message of the device.
     */
    @Then("^a set light \"([^\"]*)\" message with \"([^\"]*)\" lightvalues is sent to the device$")
    public void aSetLightOslpMessageWithLightValuesIsSentToTheDevice(final String protocol, final int nofLightValues) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.SET_LIGHT);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasSetLightRequest());

        Assert.assertEquals(nofLightValues, message.getSetLightRequest().getValuesList().size());
    }

    /**
     * Verify that a set light OSLP message is sent to the device.
     *
     * @param expectedParameters
     *            The parameters expected in the message of the device.
     */
    @Then("^a set light \"([^\"]*)\" message with one light value is sent to the device$")
    public void aSetLightOSLPMessageWithOneLightvalueIsSentToTheDevice(final String protocol,
            final Map<String, String> expectedParameters) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.SET_LIGHT);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasSetLightRequest());

        final LightValue lightValue = message.getSetLightRequest().getValues(0);

        Assert.assertEquals(
                getInteger(expectedParameters, PlatformPubliclightingKeys.KEY_INDEX,
                        PlatformPubliclightingDefaults.DEFAULT_INDEX),
                OslpUtils.byteStringToInteger(lightValue.getIndex()));
        if (expectedParameters.containsKey(PlatformPubliclightingKeys.KEY_DIMVALUE)
                && !StringUtils.isEmpty(expectedParameters.get(PlatformPubliclightingKeys.KEY_DIMVALUE))) {
            Assert.assertEquals(
                    getInteger(expectedParameters, PlatformPubliclightingKeys.KEY_DIMVALUE,
                            PlatformPubliclightingDefaults.DEFAULT_DIMVALUE),
                    OslpUtils.byteStringToInteger(lightValue.getDimValue()));
        }
        Assert.assertEquals(getBoolean(expectedParameters, PlatformPubliclightingKeys.KEY_ON,
                PlatformPubliclightingDefaults.DEFAULT_ON), lightValue.getOn());
    }

    /**
     * Verify that a set light schedule OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device. @
     */
    @Then("^a set light schedule \"([^\"]*)\" message is sent to device \"([^\"]*)\"$")
    public void aSetLightScheduleOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification,
            final Map<String, String> expectedRequest) {
        this.checkAndValidateRequest(MessageType.SET_LIGHT_SCHEDULE, expectedRequest);
    }

    /**
     * Verify that a set reboot OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     */
    @Then("^a set reboot \"([^\"]*)\" message is sent to device \"([^\"]*)\"$")
    public void aSetRebootOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.SET_REBOOT);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasSetRebootRequest());

        @SuppressWarnings("unused")
        final SetRebootRequest request = message.getSetRebootRequest();
    }

    /**
     * Verify that a set reverse tariff schedule OSLP message is sent to the
     * device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     * @param expectedRequest
     *            The request parameters expected in the message to the device.
     */
    @Then("^a set reverse tariff schedule \"([^\"]*)\" message is sent to device \"(?:([^\"]*))\"$")
    public void aSetReverseTariffScheduleOSLPMessageIsSentToDevice(final String protocol,
            final String deviceIdentification, final Map<String, String> expectedRequest) {
        this.aSetTariffScheduleOSLPMessageIsSentToDevice(protocol, deviceIdentification, expectedRequest);
    }

    /**
     * Verify that a set tariff schedule OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     */
    @Then("^a set tariff schedule \"([^\"]*)\" message is sent to device \"(?:([^\"]*))\"$")
    public void aSetTariffScheduleOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification,
            final Map<String, String> expectedRequest) {
        this.checkAndValidateRequest(MessageType.SET_TARIFF_SCHEDULE, expectedRequest);
    }

    /**
     * Verify that a set transition OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     */
    @Then("^a set transition \"([^\"]*)\" message is sent to device \"([^\"]*)\"$")
    public void aSetTransitionOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification,
            final Map<String, String> expectedResult) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.SET_TRANSITION);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasSetTransitionRequest());

        final SetTransitionRequest request = message.getSetTransitionRequest();

        Assert.assertEquals(
                getEnum(expectedResult, PlatformPubliclightingKeys.KEY_TRANSITION_TYPE, TransitionType.class),
                request.getTransitionType());
        if (expectedResult.containsKey(PlatformPubliclightingKeys.KEY_TIME)) {
            // TODO: How to check the time?
            // Assert.assertEquals(expectedResult.get(Keys.KEY_TIME),
            // request.getTime());
        }
    }

    /**
     * Verify that a start device OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     */
    @Then("^a start device \"([^\"]*)\" message is sent to device \"([^\"]*)\"$")
    public void aStartDeviceOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.START_SELF_TEST);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasStartSelfTestRequest());
    }

    /**
     * Verify that a stop device OSLP message is sent to the device.
     *
     * @param deviceIdentification
     *            The device identification expected in the message to the
     *            device.
     */
    @Then("^a stop device \"([^\"]*)\" message is sent to device \"([^\"]*)\"$")
    public void aStopDeviceOSLPMessageIsSentToDevice(final String protocol, final String deviceIdentification) {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.STOP_SELF_TEST);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasStopSelfTestRequest());
    }

    /**
     * Setup method to get a status which should be returned by the mock.
     */
    private void callMockSetScheduleResponse(final String result, final MessageType type) {
        this.oslpMockServer.mockSetScheduleResponse(type, Enum.valueOf(Status.class, result));
    }

    private void checkAndValidateRequest(final MessageType type, final Map<String, String> expectedRequest) {
        final Message message = this.oslpMockServer.waitForRequest(type);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasSetScheduleRequest());

        final SetScheduleRequest request = message.getSetScheduleRequest();

        for (final Schedule schedule : request.getSchedulesList()) {
            if (type == MessageType.SET_LIGHT_SCHEDULE) {
                Assert.assertEquals(
                        getEnum(expectedRequest, PlatformPubliclightingKeys.SCHEDULE_WEEKDAY, Weekday.class),
                        schedule.getWeekday());
            }
            if (StringUtils.isNotBlank(expectedRequest.get(PlatformPubliclightingKeys.SCHEDULE_STARTDAY))) {
                final String startDay = getDate(expectedRequest, PlatformPubliclightingKeys.SCHEDULE_STARTDAY)
                        .toDateTime(DateTimeZone.UTC).toString("yyyyMMdd");

                Assert.assertEquals(startDay, schedule.getStartDay());
            }
            if (StringUtils.isNotBlank(expectedRequest.get(PlatformPubliclightingKeys.SCHEDULE_ENDDAY))) {
                final String endDay = getDate(expectedRequest, PlatformPubliclightingKeys.SCHEDULE_ENDDAY)
                        .toDateTime(DateTimeZone.UTC).toString("yyyyMMdd");

                Assert.assertEquals(endDay, schedule.getEndDay());
            }

            if (type == MessageType.SET_LIGHT_SCHEDULE) {
                Assert.assertEquals(
                        getEnum(expectedRequest, PlatformPubliclightingKeys.SCHEDULE_ACTIONTIME, ActionTime.class),
                        schedule.getActionTime());
            }
            if (StringUtils.isNotBlank(expectedRequest.get(PlatformPubliclightingKeys.SCHEDULE_TIME))) {
                String expectedTime = getString(expectedRequest, PlatformPubliclightingKeys.SCHEDULE_TIME).replace(":",
                        "");
                if (expectedTime.contains(".")) {
                    expectedTime = expectedTime.substring(0, expectedTime.indexOf("."));
                }
                Assert.assertEquals(expectedTime, schedule.getTime());
            }
            final String scheduleLightValue = getString(expectedRequest,
                    (type == MessageType.SET_LIGHT_SCHEDULE) ? PlatformPubliclightingKeys.SCHEDULE_LIGHTVALUES
                            : PlatformPubliclightingKeys.SCHEDULE_TARIFFVALUES);
            final String[] scheduleLightValues = scheduleLightValue.split(";");
            Assert.assertEquals(scheduleLightValues.length, schedule.getValueCount());
            for (int i = 0; i < scheduleLightValues.length; i++) {
                final Integer index = OslpUtils.byteStringToInteger(schedule.getValue(i).getIndex()),
                        dimValue = OslpUtils.byteStringToInteger(schedule.getValue(i).getDimValue());
                if (type == MessageType.SET_LIGHT_SCHEDULE) {
                    Assert.assertEquals(scheduleLightValues[i], String.format("%s,%s,%s", (index != null) ? index : "",
                            schedule.getValue(i).getOn(), (dimValue != null) ? dimValue : ""));
                } else if (type == MessageType.SET_TARIFF_SCHEDULE) {
                    Assert.assertEquals(scheduleLightValues[i],
                            String.format("%s,%s", (index != null) ? index : "", !schedule.getValue(i).getOn()));
                }
            }

            if (type == MessageType.SET_LIGHT_SCHEDULE) {
                Assert.assertEquals(
                        (!getString(expectedRequest, PlatformPubliclightingKeys.SCHEDULE_TRIGGERTYPE).isEmpty())
                                ? getEnum(expectedRequest, PlatformPubliclightingKeys.SCHEDULE_TRIGGERTYPE,
                                        TriggerType.class)
                                : TriggerType.TT_NOT_SET,
                        schedule.getTriggerType());

                if (StringUtils.isNotBlank(expectedRequest.get(PlatformPubliclightingKeys.SCHEDULE_TRIGGERWINDOW))) {
                    final String[] windowTypeValues = getString(expectedRequest,
                            PlatformPubliclightingKeys.SCHEDULE_TRIGGERWINDOW).split(",");
                    if (windowTypeValues.length == 2) {
                        Assert.assertEquals(Integer.parseInt(windowTypeValues[0]),
                                schedule.getWindow().getMinutesBefore());
                        Assert.assertEquals(Integer.parseInt(windowTypeValues[1]),
                                schedule.getWindow().getMinutesAfter());
                    }
                }
            }
        }
    }

    /**
     * Simulates sending an OSLP EventNotification message to the OSLP Protocol
     * adapter.
     *
     * @throws DeviceSimulatorException
     * @throws IOException
     * @throws ParseException
     */
    @When("^receiving an \"([^\"]*)\" event notification message$")
    public void receivingAnOSLPEventNotificationMessage(final String protocol, final Map<String, String> settings)
            throws DeviceSimulatorException, IOException, ParseException {

        final EventNotification eventNotification = EventNotification.newBuilder()
                .setDescription(getString(settings, PlatformPubliclightingKeys.KEY_DESCRIPTION, ""))
                .setEvent(getEnum(settings, PlatformPubliclightingKeys.KEY_EVENT, Event.class)).build();

        final Message message = Oslp.Message.newBuilder()
                .setEventNotificationRequest(EventNotificationRequest.newBuilder().addNotifications(eventNotification))
                .build();

        // Save the OSLP response for later validation.
        ScenarioContext.current().put(PlatformPubliclightingKeys.RESPONSE, this.oslpMockServer.sendRequest(message));
    }

    @Given("^the device returns a get configuration status over \"([^\"]*)\"$")
    public void theDeviceReturnsAGetConfigurationStatusOverOSLP(final String protocol,
            final Map<String, String> requestParameters) throws UnknownHostException {
        this.theDeviceReturnsAGetConfigurationStatusWithResultOverOSLP(
                getEnum(requestParameters, PlatformPubliclightingKeys.KEY_STATUS, Status.class, Status.OK).name(),
                protocol, requestParameters);
    }

    @Given("^the device returns a get configuration status \"([^\"]*)\" over \"([^\"]*)\" using default values$")
    public void theDeviceReturnsAGetConfigurationStatusWithResultOverOSLP(final String result, final String protocol)
            throws UnknownHostException {
        final Map<String, String> requestParameters = new HashMap<>();
        this.theDeviceReturnsAGetConfigurationStatusWithResultOverOSLP(result, protocol, requestParameters);
    }

    /**
     * Setup method to set the configuration status which should be returned by
     * the mock.
     *
     * @throws UnknownHostException
     */
    @Given("^the device returns a get configuration status \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsAGetConfigurationStatusWithResultOverOSLP(final String result, final String protocol,
            final Map<String, String> requestParameters) throws UnknownHostException {
        // Note: This piece of code has been made because there are multiple
        // enumerations with the name MeterType, but not all of them has all
        // values the same. Some with underscore and some without.
        MeterType meterType;
        final String sMeterType = getString(requestParameters, PlatformPubliclightingKeys.METER_TYPE, "");
        if (!sMeterType.contains("_") && sMeterType.equals(MeterType.P1_VALUE)) {
            final String[] sMeterTypeArray = sMeterType.split("");
            meterType = MeterType.valueOf(sMeterTypeArray[0] + "_" + sMeterTypeArray[1]);
        } else {
            meterType = getEnum(requestParameters, PlatformPubliclightingKeys.METER_TYPE, MeterType.class,
                    PlatformPubliclightingDefaults.DEFAULT_OSLP_METER_TYPE);
        }

        final String osgpIpAddress = getString(requestParameters, PlatformPubliclightingKeys.OSGP_IP_ADDRESS);
        String osgpIpAddressMock;
        if (StringUtils.isEmpty(osgpIpAddress)) {
            osgpIpAddressMock = null;
        } else {
            osgpIpAddressMock = osgpIpAddress;
        }

        this.oslpMockServer.mockGetConfigurationResponse(Enum.valueOf(Status.class, result),
                getEnum(requestParameters, PlatformPubliclightingKeys.KEY_LIGHTTYPE, LightType.class,
                        PlatformPubliclightingDefaults.DEFAULT_LIGHTTYPE),
                getString(requestParameters, PlatformPubliclightingKeys.DC_LIGHTS,
                        PlatformPubliclightingDefaults.DC_LIGHTS),
                getString(requestParameters, PlatformPubliclightingKeys.DC_MAP,
                        PlatformPubliclightingDefaults.DEFAULT_DC_MAP),
                getString(requestParameters, PlatformPubliclightingKeys.RELAY_CONF,
                        PlatformPubliclightingDefaults.DEFAULT_RELAY_CONFIGURATION),
                getEnum(requestParameters, PlatformPubliclightingKeys.KEY_PREFERRED_LINKTYPE, LinkType.class,
                        PlatformPubliclightingDefaults.DEFAULT_PREFERRED_LINKTYPE),
                meterType,
                getInteger(requestParameters, PlatformPubliclightingKeys.SHORT_INTERVAL,
                        PlatformPubliclightingDefaults.SHORT_INTERVAL),
                getInteger(requestParameters, PlatformPubliclightingKeys.LONG_INTERVAL,
                        PlatformPubliclightingDefaults.LONG_INTERVAL),
                getEnum(requestParameters, PlatformPubliclightingKeys.INTERVAL_TYPE, LongTermIntervalType.class,
                        PlatformPubliclightingDefaults.DEFAULT_INTERVAL_TYPE),
                osgpIpAddressMock, getInteger(requestParameters, PlatformPubliclightingKeys.OSGP_PORT,
                        PlatformPubliclightingDefaults.DEFAULT_OSLP_PORT));
    }

    /**
     * Setup method to get the power usage history which should be returned by
     * the mock.
     */
    @Given("^the device returns a get power usage history response \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsAGetPowerUsageHistoryOverOSLP(final String result, final String protocol,
            final Map<String, String> requestParameters) {

        final Map<String, String[]> requestMap = new HashMap<>();

        for (final String key : requestParameters.keySet()) {
            final String[] values = requestParameters.get(key)
                    .split(PlatformPubliclightingKeys.SEPARATOR_SPACE_COLON_SPACE);
            requestMap.put(key, values);
        }

        this.oslpMockServer.mockGetPowerUsageHistoryResponse(Enum.valueOf(Status.class, result), requestMap);
    }

    /**
     * Setup method to get a status which should be returned by the mock.
     */
    @Given("^the device returns a get status response \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsAGetStatusResponseWithResultOverOSLP(final String result, final String protocol,
            final Map<String, String> requestParameters) {

        int eventNotificationTypes = 0;
        if (getString(requestParameters, PlatformPubliclightingKeys.KEY_EVENTNOTIFICATIONTYPES,
                PlatformPubliclightingDefaults.DEFAULT_EVENTNOTIFICATIONTYPES).trim()
                        .split(PlatformPubliclightingKeys.SEPARATOR_COMMA).length > 0) {
            for (final String eventNotificationType : getString(requestParameters,
                    PlatformPubliclightingKeys.KEY_EVENTNOTIFICATIONTYPES,
                    PlatformPubliclightingDefaults.DEFAULT_EVENTNOTIFICATIONTYPES).trim()
                            .split(PlatformPubliclightingKeys.SEPARATOR_COMMA)) {
                if (!eventNotificationType.isEmpty()) {
                    eventNotificationTypes = eventNotificationTypes
                            + Enum.valueOf(EventNotificationType.class, eventNotificationType.trim()).getValue();
                }
            }
        }

        final List<LightValue> lightValues = new ArrayList<>();
        if (!getString(requestParameters, PlatformPubliclightingKeys.KEY_LIGHTVALUES,
                PlatformPubliclightingDefaults.DEFAULT_LIGHTVALUES).isEmpty()
                && getString(requestParameters, PlatformPubliclightingKeys.KEY_LIGHTVALUES,
                        PlatformPubliclightingDefaults.DEFAULT_LIGHTVALUES)
                                .split(PlatformPubliclightingKeys.SEPARATOR_SEMICOLON).length > 0) {

            for (final String lightValueString : getString(requestParameters,
                    PlatformPubliclightingKeys.KEY_LIGHTVALUES, PlatformPubliclightingDefaults.DEFAULT_LIGHTVALUES)
                            .split(PlatformPubliclightingKeys.SEPARATOR_SEMICOLON)) {
                final String[] parts = lightValueString.split(PlatformPubliclightingKeys.SEPARATOR_COMMA);

                final LightValue lightValue = LightValue.newBuilder()
                        .setIndex(OslpUtils.integerToByteString(Integer.parseInt(parts[0])))
                        .setOn(parts[1].toLowerCase().equals("true"))
                        .setDimValue(OslpUtils.integerToByteString(Integer.parseInt(parts[2]))).build();

                lightValues.add(lightValue);
            }
        }

        final List<LightValue> tariffValues = new ArrayList<>();
        if (!getString(requestParameters, PlatformPubliclightingKeys.KEY_TARIFFVALUES,
                PlatformPubliclightingDefaults.DEFAULT_TARIFFVALUES).isEmpty()
                && getString(requestParameters, PlatformPubliclightingKeys.KEY_TARIFFVALUES,
                        PlatformPubliclightingDefaults.DEFAULT_TARIFFVALUES)
                                .split(PlatformPubliclightingKeys.SEPARATOR_SEMICOLON).length > 0) {

            for (final String tariffValueString : getString(requestParameters,
                    PlatformPubliclightingKeys.KEY_TARIFFVALUES, PlatformPubliclightingDefaults.DEFAULT_TARIFFVALUES)
                            .split(PlatformPubliclightingKeys.SEPARATOR_SEMICOLON)) {
                final String[] parts = tariffValueString.split(PlatformPubliclightingKeys.SEPARATOR_COMMA);

                final LightValue tariffValue = LightValue.newBuilder()
                        .setIndex(OslpUtils.integerToByteString(Integer.parseInt(parts[0])))
                        .setOn(parts[1].toLowerCase().equals("true")).build();

                tariffValues.add(tariffValue);
            }
        }

        this.oslpMockServer.mockGetStatusResponse(
                getEnum(requestParameters, PlatformPubliclightingKeys.KEY_PREFERRED_LINKTYPE, LinkType.class,
                        PlatformPubliclightingDefaults.DEFAULT_PREFERRED_LINKTYPE),
                getEnum(requestParameters, PlatformPubliclightingKeys.KEY_ACTUAL_LINKTYPE, LinkType.class,
                        PlatformPubliclightingDefaults.DEFAULT_ACTUAL_LINKTYPE),
                getEnum(requestParameters, PlatformPubliclightingKeys.KEY_LIGHTTYPE, LightType.class,
                        PlatformPubliclightingDefaults.DEFAULT_LIGHTTYPE),
                eventNotificationTypes, Enum.valueOf(Status.class, result), lightValues, tariffValues);
    }

    /**
     * Setup method to resume a schedule which should be returned by the mock.
     */
    @Given("^the device returns a resume schedule response \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsAResumeScheduleResponseOverOSLP(final String result, final String protocol) {
        this.oslpMockServer.mockResumeScheduleResponse(Enum.valueOf(Status.class, result));
    }

    @Given("^the device returns a set configuration status over \"([^\"]*)\"$")
    public void theDeviceReturnsASetConfigurationStatusOverOSLP(final String protocol,
            final Map<String, String> settings) {
        this.theDeviceReturnsASetConfigurationStatusWithStatusOverOSLP(
                getEnum(settings, PlatformPubliclightingKeys.KEY_STATUS, Status.class).name(), protocol);
    }

    /**
     * Setup method to set the configuration status which should be returned by
     * the mock.
     */
    @Given("^the device returns a set configuration status \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsASetConfigurationStatusWithStatusOverOSLP(final String result, final String protocol) {
        this.oslpMockServer.mockSetConfigurationResponse(Enum.valueOf(Status.class, result));
    }

    /**
     * Setup method to set the event notification which should be returned by
     * the mock.
     */
    @Given("^the device returns a set event notification \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsASetEventNotificationOverOSLP(final String result, final String protocol) {
        this.oslpMockServer.mockSetEventNotificationResponse(Enum.valueOf(Status.class, result));
    }

    /**
     * Setup method to set a light which should be returned by the mock.
     */
    @Given("^the device returns a set light response \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsASetLightOverOSLP(final String result, final String protocol) {
        this.oslpMockServer.mockSetLightResponse(Enum.valueOf(Status.class, result));
    }

    /**
     * Setup method to get a status which should be returned by the mock.
     */
    @Given("^the device returns a set light schedule response \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsASetLightScheduleResponseOverOSLP(final String result, final String protocol) {
        this.callMockSetScheduleResponse(result, MessageType.SET_LIGHT_SCHEDULE);
    }

    /**
     * Setup method which combines get configuration, set configuration and set
     * schedule mock responses. The protocol adapter component for OSLP executes
     * these 3 steps when a light schedule is pushed to a device. In case of
     * FAILURE response, the protocol adapter will only validate the last of the
     * 3 steps.
     */
    @Given("^the device returns the responses for setting a light schedule with result \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsTheResponsesForSettingLightScheduleWithResultOverProtocol(final String result,
            final String protocol) throws UnknownHostException {
        this.theDeviceReturnsAGetConfigurationStatusWithResultOverOSLP(result, protocol);
        this.theDeviceReturnsASetConfigurationStatusWithStatusOverOSLP(result, protocol);
        this.theDeviceReturnsASetLightScheduleResponseOverOSLP(result, protocol);
    }

    /**
     * Setup method to set a reboot which should be returned by the mock.
     */
    @Given("^the device returns a set reboot response \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsASetRebootResponseOverOSLP(final String result, final String protocol) {
        this.oslpMockServer.mockSetRebootResponse(Enum.valueOf(Status.class, result));
    }

    @Given("^the device returns a set reverse tariff schedule response \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsASetReverseTariffScheduleResponseOverOSLP(final String result, final String protocol) {
        this.theDeviceReturnsASetTariffScheduleResponseOverOSLP(result, protocol);
    }

    @Given("^the device returns a set tariff schedule response \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsASetTariffScheduleResponseOverOSLP(final String result, final String protocol) {
        this.callMockSetScheduleResponse(result, MessageType.SET_TARIFF_SCHEDULE);
    }

    /**
     * Setup method to set a transition which should be returned by the mock.
     */
    @Given("^the device returns a set transition response \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsASetTransitionResponseOverOSLP(final String result, final String protocol) {
        this.oslpMockServer.mockSetTransitionResponse(Enum.valueOf(Status.class, result));
    }

    /**
     * Setup method to start a device which should be returned by the mock.
     */
    @Given("^the device returns a start device response \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsAStartDeviceResponseOverOSLP(final String result, final String protocol) {
        this.oslpMockServer.mockStartDeviceResponse(Enum.valueOf(Status.class, result));
    }

    /**
     * Setup method to stop a device which should be returned by the mock.
     */
    @Given("^the device returns a stop device response \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsAStopDeviceResponseOverOSLP(final String result, final String protocol) {
        // TODO: Check if ByteString.EMPTY must be something else
        this.oslpMockServer.mockStopDeviceResponse(ByteString.EMPTY, Enum.valueOf(Status.class, result));
    }

    /**
     * Setup method to set the firmware which should be returned by the mock.
     *
     * @param firmwareVersion
     *            The firmware to respond.
     */
    @Given("^the device returns firmware version \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsFirmwareVersionOverOSLP(final String firmwareVersion, final String protocol) {
        this.oslpMockServer.mockGetFirmwareVersionResponse(firmwareVersion);
    }

    /**
     * Setup method to set the firmware which should be returned by the mock.
     */
    @Given("^the device returns update firmware response \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsUpdateFirmwareResponseOverOSLP(final String result, final String protocol) {
        this.oslpMockServer.mockUpdateFirmwareResponse(Enum.valueOf(Status.class, result));
    }

    @Then("^the \"([^\"]*)\" event notification response contains$")
    public void theOSLPEventNotificationResponseContains(final String protocol,
            final Map<String, String> expectedResponse) {
        final Message responseMessage = (Message) ScenarioContext.current().get(PlatformPubliclightingKeys.RESPONSE);

        final EventNotificationResponse response = responseMessage.getEventNotificationResponse();

        Assert.assertEquals(getString(expectedResponse, PlatformPubliclightingKeys.KEY_STATUS), response.getStatus());
    }

    @Given("^the device sends a register device request to the platform over \"([^\"]*)\"$")
    public void theDeviceSendsARegisterDeviceRequestToThePlatform(final String protocol,
            final Map<String, String> settings) throws DeviceSimulatorException {

        try {
            final OslpEnvelope request = this
                    .createEnvelopeBuilder(getString(settings, PlatformPubliclightingKeys.KEY_DEVICE_UID,
                            PlatformPubliclightingDefaults.DEVICE_UID), this.oslpMockServer.getSequenceNumber())
                    .withPayloadMessage(
                            Message.newBuilder()
                                    .setRegisterDeviceRequest(Oslp.RegisterDeviceRequest.newBuilder()
                                            .setDeviceIdentification(getString(settings,
                                                    PlatformPubliclightingKeys.KEY_DEVICE_IDENTIFICATION,
                                                    PlatformPubliclightingDefaults.DEFAULT_DEVICE_IDENTIFICATION))
                                            .setIpAddress(ByteString.copyFrom(InetAddress
                                                    .getByName(
                                                            getString(settings, PlatformPubliclightingKeys.IP_ADDRESS,
                                                                    PlatformPubliclightingDefaults.LOCALHOST))
                                                    .getAddress()))
                                            .setDeviceType(getEnum(settings, PlatformPubliclightingKeys.KEY_DEVICE_TYPE,
                                                    DeviceType.class, DeviceType.PSLD))
                                            .setHasSchedule(
                                                    getBoolean(settings, PlatformPubliclightingKeys.KEY_HAS_SCHEDULE,
                                                            PlatformPubliclightingDefaults.DEFAULT_HASSCHEDULE))
                                            .setRandomDevice(
                                                    getInteger(settings, PlatformPubliclightingKeys.RANDOM_DEVICE,
                                                            PlatformPubliclightingDefaults.RANDOM_DEVICE)))
                                    .build())
                    .build();

            this.send(request, settings);
        } catch (final IOException | IllegalArgumentException e) {
            ScenarioContext.current().put("Error", e);
        }
    }

    @Given("^the device sends a confirm register device request to the platform over \"([^\"]*)\"$")
    public void theDeviceSendsAConfirmRegisterDeviceRequestToThePlatform(final String protocol,
            final Map<String, String> settings) throws DeviceSimulatorException {

        try {
            final String deviceIdentification = getString(settings,
                    PlatformPubliclightingKeys.KEY_DEVICE_IDENTIFICATION,
                    PlatformPubliclightingDefaults.DEFAULT_DEVICE_IDENTIFICATION);

            final String deviceUid = getString(settings, PlatformPubliclightingKeys.KEY_DEVICE_UID,
                    PlatformPubliclightingDefaults.DEVICE_UID);

            final OslpDevice oslpDevice = this.oslpDeviceRepository.findByDeviceIdentification(deviceIdentification);
            final int randomDevice = oslpDevice.getRandomDevice();
            final int randomPlatform = oslpDevice.getRandomPlatform();

            final Oslp.ConfirmRegisterDeviceRequest confirmRegisterDeviceRequest = Oslp.ConfirmRegisterDeviceRequest
                    .newBuilder().setRandomDevice(randomDevice).setRandomPlatform(randomPlatform).build();

            final Message message = Message.newBuilder().setConfirmRegisterDeviceRequest(confirmRegisterDeviceRequest)
                    .build();

            final OslpEnvelope request = this.createEnvelopeBuilder(deviceUid, this.oslpMockServer.getSequenceNumber())
                    .withPayloadMessage(message).build();

            this.send(request, settings);
        } catch (final IOException | IllegalArgumentException e) {
            ScenarioContext.current().put("Error", e);
        }
    }

    @Given("^the device sends an event notification request to the platform over \"([^\"]*)\"$")
    public void theDeviceSendsAnEventNotificationRequestToThePlatform(final String protocol,
            final Map<String, String> settings) throws IOException, DeviceSimulatorException {

        this.oslpMockServer.doNextSequenceNumber();

        final Oslp.EventNotification.Builder builder = Oslp.EventNotification.newBuilder()
                .setEvent(getEnum(settings, PlatformKeys.KEY_EVENT, Event.class))
                .setDescription(getString(settings, PlatformKeys.KEY_DESCRIPTION));
        final String timeStamp = getString(settings, PlatformKeys.TIMESTAMP);
        if (timeStamp != null) {
            builder.setTimestamp(timeStamp);
        }

        final String indexValue = getString(settings, PlatformPubliclightingKeys.KEY_INDEX);
        final Integer index = indexValue == null || "EMPTY".equals(indexValue) ? 0 : Integer.valueOf(indexValue);
        builder.setIndex(ByteString.copyFrom(new byte[] { index.byteValue() }));

        final OslpEnvelope request = this
                .createEnvelopeBuilder(getString(settings, PlatformPubliclightingKeys.KEY_DEVICE_UID,
                        PlatformPubliclightingDefaults.DEVICE_UID), this.oslpMockServer.getSequenceNumber())
                .withPayloadMessage(Message.newBuilder()
                        .setEventNotificationRequest(
                                Oslp.EventNotificationRequest.newBuilder().addNotifications(builder.build()))
                        .build())
                .build();

        this.send(request, settings);
    }

    @When("^the device sends multiple event notifications request to the platform over \"([^\"]*)\"$")
    public void theDeviceSendsMultipleEventNotificationsRequestToThePlatform(final String protocol,
            final Map<String, String> settings) throws IOException, DeviceSimulatorException {

        this.oslpMockServer.doNextSequenceNumber();

        final Oslp.EventNotificationRequest.Builder requestBuilder = Oslp.EventNotificationRequest.newBuilder();
        final Oslp.EventNotification.Builder builder = Oslp.EventNotification.newBuilder();

        final String[] events = getString(settings, PlatformPubliclightingKeys.KEY_EVENTS)
                .split(PlatformPubliclightingKeys.SEPARATOR_COMMA),
                indexes = getString(settings, PlatformPubliclightingKeys.KEY_INDEXES)
                        .split(PlatformPubliclightingKeys.SEPARATOR_COMMA);

        for (int i = 0; i < events.length; i++) {
            if (!events[i].isEmpty() && !indexes[i].isEmpty()) {
                builder.setEvent(Event.valueOf(events[i].trim()));

                final String indexValue = indexes[i];
                final Integer index = indexValue == null || "EMPTY".equals(indexValue) ? 0
                        : Integer.valueOf(indexValue);
                builder.setIndex(ByteString.copyFrom(new byte[] { index.byteValue() }));

                requestBuilder.addNotifications(builder.build());
            }
        }

        final OslpEnvelope request = this
                .createEnvelopeBuilder(getString(settings, PlatformPubliclightingKeys.KEY_DEVICE_UID,
                        PlatformPubliclightingDefaults.DEVICE_UID), this.oslpMockServer.getSequenceNumber())
                .withPayloadMessage(Message.newBuilder().setEventNotificationRequest(requestBuilder.build()).build())
                .build();

        this.send(request, settings);
    }

    /**
     * Verify that we have received a response over OSLP/OSLP ELSTER
     */
    @Then("^the event notification response contains$")
    public void theEventNotificationResponseContains(final Map<String, String> expectedResponse) {
        final Message responseMessage = this.oslpMockServer.waitForResponse();

        final EventNotificationResponse response = responseMessage.getEventNotificationResponse();

        Assert.assertEquals(getString(expectedResponse, PlatformPubliclightingKeys.KEY_STATUS),
                response.getStatus().name());
    }

    /**
     * Verify that we have received a response over OSLP/OSLP ELSTER
     */
    @Then("^the set configuration response contains$")
    public void theSetConfigurationResponseContains(final Map<String, String> expectedResponse) {
        final Message responseMessage = this.oslpMockServer.waitForResponse();

        final SetConfigurationResponse response = responseMessage.getSetConfigurationResponse();

        Assert.assertEquals(getString(expectedResponse, PlatformPubliclightingKeys.KEY_STATUS),
                response.getStatus().name());
    }

    @Given("^the device sends an event notification request with sequencenumber \"([^\"]*)\" to the platform over \"([^\"]*)\"$")
    public void theDeviceSendsAStartDeviceResponseOver(final Integer sequenceNumber, final String protocol,
            final Map<String, String> settings) throws IOException, DeviceSimulatorException {

        ScenarioContext.current().put(PlatformPubliclightingKeys.NUMBER_TO_ADD_TO_SEQUENCE_NUMBER, sequenceNumber);

        this.theDeviceSendsAnEventNotificationRequestToThePlatform(protocol, settings);
    }

    /**
     * Verify that we have received a response over OSLP/OSLP ELSTER
     */
    @Then("^the register device response contains$")
    public void theRegisterDeviceResponseContains(final Map<String, String> expectedResponse)
            throws IOException, DeviceSimulatorException {
        final Exception e = (Exception) ScenarioContext.current().get("Error");
        if (e == null || getString(expectedResponse, PlatformPubliclightingKeys.MESSAGE) == null) {
            final Message responseMessage = this.oslpMockServer.waitForResponse();

            final RegisterDeviceResponse response = responseMessage.getRegisterDeviceResponse();

            Assert.assertNotNull(response.getCurrentTime());
            Assert.assertNotNull(response.getLocationInfo().getLongitude());
            Assert.assertNotNull(response.getLocationInfo().getLatitude());
            Assert.assertNotNull(response.getLocationInfo().getTimeOffset());

            Assert.assertEquals(getString(expectedResponse, PlatformPubliclightingKeys.KEY_STATUS),
                    response.getStatus().name());
        } else {
            Assert.assertEquals(getString(expectedResponse, PlatformPubliclightingKeys.MESSAGE), e.getMessage());
        }

    }

    public OslpEnvelope.Builder createEnvelopeBuilder(final String deviceUid, final Integer sequenceNumber) {
        final byte[] sequenceNumberBytes = new byte[2];
        sequenceNumberBytes[0] = (byte) (sequenceNumber >>> 8);
        sequenceNumberBytes[1] = sequenceNumber.byteValue();

        return new OslpEnvelope.Builder().withSignature(this.oslpMockServer.getOslpSignature())
                .withProvider(this.oslpMockServer.getOslpSignatureProvider())
                .withPrimaryKey(this.oslpMockServer.privateKey()).withDeviceId(Base64.decodeBase64(deviceUid))
                .withSequenceNumber(sequenceNumberBytes);
    }

    private OslpEnvelope send(final OslpEnvelope request, final Map<String, String> settings)
            throws IOException, DeviceSimulatorException {
        final String deviceIdentification = getString(settings, PlatformPubliclightingKeys.KEY_DEVICE_IDENTIFICATION);
        final String hostname = this.configuration.getPlatform();

        // See PlatformPubliclightingKeys.KEY_PROTOCOL and
        // PlatformPubliclightingDefaults.DEFAULT_PROTOCOL when using the
        // 'Protocol' key value pair in the settings.

        final InetSocketAddress address = new InetSocketAddress(hostname,
                PlatformPubliclightingDefaults.OSLP_ELSTER_SERVER_PORT);
        return this.oslpMockServer.send(address, request, deviceIdentification);
    }

    /**
     * Setup method to set the update firmware which should be returned by the
     * mock.
     */
    @Given("^the device returns a update firmware \"([^\"]*)\" over \"([^\"]*)\"$")
    public void theDeviceReturnsAUpdateFirmwareOverOSLP(final String result, final String protocol) {
        this.oslpMockServer.mockUpdateFirmwareResponse(Enum.valueOf(Status.class, result));
    }

    @Then("^an update firmware \"([^\"]*)\" message is sent to device \"([^\"]*)\"$")
    public void anUpdateFirmwareOSLPMessageIsSentToTheDevice(final String protocol, final String deviceIdentification,
            final Map<String, String> expectedParameters) throws UnknownHostException {
        final Message message = this.oslpMockServer.waitForRequest(MessageType.UPDATE_FIRMWARE);
        Assert.assertNotNull(message);
        Assert.assertTrue(message.hasUpdateFirmwareRequest());

        final UpdateFirmwareRequest request = message.getUpdateFirmwareRequest();

        // Check if the URL is equal to the file path as given by
        // 'firmware.path' property of OSGP.
        Assert.assertEquals(getString(expectedParameters, PlatformPubliclightingKeys.FIRMWARE_URL,
                PlatformPubliclightingDefaults.FIRMWARE_URL), request.getFirmwareUrl());
    }

    private String convertIpAddress(final ByteString byteString) {
        if (byteString == null || byteString.isEmpty()) {
            return "";
        }

        final StringBuilder stringBuilder = new StringBuilder();
        for (final byte number : byteString.toByteArray()) {
            int convertedNumber = number;
            if (number < 0) {
                convertedNumber = 256 + number;
            }
            final String str = String.valueOf(convertedNumber);
            stringBuilder.append(str).append(".");
        }
        final String ipValue = stringBuilder.toString();
        return ipValue.substring(0, ipValue.length() - 1);
    }
}
