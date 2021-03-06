/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package org.opensmartgridplatform.adapter.domain.smartmetering.application.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import org.opensmartgridplatform.adapter.domain.smartmetering.application.mapping.MonitoringMapper;
import org.opensmartgridplatform.domain.core.valueobjects.smartmetering.ReadAlarmRegisterRequest;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ReadAlarmRegisterRequestDto;

public class ReadAlarmRegisterRequestMappingTest {

    private MonitoringMapper monitoringMapper = new MonitoringMapper();

    // Test if mapping a ReadAlarmRegisterRequest succeeds when it's String is
    // non-null
    @Test
    public void testReadAlarmRegisterRequestMappingTest() {
        // build test data
        final String deviceId = "device1";
        final ReadAlarmRegisterRequest request = new ReadAlarmRegisterRequest(deviceId);
        // actual mapping
        final ReadAlarmRegisterRequestDto requestDto = this.monitoringMapper.map(request,
                ReadAlarmRegisterRequestDto.class);
        // test mapping
        assertNotNull(requestDto);
        assertEquals(deviceId, requestDto.getDeviceIdentification());
    }

    // Test if mapping a ReadAlarmRegisterRequest succeeds when it's String is
    // null
    @Test
    public void testWithNullString() {
        // build test data
        final String deviceId = null;
        final ReadAlarmRegisterRequest request = new ReadAlarmRegisterRequest(deviceId);
        // actual mapping
        final ReadAlarmRegisterRequestDto requestDto = this.monitoringMapper.map(request,
                ReadAlarmRegisterRequestDto.class);
        // test mapping
        assertNotNull(requestDto);
        assertNull(requestDto.getDeviceIdentification());
    }

}
