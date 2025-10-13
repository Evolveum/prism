/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.schemaContext;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;

/**
 * Created by Dominik.
 */
public class SchemaContextImpl implements SchemaContext {

    private ItemDefinition<?> itemDefinition;

    public SchemaContextImpl(ItemDefinition<?> itemDefinition) {
        this.itemDefinition = itemDefinition;
    }

    public ItemDefinition<?> getItemDefinition() {
        return itemDefinition;
    }
}
