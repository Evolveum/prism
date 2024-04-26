/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.AbstractFreezable;
import com.evolveum.midpoint.prism.Freezable;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.schema.SchemaRegistryState;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Class allow before freeze setting of SchemaRegistryStateImpl
 * and use it for resolving of definitions during initializing of schema.
 */
public abstract class FreezableInitializable extends AbstractFreezable {

    /**
     * Current schemaRegistryState. This variable is present only if this state(also this prismSchema) isn't fully initialized.
     */
    private SchemaRegistryStateImpl schemaRegistryState;

    @Override
    protected void performFreeze() {
        super.performFreeze();
        schemaRegistryState = null;
    }

    /**
     * Using during initialization of schemas when schemaRegistryState non exist in SchemaRegistry.
     * Can be call only if class is mutable.
     */
    public void setSchemaRegistryState(SchemaRegistryStateImpl schemaRegistryState) {
        if (isImmutable()) {
            throw new UnsupportedOperationException("TypeDefinitionImpl is freeze.");
        }
        this.schemaRegistryState = schemaRegistryState;
    }

    /**
     * Return schemaRegistryState that will be used for resolving of schema.
     * This method is used to move the variable to another class.
     * For resolving of definition use {@link #getSchemaResolver()}.
     */
    @Nullable
    protected final SchemaRegistryStateImpl getSchemaRegistryState() {
        return schemaRegistryState;
    }

    /**
     * @return SchemaRegistryState that can be used for resolving of schema
     */
    protected final SchemaRegistryState getSchemaResolver() {
        return Objects.requireNonNullElseGet(schemaRegistryState, () -> PrismContext.get().getSchemaRegistry());
    }
}
