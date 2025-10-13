/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.metadata;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.ValueMetadata;
import com.evolveum.midpoint.util.annotation.Experimental;

import org.jetbrains.annotations.NotNull;

/**
 * Provides empty value metadata.
 */
@Experimental
public interface ValueMetadataFactory {

    @NotNull ValueMetadata createEmpty();

    default PrismContainerDefinition<?> getDefinition() {
        return createEmpty().getDefinition();
    }

}
