package com.evolveum.midpoint.prism.impl.schemaContext;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;

/**
 * Created by Dominik.
 */
public class SchemaContextImpl implements SchemaContext {
    ItemDefinition<?> itemDefinition;

    public ItemDefinition<?> getItemDefinition() {
        return itemDefinition;
    }

    public void setItemDefinition(ItemDefinition<?> itemDefinition) {
        this.itemDefinition = itemDefinition;
    }
}
