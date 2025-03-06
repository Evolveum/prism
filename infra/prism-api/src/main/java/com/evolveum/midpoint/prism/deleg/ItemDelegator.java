/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.deleg;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.annotation.OneUseOnly;
import com.evolveum.midpoint.util.annotation.Unused;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ItemDelegator<V extends PrismValue, D extends ItemDefinition<?>> extends Item<V,D> {

    Item<V,D> delegate();

    @Override
    default D getDefinition() {
        return delegate().getDefinition();
    }

    @Override
    default boolean hasCompleteDefinition() {
        return delegate().hasCompleteDefinition();
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
    default void setDefinition(@NotNull D definition) {
        delegate().setDefinition(definition);
    }

    @Override
    default String getDisplayName() {
        return delegate().getDisplayName();
    }

    @Unused
    @Override
    default String getHelp() {
        return delegate().getHelp();
    }

    @Override
    default boolean isIncomplete() {
        return delegate().isIncomplete();
    }

    @Override
    default void setIncomplete(boolean incomplete) {
        delegate().setIncomplete(incomplete);
    }

    @Override
    default @Nullable PrismContainerValue<?> getParent() {
        return delegate().getParent();
    }

    @Override
    default void setParent(@Nullable PrismContainerValue<?> parentValue) {
        delegate().setParent(parentValue);
    }

    @Override
    default @NotNull ItemPath getPath() {
        return delegate().getPath();
    }

    @Override
    default @Nullable PrismNamespaceContext getNamespaceContext() {
        return delegate().getNamespaceContext();
    }

    @Override
    default @NotNull Map<String, Object> getUserData() {
        return delegate().getUserData();
    }

    @Override
    default <T> T getUserData(String key) {
        return delegate().getUserData(key);
    }

    @Override
    default void setUserData(String key, Object value) {
        delegate().setUserData(key, value);
    }

    @Override
    default @NotNull List<V> getValues() {
        return delegate().getValues();
    }

    @Override
    default Stream<V> valuesStream() {
        return delegate().valuesStream();
    }

    @Override
    default int size() {
        return delegate().size();
    }

    @Override
    default V getAnyValue() {
        return delegate().getAnyValue();
    }

    @Override
    default V getValue() {
        return delegate().getValue();
    }

    @Override
    default V getAnyValue(@NotNull ValueSelector<V> selector) {
        return delegate().getAnyValue(selector);
    }

    @Override
    default @Nullable Object getRealValue() {
        return delegate().getRealValue();
    }

    @Override
    default <X> X getRealValue(Class<X> type) {
        return delegate().getRealValue(type);
    }

    @OneUseOnly("connectorConfiguration")
    @Override
    default <X> X[] getRealValuesArray(Class<X> type) {
        return delegate().getRealValuesArray(type);
    }

    @Override
    default @NotNull Collection<?> getRealValues() {
        return delegate().getRealValues();
    }

    @Override
    default @NotNull <X> Collection<X> getRealValues(Class<X> type) {
        return delegate().getRealValues(type);
    }

    @OneUseOnly("Delta serialization")
    @Experimental
    @Override
    default @NotNull Collection<Object> getRealValuesOrRawTypes() {
        return delegate().getRealValuesOrRawTypes();
    }

    @Override
    default boolean isSingleValue() {
        return delegate().isSingleValue();
    }

    @Override
    default boolean isSingleValueByDefinition() {
        return delegate().isSingleValueByDefinition();
    }

    @Override
    default boolean add(@NotNull V newValue) throws SchemaException {
        return delegate().add(newValue);
    }

    @OneUseOnly("convenience")
    @Override
    default boolean add(@NotNull V newValue, @NotNull EquivalenceStrategy strategy) throws SchemaException {
        return delegate().add(newValue, strategy);
    }

    @Override
    default ItemModifyResult<V> addIgnoringEquivalents(@NotNull V newValue) throws SchemaException {
        return delegate().addIgnoringEquivalents(newValue);
    }

    @Override
    default boolean addAll(Collection<V> newValues) throws SchemaException {
        return delegate().addAll(newValues);
    }

    @Override
    default boolean addAll(Collection<V> newValues, @NotNull EquivalenceStrategy strategy) throws SchemaException {
        return delegate().addAll(newValues, strategy);
    }

    @Override
    default boolean remove(V value) {
        return delegate().remove(value);
    }

    @Override
    default boolean remove(V value, @NotNull EquivalenceStrategy strategy) {
        return delegate().remove(value, strategy);
    }

    @Override
    default void removeIf(Predicate<V> predicate) {
        delegate().removeIf(predicate);
    }

    @Override
    default ItemModifyResult<V> addRespectingMetadataAndCloning(V value, @NotNull EquivalenceStrategy strategy, EquivalenceStrategy metadataEquivalenceStrategy) throws SchemaException {
        return delegate().addRespectingMetadataAndCloning(value, strategy, metadataEquivalenceStrategy);
    }

    @Override
    default ItemModifyResult<V> removeRespectingMetadata(V value, @NotNull EquivalenceStrategy strategy, EquivalenceStrategy metadataEquivalenceStrategy) {
        return delegate().removeRespectingMetadata(value, strategy, metadataEquivalenceStrategy);
    }

    @Override
    default boolean removeAll(Collection<V> values, @NotNull EquivalenceStrategy strategy) {
        return delegate().removeAll(values, strategy);
    }

    @Override
    default void clear() {
        delegate().clear();
    }

    @Override
    default void replaceAll(Collection<V> newValues, @NotNull EquivalenceStrategy strategy) throws SchemaException {
        delegate().replaceAll(newValues, strategy);
    }

    @Override
    default void replace(V newValue) throws SchemaException {
        delegate().replace(newValue);
    }

    @Override
    default boolean equals(Object obj, @NotNull EquivalenceStrategy equivalenceStrategy) {
        return delegate().equals(obj, equivalenceStrategy);
    }

    @Override
    default boolean equals(Object obj, @NotNull ParameterizedEquivalenceStrategy equivalenceStrategy) {
        return delegate().equals(obj, equivalenceStrategy);
    }


    @Override
    default int hashCode(@NotNull EquivalenceStrategy equivalenceStrategy) {
        return delegate().hashCode(equivalenceStrategy);
    }

    @Override
    default int hashCode(@NotNull ParameterizedEquivalenceStrategy equivalenceStrategy) {
        return delegate().hashCode(equivalenceStrategy);
    }

    @Override
    default boolean contains(@NotNull V value) {
        return delegate().contains(value);
    }

    @Override
    default boolean contains(@NotNull V value, @NotNull EquivalenceStrategy strategy) {
        return delegate().contains(value, strategy);
    }

    @Override
    default V findValue(@NotNull V value, @NotNull EquivalenceStrategy strategy) {
        return delegate().findValue(value, strategy);
    }

    @Override
    default V findValue(V value, @NotNull Comparator<V> comparator) {
        return delegate().findValue(value, comparator);
    }

    @Override
    default ItemDelta<V, D> diff(Item<V, D> other) {
        return delegate().diff(other);
    }

    @Override
    default ItemDelta<V, D> diff(Item<V, D> other, @NotNull ParameterizedEquivalenceStrategy strategy) {
        return delegate().diff(other, strategy);
    }

    @Override
    default Collection<V> getClonedValues() {
        return delegate().getClonedValues();
    }

    @Override
    default void normalize() {
        delegate().normalize();
    }

    @Override
    default void merge(Item<V, D> otherItem) throws SchemaException {
        delegate().merge(otherItem);
    }

    @Override
    default Object find(ItemPath path) {
        return delegate().find(path);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> PartiallyResolvedItem<IV, ID> findPartial(ItemPath path) {
        return delegate().findPartial(path);
    }

    @Override
    default ItemDelta<V, D> createDelta() {
        return delegate().createDelta();
    }

    @Override
    default ItemDelta<V, D> createDelta(ItemPath path) {
        return delegate().createDelta(path);
    }

    @Override
    default void acceptParentVisitor(@NotNull Visitor visitor) {
        delegate().acceptParentVisitor(visitor);
    }

    @Override
    default void recomputeAllValues() {
        delegate().recomputeAllValues();
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
    default void applyDefinition(@NotNull D definition) throws SchemaException {
        delegate().applyDefinition(definition);
    }

    @Override
    default void applyDefinitionIfMissing(@NotNull D definition) throws SchemaException {
        delegate().applyDefinitionIfMissing(definition);
    }

    @Override
    default void applyDefinition(@NotNull D definition, boolean force) throws SchemaException {
        delegate().applyDefinition(definition, force);
    }

    @Override
    default Item<V, D> copy() {
        return delegate().copy();
    }


    @Override
    default Item<V, D> createImmutableClone() {
        return delegate().createImmutableClone();
    }

    @Override
    default Item<V, D> cloneComplex(CloneStrategy strategy) {
        return delegate().cloneComplex(strategy);
    }

    @Override
    default void checkConsistence(boolean requireDefinitions, ConsistencyCheckScope scope) {
        delegate().checkConsistence(requireDefinitions, scope);
    }

    @Override
    default void checkConsistence(boolean requireDefinitions, boolean prohibitRaw) {
        delegate().checkConsistence(requireDefinitions, prohibitRaw);
    }

    @Override
    default void checkConsistence(boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope) {
        delegate().checkConsistence(requireDefinitions, prohibitRaw, scope);
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
    default void checkConsistenceInternal(Itemable rootItem, boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope) {
        delegate().checkConsistenceInternal(rootItem, requireDefinitions, prohibitRaw, scope);
    }

    @Override
    default void assertDefinitions() throws SchemaException {
        delegate().assertDefinitions();
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
    default boolean hasRaw() {
        return delegate().hasRaw();
    }

    @Override
    default boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    default boolean hasNoValues() {
        return delegate().hasNoValues();
    }

    @Override
    default boolean hasAnyValue() {
        return delegate().hasAnyValue();
    }

    @Override
    default boolean isOperational() {
        return delegate().isOperational();
    }

    @Override
    default @NotNull Collection<PrismValue> getAllValues(ItemPath path) {
        return delegate().getAllValues(path);
    }

    @Override
    default @NotNull Collection<Item<?, ?>> getAllItems(@NotNull ItemPath path) {
        return delegate().getAllItems(path);
    }

    @Override
    default Long getHighestId() {
        return delegate().getHighestId();
    }

    @Override
    default boolean acceptVisitor(PrismVisitor visitor) {
        return delegate().acceptVisitor(visitor);
    }

    @Override
    default String debugDump() {
        return delegate().debugDump();
    }

    @Override
    default String debugDump(int indent) {
        return delegate().debugDump(indent);
    }

    @Override
    default Object debugDumpLazily() {
        return delegate().debugDumpLazily();
    }

    @Override
    default Object debugDumpLazily(int indent) {
        return delegate().debugDumpLazily(indent);
    }

    @Override
    default void accept(Visitor visitor) {
        delegate().accept(visitor);
    }

    @Override
    default void accept(Visitor visitor, ItemPath path, boolean recursive) {
        delegate().accept(visitor, path, recursive);
    }

    @Override
    default void revive(PrismContext prismContext) {
        delegate().revive(prismContext);
    }

    @Override
    default boolean isImmutable() {
        return delegate().isImmutable();
    }

    @Override
    default void freeze() {
        delegate().freeze();
    }

    @Override
    default void checkMutable() {
        delegate().checkMutable();
    }

    @Override
    default void checkImmutable() {
        delegate().checkImmutable();
    }
}
