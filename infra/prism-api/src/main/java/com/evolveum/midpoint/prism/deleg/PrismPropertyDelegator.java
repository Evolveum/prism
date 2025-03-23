/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.deleg;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemPath;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface PrismPropertyDelegator<T> extends PrismProperty<T>, ItemDelegator<PrismPropertyValue<T>, PrismPropertyDefinition<T>>  {

    PrismProperty<T> delegate();

    default <X> List<PrismPropertyValue<X>> getValues(Class<X> type) {
        return delegate().getValues(type);
    }

    @Override
    default @NotNull Collection<T> getRealValues() {
        return delegate().getRealValues();
    }

    @Override
    default @NotNull <X> Collection<X> getRealValues(Class<X> type) {
        return delegate().getRealValues(type);
    }

    @Override
    default T getAnyRealValue() {
        return delegate().getAnyRealValue();
    }

    @Override
    default T getRealValue() {
        return delegate().getRealValue();
    }

    @Override
    default <X> PrismPropertyValue<X> getValue(Class<X> type) {
        return delegate().getValue(type);
    }

    @Override
    default void setValue(PrismPropertyValue<T> value) {
        delegate().setValue(value);
    }

    @Override
    default void setRealValue(T realValue) {
        delegate().setRealValue(realValue);
    }

    @Override
    default void setRealValues(T... realValues) {
        delegate().setRealValues(realValues);
    }

    @Override
    default void addValues(Collection<PrismPropertyValue<T>> pValuesToAdd) {
        delegate().addValues(pValuesToAdd);
    }

    @Override
    default void addValue(PrismPropertyValue<T> pValueToAdd) {
        delegate().addValue(pValueToAdd);
    }

    @Override
    default void addRealValue(T valueToAdd) {
        delegate().addRealValue(valueToAdd);
    }

    @Override
    default void addRealValueSkipUniquenessCheck(T valueToAdd) {
        delegate().addRealValueSkipUniquenessCheck(valueToAdd);
    }

    @Override
    default void addRealValues(T... valuesToAdd) {
        delegate().addRealValues(valuesToAdd);
    }

    @Override
    default boolean deleteValues(Collection<PrismPropertyValue<T>> pValuesToDelete) {
        return delegate().deleteValues(pValuesToDelete);
    }

    @Override
    default boolean deleteValue(PrismPropertyValue<T> pValueToDelete) {
        return delegate().deleteValue(pValueToDelete);
    }

    @Override
    default void replaceValues(Collection<PrismPropertyValue<T>> valuesToReplace) {
        delegate().replaceValues(valuesToReplace);
    }

    @Override
    default boolean hasRealValue(PrismPropertyValue<T> value) {
        return delegate().hasRealValue(value);
    }

    @Override
    default Class<T> getValueClass() {
        return delegate().getValueClass();
    }

    @Override
    default PropertyDelta<T> createDelta() {
        return delegate().createDelta();
    }

    @Override
    default PropertyDelta<T> createDelta(ItemPath path) {
        return delegate().createDelta(path);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> PartiallyResolvedItem<IV, ID> findPartial(ItemPath path) {
        return delegate().findPartial(path);
    }

    @Override
    default PropertyDelta<T> diff(PrismProperty<T> other) {
        return delegate().diff(other);
    }

    @Override
    default PropertyDelta<T> diff(PrismProperty<T> other, ParameterizedEquivalenceStrategy strategy) {
        return delegate().diff(other, strategy);
    }

    @Deprecated // use immutableCopy()
    @Override
    default PrismProperty<T> createImmutableClone() {
        return delegate().createImmutableClone();
    }

    @Deprecated // use copy()
    @Override
    default PrismProperty<T> clone() {
        return delegate().clone();
    }

    @Override
    default @NotNull PrismProperty<T> cloneComplex(@NotNull CloneStrategy strategy) {
        return delegate().cloneComplex(strategy);
    }

    @Override
    default String toHumanReadableString() {
        return delegate().toHumanReadableString();
    }

}
