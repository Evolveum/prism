package com.evolveum.midpoint.prism.impl.schemaContext;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;

/**
 * Created by Dominik.
 */
public class SchemaContextImpl implements SchemaContext {
    ItemDefinition<?> itemDefinition;

    public SchemaContextImpl(ItemDefinition<?> itemDefinition) {
        this.itemDefinition = itemDefinition;
    }

    public ItemDefinition<?> getItemDefinition() {
        return itemDefinition;
    }
}
