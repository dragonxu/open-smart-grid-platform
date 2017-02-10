/**
 * Copyright 2017 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.platform.dlms.cucumber.support.ws.smartmetering.installation;

import java.util.Map;

import com.alliander.osgp.adapter.ws.schema.smartmetering.installation.Device;
import com.alliander.osgp.platform.cucumber.core.Helpers;
import com.alliander.osgp.platform.cucumber.helpers.SettingsHelper;
import com.alliander.osgp.platform.cucumber.steps.Defaults;
import com.alliander.osgp.platform.cucumber.steps.Keys;
import com.alliander.osgp.platform.dlms.cucumber.support.ws.smartmetering.RequestBuilderHelper;

public class DeviceBuilder {

    private DeviceBuilder() {
        // Private constructor for utility class.
    }

    public static Device fromParameterMap(final Map<String, String> requestParameters) {

        final Map<String, String> settings = SettingsHelper.addDefault(requestParameters, Keys.KEY_DEVICE_DELIVERY_DATE,
                Defaults.DLMS_DEFAULT_DEVICE_DELIVERY_DATE);

        final Device device = new Device();
        device.setDeviceIdentification(
                Helpers.getString(settings, Keys.KEY_DEVICE_IDENTIFICATION,
                        Defaults.DEFAULT_DEVICE_IDENTIFICATION));
        device.setDeviceType(Helpers.getString(settings, Keys.KEY_DEVICE_TYPE, Defaults.DEFAULT_DEVICE_TYPE));

        device.setCommunicationMethod(settings.get(Keys.KEY_DEVICE_COMMUNICATIONMETHOD));
        device.setCommunicationProvider(settings.get(Keys.KEY_DEVICE_COMMUNICATIONPROVIDER));
        device.setICCId(settings.get(Keys.KEY_DEVICE_ICCID));

        device.setDSMRVersion(settings.get(Keys.KEY_DEVICE_DSMRVERSION));
        device.setSupplier(settings.get(Keys.KEY_DEVICE_SUPPLIER));

        device.setHLS3Active(Boolean.valueOf(
                Helpers.getBoolean(settings, Keys.KEY_DEVICE_HLS3ACTIVE, Defaults.DLMS_DEFAULT_HSL3_ACTIVE)));
        device.setHLS4Active(Boolean.valueOf(
                Helpers.getBoolean(settings, Keys.KEY_DEVICE_HLS4ACTIVE, Defaults.DLMS_DEFAULT_HSL4_ACTIVE)));
        device.setHLS5Active(Boolean.valueOf(
                Helpers.getBoolean(settings, Keys.KEY_DEVICE_HLS5ACTIVE, Defaults.DLMS_DEFAULT_HSL5_ACTIVE)));

        device.setMasterKey(
                RequestBuilderHelper.hexDecodeDeviceKey(settings.get(Keys.KEY_DEVICE_MASTERKEY),
                        Keys.KEY_DEVICE_MASTERKEY));
        device.setAuthenticationKey(
                RequestBuilderHelper.hexDecodeDeviceKey(settings.get(Keys.KEY_DEVICE_AUTHENTICATIONKEY),
                        Keys.KEY_DEVICE_AUTHENTICATIONKEY));
        device.setGlobalEncryptionUnicastKey(
                RequestBuilderHelper.hexDecodeDeviceKey(settings.get(Keys.KEY_DEVICE_ENCRYPTIONKEY),
                        Keys.KEY_DEVICE_ENCRYPTIONKEY));

        device.setDeliveryDate(
                SettingsHelper.getXmlGregorianCalendarValue(settings, Keys.KEY_DEVICE_DELIVERY_DATE));

        return device;
    }
}
