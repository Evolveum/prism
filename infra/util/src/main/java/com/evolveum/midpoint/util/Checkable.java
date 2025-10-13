/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util;

import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

@Experimental
public interface Checkable {

    void checkConsistence() throws SchemaException;

    /** To throw unchecked exception in case of consistency check failure. */
    default IllegalStateException checkFailedException(Throwable e) {
        throw new IllegalStateException("Consistency check failed for " + this + ": " + e.getMessage(), e);
    }
}
