/**
 * Copyright 2018 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.cucumber.platform.smartmetering.support.ws.smartmetering.management;

import java.util.Map;

import com.alliander.osgp.adapter.ws.schema.smartmetering.management.DeviceLifecycleStatus;
import com.alliander.osgp.adapter.ws.schema.smartmetering.management.SetDeviceLifecycleStatusByChannelAsyncRequest;
import com.alliander.osgp.adapter.ws.schema.smartmetering.management.SetDeviceLifecycleStatusByChannelRequest;
import com.alliander.osgp.adapter.ws.schema.smartmetering.management.SetDeviceLifecycleStatusByChannelRequestData;
import com.alliander.osgp.cucumber.platform.smartmetering.PlatformSmartmeteringKeys;
import com.alliander.osgp.cucumber.platform.smartmetering.support.ws.smartmetering.RequestFactoryHelper;

public class SetDeviceLifecycleStatusByChannelRequestFactory {

    private SetDeviceLifecycleStatusByChannelRequestFactory() {
        // Private constructor for utility class
    }

    public static SetDeviceLifecycleStatusByChannelRequest fromParameterMap(final Map<String, String> settings) {
        final SetDeviceLifecycleStatusByChannelRequest request = new SetDeviceLifecycleStatusByChannelRequest();
        final SetDeviceLifecycleStatusByChannelRequestData requestData = new SetDeviceLifecycleStatusByChannelRequestData();
        request.setGatewayDeviceIdentification(settings.get(PlatformSmartmeteringKeys.KEY_DEVICE_IDENTIFICATION));
        requestData.setChannel(Short.parseShort(settings.get(PlatformSmartmeteringKeys.KEY_CHANNEL)));
        requestData.setDeviceLifecycleStatus(
                DeviceLifecycleStatus.valueOf(settings.get(PlatformSmartmeteringKeys.KEY_DEVICE_LIFECYCLE_STATUS)));
        request.setSetDeviceLifecycleStatusByChannelRequestData(requestData);
        return request;
    }

    public static SetDeviceLifecycleStatusByChannelAsyncRequest fromScenarioContext() {
        final SetDeviceLifecycleStatusByChannelAsyncRequest asyncRequest = new SetDeviceLifecycleStatusByChannelAsyncRequest();
        asyncRequest.setCorrelationUid(RequestFactoryHelper.getCorrelationUidFromScenarioContext());
        asyncRequest.setDeviceIdentification(RequestFactoryHelper.getDeviceIdentificationFromScenarioContext());

        return asyncRequest;
    }

}
