/**
 * Copyright 2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.dto.valueobjects.smartmetering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccessSelectorListDto implements Serializable {
    private static final long serialVersionUID = 6844148787752579231L;

    protected final List<Integer> accessSelector;

    public AccessSelectorListDto(final List<Integer> accessSelector) {
        this.accessSelector = Collections.unmodifiableList(accessSelector);
    }

    public List<Integer> getAccessSelector() {
        return new ArrayList<>(this.accessSelector);
    }
}
