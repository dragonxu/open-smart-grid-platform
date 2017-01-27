/**
 * Copyright 2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.automatictests.platform.glue.steps.database.core;

import static com.alliander.osgp.automatictests.platform.core.Helpers.getString;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alliander.osgp.adapter.ws.shared.db.domain.repositories.writable.WritableDeviceFirmwareRepository;
import com.alliander.osgp.adapter.ws.shared.db.domain.repositories.writable.WritableFirmwareRepository;
import com.alliander.osgp.automatictests.platform.Defaults;
import com.alliander.osgp.automatictests.platform.Keys;
import com.alliander.osgp.automatictests.platform.StepsBase;
import com.alliander.osgp.domain.core.entities.Device;
import com.alliander.osgp.domain.core.entities.DeviceFirmware;
import com.alliander.osgp.domain.core.entities.Firmware;
import com.alliander.osgp.domain.core.repositories.DeviceRepository;

import cucumber.api.java.en.Given;

public class DeviceFirmwareSteps extends StepsBase {
    
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private WritableDeviceFirmwareRepository deviceFirmwareRepository;

    @Autowired
    private WritableFirmwareRepository firmwareRepository;

    /**
     * Generic method which adds a device firmware using the settings.
     *
     * @param settings
     *            The settings for the device to be used.
     * @throws Throwable
     */
    @Given("^a device firmware$")
    public void aDeviceFirmware(final Map<String, String> settings) throws Throwable {

        // Get the device
        final Device device = this.deviceRepository.findByDeviceIdentification(
                getString(settings, Keys.DEVICE_IDENTIFICATION, Defaults.DEVICE_IDENTIFICATION));

        // TODO for now take the last
        final List<Firmware> fws = this.firmwareRepository.findAll();
        final Firmware firmware = fws.get(fws.size() - 1);

        // Now create the device firmware.
        final DeviceFirmware deviceFirmware = new DeviceFirmware();

        deviceFirmware.setDevice(device);
        deviceFirmware.setFirmware(firmware);

        this.deviceFirmwareRepository.save(deviceFirmware);
    }
}
