/**
 * Copyright 2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.cucumber.platform.smartmetering.glue.steps.ws.smartmetering.smartmeteringinstallation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.soap.client.SoapFaultClientException;

import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DeCoupleMbusDeviceAsyncRequest;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DeCoupleMbusDeviceAsyncResponse;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DeCoupleMbusDeviceRequest;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DeCoupleMbusDeviceResponse;
import org.opensmartgridplatform.cucumber.core.ScenarioContext;
import org.opensmartgridplatform.cucumber.platform.PlatformKeys;
import org.opensmartgridplatform.cucumber.platform.smartmetering.glue.steps.ws.smartmetering.AbstractSmartMeteringSteps;
import org.opensmartgridplatform.cucumber.platform.smartmetering.support.ws.smartmetering.installation.DeCoupleMbusDeviceRequestFactory;
import org.opensmartgridplatform.cucumber.platform.smartmetering.support.ws.smartmetering.installation.SmartMeteringInstallationClient;
import org.opensmartgridplatform.shared.exceptionhandling.WebServiceSecurityException;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class DeCoupleDeviceSteps extends AbstractSmartMeteringSteps {

    @Autowired
    private SmartMeteringInstallationClient smartMeteringInstallationClient;

    @When("^the DeCouple G-meter \"([^\"]*)\" from E-meter \"([^\"]*)\" request is received for an unknown gateway$")
    public void theDeCoupleGMeterFromEMeterRequestIsReceivedForAnUnknownDevice(final String gasMeter,
            final String eMeter) throws WebServiceSecurityException {

        final DeCoupleMbusDeviceRequest request = DeCoupleMbusDeviceRequestFactory.forGatewayAndMbusDevice(eMeter,
                gasMeter);

        try {
            this.smartMeteringInstallationClient.deCoupleMbusDevice(request);
            fail("A SoapFaultClientException should be thrown");
        } catch (final SoapFaultClientException e) {
            ScenarioContext.current().put(PlatformKeys.RESPONSE, e);
        }
    }

    @When("^the DeCouple G-meter \"([^\"]*)\" from E-meter \"([^\"]*)\" request is received for an inactive gateway$")
    public void theDeCoupleGMeterFromEMeterRequestIsReceivedForAnInactiveDevice(final String gasMeter,
            final String eMeter) throws WebServiceSecurityException {

        final DeCoupleMbusDeviceRequest request = DeCoupleMbusDeviceRequestFactory.forGatewayAndMbusDevice(eMeter,
                gasMeter);

        try {
            this.smartMeteringInstallationClient.deCoupleMbusDevice(request);
            fail("A SoapFaultClientException should be thrown");
        } catch (final SoapFaultClientException e) {
            ScenarioContext.current().put(PlatformKeys.RESPONSE, e);
        }
    }

    @When("^the DeCouple G-meter \"([^\"]*)\" from E-meter \"([^\"]*)\" request is received$")
    public void theDeCoupleGMeterRequestIsReceived(final String gasMeter, final String eMeter)
            throws WebServiceSecurityException {

        final DeCoupleMbusDeviceRequest request = DeCoupleMbusDeviceRequestFactory.forGatewayAndMbusDevice(eMeter,
                gasMeter);
        final DeCoupleMbusDeviceAsyncResponse asyncResponse = this.smartMeteringInstallationClient
                .deCoupleMbusDevice(request);

        this.checkAndSaveCorrelationId(asyncResponse.getCorrelationUid());
    }

    @Then("^the DeCouple response is \"([^\"]*)\"$")
    public void theDeCoupleResponseIs(final String status) throws WebServiceSecurityException {

        final DeCoupleMbusDeviceAsyncRequest deCoupleMbusDeviceAsyncRequest = DeCoupleMbusDeviceRequestFactory
                .fromScenarioContext();
        final DeCoupleMbusDeviceResponse response = this.smartMeteringInstallationClient
                .getDeCoupleMbusDeviceResponse(deCoupleMbusDeviceAsyncRequest);

        assertNotNull("Result", response.getResult());
        assertEquals("Result", status, response.getResult().name());
    }

    @Then("^the DeCouple response is \"([^\"]*)\" and contains$")
    public void theDeCoupleResponseIsAndContains(final String status, final List<String> resultList)
            throws WebServiceSecurityException {

        final DeCoupleMbusDeviceAsyncRequest deCoupleMbusDeviceAsyncRequest = DeCoupleMbusDeviceRequestFactory
                .fromScenarioContext();
        final DeCoupleMbusDeviceResponse response = this.smartMeteringInstallationClient
                .getDeCoupleMbusDeviceResponse(deCoupleMbusDeviceAsyncRequest);

        assertNotNull("Result", response.getResult());
        assertEquals("Result", status, response.getResult().name());
        assertTrue("Description should contain all of " + resultList,
                this.checkDescription(response.getDescription(), resultList));
    }

    @Then("^retrieving the DeCouple response results in an exception$")
    public void retrievingTheDeCoupleResponseResultsInAnException() throws WebServiceSecurityException {

        final DeCoupleMbusDeviceAsyncRequest asyncRequest = DeCoupleMbusDeviceRequestFactory.fromScenarioContext();

        try {
            this.smartMeteringInstallationClient.getDeCoupleMbusDeviceResponse(asyncRequest);
            fail("A SoapFaultClientException should be thrown");
        } catch (final SoapFaultClientException e) {
            ScenarioContext.current().put(PlatformKeys.RESPONSE, e);
        }
    }
}
