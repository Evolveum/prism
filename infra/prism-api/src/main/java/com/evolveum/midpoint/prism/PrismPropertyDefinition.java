/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import java.util.Collection;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.util.DisplayableValue;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

import static com.evolveum.midpoint.util.MiscUtil.stateCheck;

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

    /**
     * Converts {@link String} value to {@link PolyString}. Regular properties use simple {@link PolyString#fromOrig(String)},
     * but custom normalization-aware properties (midPoint shadow attributes) can use custom strategies here.
     */
    default @NotNull T convertStringValueToPolyString(@NotNull String stringValue) throws SchemaException {
        var type = getTypeClass();
        stateCheck(PolyString.class.equals(type), "Unexpected type: %s, should be PolyString", type);
        //noinspection unchecked
        return (T) PolyString.fromOrig(stringValue);
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
