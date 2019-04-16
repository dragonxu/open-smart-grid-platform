/**
 * Copyright 2019 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.domain.core.application.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensmartgridplatform.logging.domain.entities.DeviceLogItem;
import org.opensmartgridplatform.logging.domain.repositories.DeviceLogItemSlicingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

public class TransactionalDeviceLogItemServiceTest {

    private final Date date = DateTime.now().toDate();

    @InjectMocks
    private TransactionalDeviceLogItemService transactionalDeviceLogItemService;

    @Mock
    private DeviceLogItemSlicingRepository deviceLogItemSlicingRepository;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void serviceReturnsOneDeviceLogItem() {
        final Slice<DeviceLogItem> mockSlice = this.mockSliceOfDeviceLogItems(1);
        final PageRequest pageRequest = new PageRequest(0, 1);
        Mockito.when(this.deviceLogItemSlicingRepository.findByCreationTimeBefore(this.date, pageRequest))
                .thenReturn(mockSlice);

        final List<DeviceLogItem> deviceLogItems = this.transactionalDeviceLogItemService
                .findDeviceLogItemsBeforeDate(this.date, 1);
        assertThat(deviceLogItems.isEmpty()).isFalse();
        assertThat(deviceLogItems.size()).isEqualTo(1);
    }

    @Test
    public void serviceReturnsTenDeviceLogItem() {
        final Slice<DeviceLogItem> mockSlice = this.mockSliceOfDeviceLogItems(10);
        final PageRequest pageRequest = new PageRequest(0, 10);
        Mockito.when(this.deviceLogItemSlicingRepository.findByCreationTimeBefore(this.date, pageRequest))
                .thenReturn(mockSlice);

        final List<DeviceLogItem> deviceLogItems = this.transactionalDeviceLogItemService
                .findDeviceLogItemsBeforeDate(this.date, 10);
        assertThat(deviceLogItems.isEmpty()).isFalse();
        assertThat(deviceLogItems.size()).isEqualTo(10);
    }

    @Test
    public void serviceDeletesDeviceLogItem() {
        final List<DeviceLogItem> deviceLogItems = this.mockSliceOfDeviceLogItems(1).getContent();

        try {
            this.transactionalDeviceLogItemService.deleteDeviceLogItems(deviceLogItems);
        } catch (final Exception e) {
            assertThat(e).isNull();
        }
    }

    private Slice<DeviceLogItem> mockSliceOfDeviceLogItems(final int numberOfDeviceLogItems) {

        final List<DeviceLogItem> deviceLogItems = new ArrayList<>();
        for (int i = 0; i < numberOfDeviceLogItems; i++) {
            final DeviceLogItem.Builder builder = new DeviceLogItem.Builder().withIncoming(false)
                    .withDeviceUid("deviceUID").withEncodedMessage("0x48 0x49").withDecodedMessage("H I")
                    .withDeviceIdentification("test").withOrganisationIdentification("organisation").withValid(true)
                    .withPayloadMessageSerializedSize(2);
            final DeviceLogItem deviceLogItem = new DeviceLogItem(builder);
            deviceLogItems.add(deviceLogItem);
        }

        return new SliceImpl<>(deviceLogItems);
    }
}
