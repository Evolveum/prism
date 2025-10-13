/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.AbstractFreezable;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.schema.SchemaLookup;

import org.jetbrains.annotations.Nullable;

/**
 * Class allow before freeze setting of SchemaRegistryStateImpl
 * and use it for resolving of definitions during initializing of schema.
 */
public abstract class SchemaRegistryStateAware extends AbstractFreezable implements SchemaLookup.Aware {

    /**
     * Current schemaRegistryState. This variable is present only if this state(also this prismSchema) isn't fully initialized.
     */
    private SchemaLookup schemaLookup;

    @Override
    protected void performFreeze() {
        super.performFreeze();
        //schemaRegistryState = null;
    }

    /**
     * Using during initialization of schemas when schemaRegistryState non exist in SchemaRegistry.
     * Can be call only if class is mutable.
     */
    public void setSchemaLookup(SchemaLookup schemaLookup) {
        if (isImmutable()) {
            throw new UnsupportedOperationException("TypeDefinitionImpl is freeze.");
        }
        this.schemaLookup = schemaLookup;
    }

    /**
     * Return schemaRegistryState that will be used for resolving of schema.
     * This method is used to move the variable to another class.
     * For resolving of definition use {@link #schemaLookup()}.
     *
     * @return SchemaRegistryState that can be used for resolving of schema
     */
    @Override
    public SchemaLookup schemaLookup() {
        if (schemaLookup == null) {
            return SchemaLookup.Aware.super.schemaLookup();
        }
        return schemaLookup;
    }
}
