/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.delta;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;

/**
 * @author semancik
 *
 */
public interface ReferenceDelta extends ItemDelta<PrismReferenceValue,PrismReferenceDefinition> {

    @Override
    Class<PrismReference> getItemClass();

    @Override
    void setDefinition(@NotNull PrismReferenceDefinition definition);

    @Override
    void applyDefinition(@NotNull PrismReferenceDefinition definition) throws SchemaException;

    boolean isApplicableToType(Item item);

    @Override
    ReferenceDelta clone();

}
