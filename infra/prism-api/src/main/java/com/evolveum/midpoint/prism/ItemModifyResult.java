package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.evolveum.midpoint.prism.ItemModifyResult.ActualApplyOperation.*;

public class ItemModifyResult<V extends PrismValue> {



    public enum ActualApplyOperation {
        /**
         * Value was actually added to parent item - no value was preexisting.
         */
        ADDED,
        /**
         * Value was removed from parent item.
         */
        DELETED,
        /**
         * Operation resulted in modification of preexisting value. This may happen even for add / delete item delta operations
         * if metadata are considered.
         */
        MODIFIED,

        /**
         * Operation did not result in any modificiation of preexisting value or item itself.
         */
        UNMODIFIED
    }

    @NotNull
    public final V requestValue;
    @Nullable
    public final V finalValue;
    public final ActualApplyOperation operation;

    protected ItemModifyResult(V requestValue, V finalValue, ActualApplyOperation operation) {
        this.requestValue = requestValue;
        this.finalValue = finalValue;
        this.operation = operation;
    }

    public static <V extends PrismValue> ItemModifyResult<V> unmodified(V value) {
        return from(value, null, UNMODIFIED);
    }

    public static <V extends PrismValue> ItemModifyResult<V> removed(V requestValue, V existingValue) {
        return from(requestValue, existingValue, DELETED);
    }

    public static <V extends PrismValue> ItemModifyResult<V> modified(V requestValue, V existingValue) {
        return from(requestValue, existingValue, MODIFIED);
    }

    public static <V extends PrismValue> ItemModifyResult<V> added(V requestValue, V existingValue) {
        return from(requestValue, existingValue, ADDED);
    }

    public static <V extends PrismValue> ItemModifyResult<V> from(V requestValue, V existingValue, ActualApplyOperation actualApplyOperation) {
        return new ItemModifyResult<>(requestValue, existingValue, actualApplyOperation);
    }

    public <T> T requestRealValue() {
        return requestValue.getRealValue();
    }

    public <T> T finalRealValue() {
        return finalValue != null ? finalValue.getRealValue() : null;
    }

    public ActualApplyOperation operation() {
        return operation;
    }

    public boolean isUnmodified() {
        return UNMODIFIED.equals(operation);
    }

    public boolean isChanged() {
        return !isUnmodified();
    }

}
