/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.deleg;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ItemDeltaValidator;
import com.evolveum.midpoint.prism.delta.PlusMinusZero;
import com.evolveum.midpoint.prism.delta.PrismValueDeltaSetTriple;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.Processor;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ItemDeltaDelegator<V extends PrismValue, D extends ItemDefinition<?>> extends ItemDelta<V, D> {

    ItemDelta<V, D> delegate();

    @Override
    default void freeze() {
        delegate().freeze();
    }

    @Override
    default PrismContext getPrismContext() {
        return delegate().getPrismContext();
    }

    @Override
    default ItemName getElementName() {
        return delegate().getElementName();
    }

    @Override
    default void setElementName(QName elementName) {
        delegate().setElementName(elementName);
    }

    @Override
    default ItemPath getParentPath() {
        return delegate().getParentPath();
    }

    @Override
    default void setParentPath(ItemPath parentPath) {
        delegate().setParentPath(parentPath);
    }

    @Override
    default @NotNull ItemPath getPath() {
        return delegate().getPath();
    }

    @Override
    default D getDefinition() {
        return delegate().getDefinition();
    }

    @Override
    default void setDefinition(D definition) {
        delegate().setDefinition(definition);
    }

    @SuppressWarnings("rawtypes")
    @Override
    default void accept(Visitor visitor) {
        delegate().accept(visitor);
    }

    @SuppressWarnings("rawtypes")
    @Override
    default void accept(Visitor visitor, boolean includeOldValues) {
        delegate().accept(visitor, includeOldValues);
    }

    @Override
    default int size() {
        return delegate().size();
    }

    @SuppressWarnings("rawtypes")
    @Override
    default void accept(Visitor visitor, ItemPath path, boolean recursive) {
        delegate().accept(visitor, path, recursive);
    }

    @Override
    default void applyDefinition(D definition) throws SchemaException {
        delegate().applyDefinition(definition);
    }

    @Override
    default boolean hasCompleteDefinition() {
        return delegate().hasCompleteDefinition();
    }

    @Override
    default Class<? extends Item> getItemClass() {
        return delegate().getItemClass();
    }

    @Override
    default Collection<V> getValuesToAdd() {
        return delegate().getValuesToAdd();
    }

    @Override
    default void clearValuesToAdd() {
        delegate().clearValuesToAdd();
    }

    @Override
    default Collection<V> getValuesToDelete() {
        return delegate().getValuesToDelete();
    }

    @Override
    default void clearValuesToDelete() {
        delegate().clearValuesToDelete();
    }

    @Override
    default Collection<V> getValuesToReplace() {
        return delegate().getValuesToReplace();
    }

    @Override
    default void clearValuesToReplace() {
        delegate().clearValuesToReplace();
    }

    @Override
    default void addValuesToAdd(Collection<V> newValues) {
        delegate().addValuesToAdd(newValues);
    }

    @SuppressWarnings("unchecked")
    @Override
    default void addValuesToAdd(V... newValues) {
        delegate().addValuesToAdd(newValues);
    }

    @Override
    default void addValueToAdd(V newValue) {
        delegate().addValueToAdd(newValue);
    }

    @Override
    default boolean removeValueToAdd(PrismValue valueToRemove) {
        return delegate().removeValueToAdd(valueToRemove);
    }

    @Override
    default boolean removeValueToDelete(PrismValue valueToRemove) {
        return delegate().removeValueToDelete(valueToRemove);
    }

    @Override
    default boolean removeValueToReplace(PrismValue valueToRemove) {
        return delegate().removeValueToReplace(valueToRemove);
    }

    @Override
    default void mergeValuesToAdd(Collection<V> newValues) {
        delegate().mergeValuesToAdd(newValues);
    }

    @Override
    default void mergeValuesToAdd(V[] newValues) {
        delegate().mergeValuesToAdd(newValues);
    }

    @Override
    default void mergeValueToAdd(V newValue) {
        delegate().mergeValueToAdd(newValue);
    }

    @Override
    default void addValuesToDelete(Collection<V> newValues) {
        delegate().addValuesToDelete(newValues);
    }

    @SuppressWarnings("unchecked")
    @Override
    default void addValuesToDelete(V... newValues) {
        delegate().addValuesToDelete(newValues);
    }

    @Override
    default void addValueToDelete(V newValue) {
        delegate().addValueToDelete(newValue);
    }

    @Override
    default void mergeValuesToDelete(Collection<V> newValues) {
        delegate().mergeValuesToDelete(newValues);
    }

    @Override
    default void mergeValuesToDelete(V[] newValues) {
        delegate().mergeValuesToDelete(newValues);
    }

    @Override
    default void mergeValueToDelete(V newValue) {
        delegate().mergeValueToDelete(newValue);
    }

    @Override
    default void resetValuesToAdd() {
        delegate().resetValuesToAdd();
    }

    @Override
    default void resetValuesToDelete() {
        delegate().resetValuesToDelete();
    }

    @Override
    default void resetValuesToReplace() {
        delegate().resetValuesToReplace();
    }

    @Override
    default void setValuesToReplace(Collection<V> newValues) {
        delegate().setValuesToReplace(newValues);
    }

    @SuppressWarnings("unchecked")
    @Override
    default void setValuesToReplace(V... newValues) {
        delegate().setValuesToReplace(newValues);
    }

    @Override
    default void setValueToReplace() {
        delegate().setValueToReplace();
    }

    @Override
    default void setValueToReplace(V newValue) {
        delegate().setValueToReplace(newValue);
    }

    @Override
    default void addValueToReplace(V newValue) {
        delegate().addValueToReplace(newValue);
    }

    @Override
    default void mergeValuesToReplace(Collection<V> newValues) {
        delegate().mergeValuesToReplace(newValues);
    }

    @Override
    default void mergeValuesToReplace(V[] newValues) {
        delegate().mergeValuesToReplace(newValues);
    }

    @Override
    default void mergeValueToReplace(V newValue) {
        delegate().mergeValueToReplace(newValue);
    }

    @Override
    default boolean isValueToAdd(V value) {
        return delegate().isValueToAdd(value);
    }

    @Override
    default boolean isValueToDelete(V value) {
        return delegate().isValueToDelete(value);
    }

    @Override
    default boolean isValueToReplace(V value) {
        return delegate().isValueToReplace(value);
    }

    @Override
    default V getAnyValue() {
        return delegate().getAnyValue();
    }

    @Override
    default boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    default boolean isLiterallyEmpty() {
        return delegate().isLiterallyEmpty();
    }

    @Override
    default boolean addsAnyValue() {
        return delegate().addsAnyValue();
    }

    @Override
    default void foreach(Processor<V> processor) {
        delegate().foreach(processor);
    }

    @Override
    default Collection<V> getEstimatedOldValues() {
        return delegate().getEstimatedOldValues();
    }

    @Override
    default void setEstimatedOldValues(Collection<V> estimatedOldValues) {
        delegate().setEstimatedOldValues(estimatedOldValues);
    }

    @Override
    default void addEstimatedOldValues(Collection<V> newValues) {
        delegate().addEstimatedOldValues(newValues);
    }

    @SuppressWarnings("unchecked")
    @Override
    default void addEstimatedOldValues(V... newValues) {
        delegate().addEstimatedOldValues(newValues);
    }

    @Override
    default void addEstimatedOldValue(V newValue) {
        delegate().addEstimatedOldValue(newValue);
    }

    @Override
    default void normalize() {
        delegate().normalize();
    }

    @Override
    default boolean isReplace() {
        return delegate().isReplace();
    }

    @Override
    default boolean isAdd() {
        return delegate().isAdd();
    }

    @Override
    default boolean isDelete() {
        return delegate().isDelete();
    }

    @Override
    default void clear() {
        delegate().clear();
    }

    @Override
    default ItemDelta<V, D> narrow(PrismObject<? extends Objectable> object, @NotNull Comparator<V> plusComparator, @NotNull Comparator<V> minusComparator, boolean assumeMissingItems) {
        return delegate().narrow(object, plusComparator, minusComparator, assumeMissingItems);
    }

    @Override
    default boolean isRedundant(PrismObject<? extends Objectable> object, ParameterizedEquivalenceStrategy strategy, boolean assumeMissingItems) {
        return delegate().isRedundant(object, strategy, assumeMissingItems);
    }

    @Override
    default void validate() throws SchemaException {
        delegate().validate();
    }

    @Override
    default void validate(String contextDescription) throws SchemaException {
        delegate().validate(contextDescription);
    }

    @Override
    default void validateValues(ItemDeltaValidator<V> validator) throws SchemaException {
        delegate().validateValues(validator);
    }

    @Override
    default void validateValues(ItemDeltaValidator<V> validator, Collection<V> oldValues) throws SchemaException {
        delegate().validateValues(validator, oldValues);
    }

    @Override
    default void checkConsistence() {
        delegate().checkConsistence();
    }

    @Override
    default void checkConsistence(ConsistencyCheckScope scope) {
        delegate().checkConsistence(scope);
    }

    @Override
    default void checkConsistence(boolean requireDefinition, boolean prohibitRaw, ConsistencyCheckScope scope) {
        delegate().checkConsistence(requireDefinition, prohibitRaw, scope);
    }

    @Override
    default void distributeReplace(Collection<V> existingValues) {
        delegate().distributeReplace(existingValues);
    }

    @Override
    default void merge(ItemDelta<V, D> deltaToMerge) {
        delegate().merge(deltaToMerge);
    }

    @Override
    default Collection<V> getValueChanges(PlusMinusZero mode) {
        return delegate().getValueChanges(mode);
    }

    @Override
    default void simplify() {
        delegate().simplify();
    }

    @Override
    default void applyTo(PrismContainerValue containerValue) throws SchemaException {
        delegate().applyTo(containerValue);
    }

    @Override
    default void applyTo(Item item) throws SchemaException {
        delegate().applyTo(item);
    }

    @Override
    default void applyToMatchingPath(Item item) throws SchemaException {
        delegate().applyToMatchingPath(item);
    }

    @Override
    default ItemDelta<?, ?> getSubDelta(ItemPath path) {
        return delegate().getSubDelta(path);
    }

    @Override
    default boolean isApplicableTo(Item item) {
        return delegate().isApplicableTo(item);
    }

    @Override
    default Item<V, D> getItemNew() throws SchemaException {
        return delegate().getItemNew();
    }

    @Override
    default Item<V, D> getItemNew(Item<V, D> itemOld) throws SchemaException {
        return delegate().getItemNew(itemOld);
    }

    @Override
    default Item<V, D> getItemNewMatchingPath(Item<V, D> itemOld) throws SchemaException {
        return delegate().getItemNewMatchingPath(itemOld);
    }

    @Override
    default boolean contains(ItemDelta<V, D> other) {
        return delegate().contains(other);
    }

    @Override
    default boolean contains(ItemDelta<V, D> other, EquivalenceStrategy strategy) {
        return delegate().contains(other, strategy);
    }

    @Override
    default void filterValues(Function<V, Boolean> function) {
        delegate().filterValues(function);
    }

    @Override
    default void filterYields(BiFunction<V, PrismContainerValue, Boolean> function) {
        delegate().filterYields(function);
    }

    @Override
    default ItemDelta<V, D> clone() {
        return delegate().clone();
    }

    @Override
    default ItemDelta<V, D> cloneWithChangedParentPath(ItemPath newParentPath) {
        return delegate().cloneWithChangedParentPath(newParentPath);
    }

    @Override
    default PrismValueDeltaSetTriple<V> toDeltaSetTriple(Item<V, D> itemOld) throws SchemaException {
        return delegate().toDeltaSetTriple(itemOld);
    }

    @Override
    default void assertDefinitions(Supplier<String> sourceDescriptionSupplier) throws SchemaException {
        delegate().assertDefinitions(sourceDescriptionSupplier);
    }

    @Override
    default void assertDefinitions(boolean tolerateRawValues, Supplier<String> sourceDescriptionSupplier) throws SchemaException {
        delegate().assertDefinitions(tolerateRawValues, sourceDescriptionSupplier);
    }

    @Override
    default boolean isRaw() {
        return delegate().isRaw();
    }

    @Override
    default void revive(PrismContext prismContext) throws SchemaException {
        delegate().revive(prismContext);
    }

    @Override
    default void applyDefinition(D itemDefinition, boolean force) throws SchemaException {
        delegate().applyDefinition(itemDefinition, force);
    }

    @Override
    default boolean equivalent(ItemDelta other) {
        return delegate().equivalent(other);
    }

    @Override
    default String debugDump(int indent) {
        return delegate().debugDump(indent);
    }

    @Override
    default void addToReplaceDelta() {
        delegate().addToReplaceDelta();
    }

    @Override
    default ItemDelta<V, D> createReverseDelta() {
        return delegate().createReverseDelta();
    }

    @Override
    default V findValueToAddOrReplace(V value) {
        return delegate().findValueToAddOrReplace(value);
    }

    @Override
    default void setOriginTypeRecursive(OriginType originType) {
        delegate().setOriginTypeRecursive(originType);
    }

    @Override
    default boolean isImmutable() {
        return delegate().isImmutable();
    }
}
