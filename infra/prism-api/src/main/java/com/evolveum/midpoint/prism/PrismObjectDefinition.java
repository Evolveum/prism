/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

/**
 * TODO
 */
public interface PrismObjectDefinition<O extends Objectable>
        extends PrismContainerDefinition<O> {

    @Override
    @NotNull
    PrismObject<O> instantiate() throws SchemaException;

    @Override
    @NotNull
    PrismObject<O> instantiate(QName name) throws SchemaException;

    @NotNull
    PrismObjectDefinition<O> clone();

    @Override
    PrismObjectDefinition<O> deepClone(@NotNull DeepCloneOperation operation);

    @NotNull PrismObjectDefinition<O> cloneWithNewDefinition(QName newItemName, ItemDefinition<?> newDefinition);

    PrismContainerDefinition<?> getExtensionDefinition();

    @Override
    PrismObjectValue<O> createValue();

    @Override
    PrismObjectDefinitionMutator<O> mutator();

    interface PrismObjectDefinitionMutator<O extends Objectable>
            extends PrismContainerDefinitionMutator<O> {
    }
}
