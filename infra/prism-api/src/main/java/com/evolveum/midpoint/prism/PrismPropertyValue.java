/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.util.CloneUtil;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.DebugDumpable;

import com.evolveum.midpoint.util.MiscUtil;

import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.xml.bind.JAXBElement;
import java.io.Serializable;

import static com.evolveum.midpoint.prism.CloneStrategy.LITERAL_ANY;
import static com.evolveum.midpoint.prism.CloneStrategy.LITERAL_MUTABLE;

/**
 * @author lazyman
 */
public interface PrismPropertyValue<T> extends DebugDumpable, Serializable, PrismValue {

    void setValue(T value);

    T getValue();

    XNode getRawElement();

    void setRawElement(XNode rawElement);

    @Nullable
    ExpressionWrapper getExpression();

    void setExpression(@Nullable ExpressionWrapper expression);

    <IV extends PrismValue,ID extends ItemDefinition<?>> PartiallyResolvedItem<IV,ID> findPartial(ItemPath path);

    PrismPropertyValue<T> clone();

    @Override
    PrismPropertyValue<T> cloneComplex(@NotNull CloneStrategy strategy);

    default PrismPropertyValue<T> copy() {
        return cloneComplex(LITERAL_ANY);
    }

    default PrismPropertyValue<T> mutableCopy() {
        return cloneComplex(LITERAL_MUTABLE);
    }

    default PrismPropertyValue<T> immutableCopy() {
        return CloneUtil.immutableCopy(this);
    }

    /**
     * @return true if values are equivalent under given strategy and (if present) also under matching rule.
     * Some of the strategy requirements (e.g. literal DOM comparison) can be skipped if matching rule is used.
     */
    boolean equals(PrismPropertyValue<?> other, @NotNull ParameterizedEquivalenceStrategy strategy, @Nullable MatchingRule<T> matchingRule);

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    String debugDump(int indent, boolean detailedDump);

    /**
     * Returns JAXBElement corresponding to the this value.
     * Name of the element is the name of parent property; its value is the real value of the property.
     *
     * @return Created JAXBElement.
     */
    JAXBElement<T> toJaxbElement();

    @Override
    Class<?> getRealClass();

    /**
     * Under what circumstances can this method return null (for properties)?
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    T getRealValue();

    default @NotNull T getRealValueRequired() {
        return MiscUtil.stateNonNull(getRealValue(), "No real value in %s", this);
    }

    static <T> T getRealValue(PrismPropertyValue<T> propertyValue) {
        return propertyValue != null ? propertyValue.getRealValue() : null;
    }

    static boolean isNotFalse(PrismPropertyValue<Boolean> booleanPropertyValue) {
        return booleanPropertyValue == null || BooleanUtils.isNotFalse(booleanPropertyValue.getRealValue());
    }

    static boolean isTrue(PrismPropertyValue<Boolean> booleanPropertyValue) {
        return booleanPropertyValue != null && BooleanUtils.isTrue(booleanPropertyValue.getRealValue());
    }

}
