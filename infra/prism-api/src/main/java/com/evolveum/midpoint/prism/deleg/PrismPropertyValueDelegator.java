/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.deleg;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.xnode.XNode;

import jakarta.xml.bind.JAXBElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PrismPropertyValueDelegator<T> extends PrismPropertyValue<T>, PrismValueDelegator {

    PrismPropertyValue<T> delegate();

    default void setValue(T value) {
        delegate().setValue(value);
    }

    default T getValue() {
        return delegate().getValue();
    }

    default XNode getRawElement() {
        return delegate().getRawElement();
    }

    default void setRawElement(XNode rawElement) {
        delegate().setRawElement(rawElement);
    }

    default @Nullable ExpressionWrapper getExpression() {
        return delegate().getExpression();
    }

    default void setExpression(@Nullable ExpressionWrapper expression) {
        delegate().setExpression(expression);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>> PartiallyResolvedItem<IV, ID> findPartial(ItemPath path) {
        return delegate().findPartial(path);
    }

    default PrismPropertyValue<T> clone() {
        return delegate().clone();
    }

    default PrismPropertyValue<T> cloneComplex(@NotNull CloneStrategy strategy) {
        return delegate().cloneComplex(strategy);
    }

    default boolean equals(PrismPropertyValue<?> other, @NotNull ParameterizedEquivalenceStrategy strategy, @Nullable MatchingRule<T> matchingRule) {
        return delegate().equals(other, strategy, matchingRule);
    }

    default String debugDump(int indent, boolean detailedDump) {
        return delegate().debugDump(indent, detailedDump);
    }

    default JAXBElement<T> toJaxbElement() {
        return delegate().toJaxbElement();
    }

    default Class<?> getRealClass() {
        return delegate().getRealClass();
    }

    default @Nullable T getRealValue() {
        return delegate().getRealValue();
    }

    default @NotNull T getRealValueRequired() {
        return delegate().getRealValueRequired();
    }

    public static <T1> T1 getRealValue(PrismPropertyValue<T1> propertyValue) {
        return PrismPropertyValue.getRealValue(propertyValue);
    }

    public static boolean isNotFalse(PrismPropertyValue<Boolean> booleanPropertyValue) {
        return PrismPropertyValue.isNotFalse(booleanPropertyValue);
    }

    public static boolean isTrue(PrismPropertyValue<Boolean> booleanPropertyValue) {
        return PrismPropertyValue.isTrue(booleanPropertyValue);
    }
}
