/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.DisplayableValue;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

public interface PrismItemValuesDefinition<T> {

    /**
     * Returns allowed values for this property.
     */
    @Nullable Collection<? extends DisplayableValue<T>> getAllowedValues();

    /**
     * Returns suggested values for this property.
     */
    @Nullable Collection<? extends DisplayableValue<T>> getSuggestedValues();

    @Nullable T defaultValue();

    @Nullable PrismReferenceValue getValueEnumerationRef();

    interface Delegable<T> extends PrismItemValuesDefinition<T> {

        PrismItemValuesDefinition<T> prismItemValuesDefinition();

        @Override
        default @Nullable Collection<? extends DisplayableValue<T>> getAllowedValues() {
            return prismItemValuesDefinition().getAllowedValues();
        }

        @Override
        default @Nullable Collection<? extends DisplayableValue<T>> getSuggestedValues() {
            return prismItemValuesDefinition().getSuggestedValues();
        }

        @Override
        default @Nullable T defaultValue() {
            return prismItemValuesDefinition().defaultValue();
        }

        @Override
        default @Nullable PrismReferenceValue getValueEnumerationRef() {
            return prismItemValuesDefinition().getValueEnumerationRef();
        }
    }

    interface Mutator<T> {

        void setAllowedValues(Collection<? extends DisplayableValue<T>> values);
        void setSuggestedValues(Collection<? extends DisplayableValue<T>> values);
        void setDefaultValue(T value);
        void setValueEnumerationRef(PrismReferenceValue prismReferenceValue);

        interface Delegable<T> extends Mutator<T> {

            Mutator<T> prismItemValuesDefinition();

            @Override
            default void setAllowedValues(Collection<? extends DisplayableValue<T>> values) {
                prismItemValuesDefinition().setAllowedValues(values);
            }

            @Override
            default void setSuggestedValues(Collection<? extends DisplayableValue<T>> values) {
                prismItemValuesDefinition().setSuggestedValues(values);
            }

            @Override
            default void setDefaultValue(T value) {
                prismItemValuesDefinition().setDefaultValue(value);
            }

            @Override
            default void setValueEnumerationRef(PrismReferenceValue prismReferenceValue) {
                prismItemValuesDefinition().setValueEnumerationRef(prismReferenceValue);
            }
        }
    }

    class Data<T>
            extends AbstractFreezable
            implements PrismItemValuesDefinition<T>, Mutator<T>, Serializable {

        @Nullable private Collection<? extends DisplayableValue<T>> allowedValues;
        @Nullable private Collection<? extends DisplayableValue<T>> suggestedValues;
        @Nullable private T defaultValue;
        @Nullable private PrismReferenceValue valueEnumerationRef;

        @Override
        public @Nullable Collection<? extends DisplayableValue<T>> getAllowedValues() {
            return allowedValues;
        }

        @Override
        public void setAllowedValues(@Nullable Collection<? extends DisplayableValue<T>> allowedValues) {
            checkMutable();
            this.allowedValues = allowedValues;
        }

        @Override
        public @Nullable Collection<? extends DisplayableValue<T>> getSuggestedValues() {
            return suggestedValues;
        }

        @Override
        public void setSuggestedValues(@Nullable Collection<? extends DisplayableValue<T>> suggestedValues) {
            checkMutable();
            this.suggestedValues = suggestedValues;
        }

        @Override
        public @Nullable T defaultValue() {
            return defaultValue;
        }

        @Override
        public void setDefaultValue(@Nullable T defaultValue) {
            checkMutable();
            this.defaultValue = defaultValue;
        }

        public @Nullable PrismReferenceValue getValueEnumerationRef() {
            return valueEnumerationRef;
        }

        @Override
        public void setValueEnumerationRef(@Nullable PrismReferenceValue valueEnumerationRef) {
            checkMutable();
            this.valueEnumerationRef = valueEnumerationRef;
        }

        public void copyFrom(PrismItemValuesDefinition<T> source) {
            checkMutable();
            allowedValues = source.getAllowedValues();
            suggestedValues = source.getSuggestedValues();
            defaultValue = source.defaultValue();
            valueEnumerationRef = source.getValueEnumerationRef();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Data<?> data = (Data<?>) o;
            return Objects.equals(allowedValues, data.allowedValues)
                    && Objects.equals(suggestedValues, data.suggestedValues)
                    && Objects.equals(defaultValue, data.defaultValue)
                    && Objects.equals(valueEnumerationRef, data.valueEnumerationRef);
        }

        @Override
        public int hashCode() {
            return Objects.hash(allowedValues, suggestedValues, defaultValue, valueEnumerationRef);
        }
    }
}
