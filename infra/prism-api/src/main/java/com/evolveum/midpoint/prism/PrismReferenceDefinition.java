/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.annotation.Experimental;

import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

public interface PrismReferenceDefinition extends ItemDefinition<PrismReference> {

    QName getTargetTypeName();

    /**
     * Returns a definition applicable to the target of this reference.
     *
     * Introduced to support shadow association definitions in midPoint:
     * it applies to `ShadowAssociationValueType.shadowRef`.
     */
    @Experimental
    default @Nullable PrismObjectDefinition<?> getTargetObjectDefinition() {
        return null;
    }

    boolean isComposite();

    @NotNull
    @Override
    PrismReference instantiate();

    @NotNull
    @Override
    PrismReference instantiate(QName name);

    @NotNull
    @Override
    PrismReferenceDefinition clone();

    PrismReferenceDefinitionMutator mutator();

    default @NotNull PrismReferenceValue migrateIfNeeded(@NotNull PrismReferenceValue value) throws SchemaException {
        return value;
    }

    interface PrismReferenceDefinitionMutator extends ItemDefinitionMutator {

        void setTargetTypeName(QName typeName);
        void setTargetObjectDefinition(PrismObjectDefinition<?> definition);
        void setComposite(boolean value);
    }

    interface PrismReferenceDefinitionBuilder
            extends ItemDefinitionLikeBuilder, PrismReferenceDefinitionMutator {
    }
}
