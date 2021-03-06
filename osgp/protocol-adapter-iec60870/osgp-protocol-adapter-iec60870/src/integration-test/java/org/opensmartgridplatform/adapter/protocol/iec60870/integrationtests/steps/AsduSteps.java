/**
 * Copyright 2019 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.protocol.iec60870.integrationtests.steps;

import org.openmuc.j60870.ASdu;
import org.openmuc.j60870.TypeId;
import org.opensmartgridplatform.adapter.protocol.iec60870.domain.factories.AsduFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.When;

public class AsduSteps {

    @Autowired
    private ConnectionSteps connectionSteps;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionSteps.class);

    @When("I receive an ASDU of type {string} from the IEC60870 device")
    public void whenIReceiveAsduOfType(final String typeId) {
        LOGGER.debug("When I receive an ASDU of type {}", typeId);

        final ASdu asdu = AsduFactory.ofType(TypeId.valueOf(typeId));
        this.connectionSteps.getConnectionEventListener().newASdu(asdu);
    }
}
