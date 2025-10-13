/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.ShortDumpable;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

@Experimental
public interface ValueMetadata extends PrismContainer<Containerable>, ShortDumpable {

    ValueMetadata clone();

    default void addMetadataValue(PrismContainerValue<?> metadataValue) throws SchemaException {
        //noinspection unchecked
        add((PrismContainerValue) metadataValue);
    }
}
