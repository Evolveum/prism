/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.ItemProcessing;

import org.jetbrains.annotations.NotNull;

/**
 * TODO DOC
 */
public class ItemProcessingConverter extends EnumAnnotationConverter<ItemProcessing> {

    public ItemProcessingConverter() {
        super(ItemProcessing.class);
    }

    @Override
    protected boolean equals(@NotNull ItemProcessing itemProcessing, @NotNull String value) {
        return itemProcessing.getValue().equals(value);
    }
}
