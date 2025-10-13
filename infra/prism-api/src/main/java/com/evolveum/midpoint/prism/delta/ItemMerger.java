/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.delta;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.key.NaturalKeyDefinition;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.SchemaException;

public interface ItemMerger {

    NaturalKeyDefinition getNaturalKey();

    /**
     * Merges all data about specific item (named `itemName`) from `source` container value to `target` one, according
     * to (its own) strategy.
     *
     * So, `source` is not modified; the `target` is.
     *
     * The implementation is responsible for setting origin information on all prism values copied from `source` to `target`.
     */
    void merge(@NotNull ItemName itemName, @NotNull PrismContainerValue<?> target, @NotNull PrismContainerValue<?> source)
            throws ConfigurationException, SchemaException;
}
