/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.util.DisplayableValue;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Definition of a prism property.
 */
public interface PrismPropertyDefinition<T>
        extends ItemDefinition<PrismProperty<T>>,
        PrismItemMatchingDefinition<T>,
        PrismItemValuesDefinition<T>,
        PrismItemInstantiableDefinition<T, PrismPropertyValue<T>, PrismProperty<T>, PrismPropertyDefinition<T>, PropertyDelta<T>> {

    @Override
    default Class<T> getTypeClass() {
        return PrismItemMatchingDefinition.super.getTypeClass();
    }

    default boolean isAnyType() {
        return DOMUtil.XSD_ANYTYPE.equals(getTypeName());
    }

    @Override
    @NotNull PrismPropertyDefinition<T> clone();

    /**
     * This is the original implementation. Moving to more comprehensive one (as part of MID-2119 implementation) broke some
     * things especially in the prism query language parser, so we temporarily provide the legacy (partial) implementation here.
     *
     * The difference is that the partial implementation covers only basic types.
     * The comprehensive one covers also complex types via {@link SchemaRegistry#determineJavaClassForType(QName)}.
     */
    default Class<T> getTypeClassLegacy() {
        return XsdTypeMapper.toJavaTypeIfKnown(getTypeName());
    }

    @Override
    PrismPropertyDefinitionMutator<T> mutator();

    /** TEMPORARY! Used only for normalization-aware resource attribute storage. FIXME as part of MID-2119. */
    default @NotNull List<T> adoptRealValues(@NotNull Collection<?> realValues) throws SchemaException {
        throw new UnsupportedOperationException(); // FIXME
    }

    interface PrismPropertyDefinitionMutator<T>
            extends
            ItemDefinitionMutator,
            PrismItemMatchingDefinition.Mutator,
            PrismItemValuesDefinition.Mutator<T> {

        PrismPropertyDefinitionMutator<T> clone();
    }

    interface PrismPropertyLikeDefinitionBuilder<T>
            extends PrismPropertyDefinitionMutator<T>,
            ItemDefinitionLikeBuilder {

        void setAllowedValues(Collection<? extends DisplayableValue<T>> displayableValues);

    }
}
