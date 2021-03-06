/**
 * Copyright 2017 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.dto.valueobjects;

import java.io.Serializable;

public class FirmwareFileDto implements Serializable {

    private static final long serialVersionUID = -4794626243032507358L;

    private String firmwareIdentification;
    private byte[] firmwareFile;

    public FirmwareFileDto(final String firmwareIdentification, final byte[] firmwareFile) {
        this.firmwareIdentification = firmwareIdentification;
        this.firmwareFile = firmwareFile;
    }

    public String getFirmwareIdentification() {
        return this.firmwareIdentification;
    }

    public byte[] getFirmwareFile() {
        if (this.firmwareFile == null) {
            return new byte[] {};
        }
        return this.firmwareFile.clone();
    }
}
