/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.schemaContext;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;

import org.jetbrains.annotations.Nullable;

public class SchemaContextImpl implements SchemaContext {

    @Nullable private final ItemDefinition<?> itemDefinition;

    public SchemaContextImpl(@Nullable ItemDefinition<?> itemDefinition) {
        this.itemDefinition = itemDefinition;
    }

    public @Nullable ItemDefinition<?> getItemDefinition() {
        return itemDefinition;
    }
}
