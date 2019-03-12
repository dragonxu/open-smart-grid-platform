/**
 * Copyright 2018 Smart Society Services B.V.
 */
package org.opensmartgridplatform.adapter.protocol.dlms.domain.entities;

/**
 * Default value that is either set or not set, for use by builder classes
 * (which usually generate values on the fly when no default is set).
 */
// TODO (RvM): move this to place where tests in all modules can access it.
public class DefaultValue<T> {
    private enum Status {SET, NOT_SET}

    private final DefaultValue.Status status;
    private final T value;

    private DefaultValue(final DefaultValue.Status status, final T value) {
        this.status = status;
        this.value = value;
    }

    public static <T> DefaultValue<T> notSet() {
        return new DefaultValue<>(Status.NOT_SET, null);
    }

    public static <T> DefaultValue<T> setTo(final T value) {
        return new DefaultValue<>(Status.SET, value);
    }

    /** Returns default value when set, or the given value when default value is not set. */
    public T orElse(final T elseValue) {
        if (this.status == Status.SET) {
            return this.value;
        }
        return elseValue;
    }
}
