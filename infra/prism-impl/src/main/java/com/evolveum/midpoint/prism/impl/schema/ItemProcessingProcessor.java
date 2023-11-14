/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.ItemProcessing;
import com.evolveum.midpoint.prism.MutableItemDefinition;
import com.evolveum.midpoint.prism.PrismConstants;

import org.jetbrains.annotations.NotNull;

/**
 * Processor for {@link com.evolveum.midpoint.prism.PrismConstants#A_PROCESSING} annotation with {@link ItemProcessing} values.
 */
public class ItemProcessingProcessor extends EnumAnnotationProcessor<MutableItemDefinition<?>, ItemProcessing> {

    public ItemProcessingProcessor() {
        super(PrismConstants.A_PROCESSING, ItemProcessing.class, MutableItemDefinition.class, MutableItemDefinition::setProcessing);
    }

    @Override
    protected boolean equals(@NotNull ItemProcessing itemProcessing, @NotNull String value) {
        return itemProcessing.getValue().equals(value);
    }
}
