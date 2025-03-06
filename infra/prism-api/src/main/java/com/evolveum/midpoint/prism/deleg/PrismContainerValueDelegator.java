/*
 * Copyright (C) 2024-2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union default License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.deleg;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.SchemaLookup;
import com.evolveum.midpoint.util.annotation.Unused;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface PrismContainerValueDelegator<C extends Containerable> extends PrismContainerValue<C>, PrismValueDelegator {


    @Override
    PrismContainerValue<C> delegate();

    @Override
    default boolean acceptVisitor(PrismVisitor visitor) {
        return delegate().acceptVisitor(visitor);
    }

    @Override
    default SchemaLookup schemaLookup() {
        return delegate().schemaLookup();
    }

    @Override
    default void checkNothingExceptFor(QName... allowedItemNames) {
        delegate().checkNothingExceptFor(allowedItemNames);
    }

    @Override
    default boolean hasNoItems() {
        return delegate().hasNoItems();
    }

    @Override
    default void acceptParentVisitor(Visitor visitor) {
        delegate().acceptParentVisitor(visitor);
    }

    @Override
    default PrismContainerDefinition<C> getDefinition() {
        return delegate().getDefinition();
    }

    @Override
    default void removeOperationalItems() {
        delegate().removeOperationalItems();
    }

    @Override
    default void removeItems(List<? extends ItemPath> itemsToRemove) {
        delegate().removeItems(itemsToRemove);
    }

    @Override
    default void removeMetadataFromPaths(List<? extends ItemPath> pathsToRemoveMetadata) throws SchemaException {
        delegate().removeMetadataFromPaths(pathsToRemoveMetadata);
    }

    @Override
    default void removePaths(List<? extends ItemPath> remove) throws SchemaException {
        delegate().removePaths(remove);
    }

    @Override
    default void keepPaths(List<? extends ItemPath> keep) throws SchemaException {
        delegate().keepPaths(keep);
    }

    @Override
    default void setOriginTypeRecursive(OriginType originType) {
        delegate().setOriginTypeRecursive(originType);
    }

    @Override
    default @NotNull PrismContainerValue<?> getRootValue() {
        return delegate().getRootValue();
    }

    @Override
    default void mergeContent(@NotNull PrismContainerValue<?> other, @NotNull List<QName> overwrite) throws SchemaException {
        delegate().mergeContent(other, overwrite);
    }

    @Override
    default PrismContainer<C> asSingleValuedContainer(@NotNull QName itemName) throws SchemaException {
        return delegate().asSingleValuedContainer(itemName);
    }

    @Override
    default @Nullable ComplexTypeDefinition getComplexTypeDefinition() {
        return delegate().getComplexTypeDefinition();
    }

    @Override
    default boolean equivalent(PrismContainerValue<?> other) {
        return delegate().equivalent(other);
    }

    @Override
    default PrismContainerValue<C> cloneComplex(@NotNull CloneStrategy strategy) {
        return delegate().cloneComplex(strategy);
    }

    @Override
    default PrismContainerValue<C> createImmutableClone() {
        return delegate().createImmutableClone();
    }

    @Override
    default PrismContainerValue<C> clone() {
        return delegate().clone();
    }

    @Override
    default void assertDefinitions(boolean tolerateRaw, Supplier<String> sourceDescriptionSupplier) throws SchemaException {
        delegate().assertDefinitions(tolerateRaw, sourceDescriptionSupplier);
    }

    @Override
    default void assertDefinitions(Supplier<String> sourceDescriptionSupplier) throws SchemaException {
        delegate().assertDefinitions(sourceDescriptionSupplier);
    }

    @Override
    default boolean isIdOnly() {
        return delegate().isIdOnly();
    }

    @Override
    default PrismContainerValue<C> applyDefinition(@NotNull PrismContainerDefinition<C> containerDef, boolean force) throws SchemaException {
        return delegate().applyDefinition(containerDef, force);
    }

    @Override
    default PrismContainerValue<C> applyDefinition(@NotNull ItemDefinition<?> itemDefinition, boolean force) throws SchemaException {
        return delegate().applyDefinition(itemDefinition, force);
    }

    @Override
    default PrismContainerValue<C> applyDefinition(@NotNull ItemDefinition<?> itemDefinition) throws SchemaException {
        return delegate().applyDefinition(itemDefinition);
    }

    @Override
    default boolean removeRawElement(Object element) {
        return delegate().removeRawElement(element);
    }

    @Override
    default boolean deleteRawElement(Object element) throws SchemaException {
        return delegate().deleteRawElement(element);
    }

    @Override
    default boolean addRawElement(Object element) throws SchemaException {
        return delegate().addRawElement(element);
    }

    @Override
    default boolean hasCompleteDefinition() {
        return delegate().hasCompleteDefinition();
    }

    @Override
    default void accept(Visitor visitor, ItemPath path, boolean recursive) {
        delegate().accept(visitor, path, recursive);
    }

    @Override
    default void accept(Visitor visitor) {
        delegate().accept(visitor);
    }

    @Override
    default void recompute(PrismContext prismContext) {
        delegate().recompute(prismContext);
    }

    @Override
    default <T> T getItemRealValue(ItemName itemName, Class<T> type) {
        return delegate().getItemRealValue(itemName, type);
    }

    @Override
    default <T> T getPropertyRealValue(QName propertyName, Class<T> type) {
        return delegate().getPropertyRealValue(propertyName, type);
    }

    @Override
    default <T> void setPropertyRealValue(QName propertyName, T realValue) throws SchemaException {
        delegate().setPropertyRealValue(propertyName, realValue);
    }

    @Override
    default void removeReference(ItemPath path) {
        delegate().removeReference(path);
    }

    @Override
    default void removeContainer(ItemPath path) {
        delegate().removeContainer(path);
    }

    @Override
    default void removeProperty(ItemPath path) {
        delegate().removeProperty(path);
    }

    @Override
    default void removeItem(@NotNull ItemPath path) {
        delegate().removeItem(path);
    }

    @Override
    default <X> PrismProperty<X> createProperty(PrismPropertyDefinition<X> propertyDefinition) throws SchemaException {
        return delegate().createProperty(propertyDefinition);
    }

    @Override
    default <X> PrismProperty<X> createProperty(QName propertyName) throws SchemaException {
        return delegate().createProperty(propertyName);
    }

    @Override
    default <X> PrismProperty<X> findOrCreateProperty(PrismPropertyDefinition propertyDef) throws SchemaException {
        return delegate().findOrCreateProperty(propertyDef);
    }

    @Override
    default <X> PrismProperty<X> findOrCreateProperty(ItemPath propertyPath) throws SchemaException {
        return delegate().findOrCreateProperty(propertyPath);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findOrCreateItem(ItemPath path, Class<I> type, ID definition) throws SchemaException {
        return delegate().findOrCreateItem(path, type, definition);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findOrCreateItem(QName containerName, Class<I> type) throws SchemaException {
        return delegate().findOrCreateItem(containerName, type);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> findOrCreateItem(QName containerName) throws SchemaException {
        return delegate().findOrCreateItem(containerName);
    }

    @Override
    default PrismReference findOrCreateReference(QName referenceName) throws SchemaException {
        return delegate().findOrCreateReference(referenceName);
    }

    @Override
    default <T extends Containerable> PrismContainer<T> findOrCreateContainer(QName containerName) throws SchemaException {
        return delegate().findOrCreateContainer(containerName);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I createDetachedSubItem(QName name, Class<I> type, ID itemDefinition, boolean immutable) throws SchemaException, RemovedItemDefinitionException {
        return delegate().createDetachedSubItem(name, type, itemDefinition, immutable);
    }

    @Override
    default boolean containsItem(ItemPath propPath, boolean acceptEmptyItem) throws SchemaException {
        return delegate().containsItem(propPath, acceptEmptyItem);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findItem(ItemDefinition itemDefinition, Class<I> type) {
        return delegate().findItem(itemDefinition, type);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> findItem(ItemPath itemPath) {
        return delegate().findItem(itemPath);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findItem(ItemPath itemPath, Class<I> type) {
        return delegate().findItem(itemPath, type);
    }

    @Override
    default PrismReference findReference(QName elementName) {
        return delegate().findReference(elementName);
    }

    @Override
    default <X extends Containerable> PrismContainer<X> findContainer(QName containerName) {
        return delegate().findContainer(containerName);
    }

    @Override
    default <X> PrismProperty<X> findProperty(PrismPropertyDefinition<X> propertyDefinition) {
        return delegate().findProperty(propertyDefinition);
    }

    @Override
    default <X> PrismProperty<X> findProperty(ItemPath propertyPath) {
        return delegate().findProperty(propertyPath);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> PartiallyResolvedItem<IV, ID> findPartial(ItemPath path) {
        return delegate().findPartial(path);
    }

    @Override
    default boolean contains(ItemName itemName) {
        return delegate().contains(itemName);
    }

    @Override
    default boolean contains(Item item) {
        return delegate().contains(item);
    }

    @Override
    default void clear() {
        delegate().clear();
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> void replace(Item<IV, ID> oldItem, Item<IV, ID> newItem) throws SchemaException {
        delegate().replace(oldItem, newItem);
    }

    @Override
    default void addAllReplaceExisting(Collection<? extends Item<?, ?>> itemsToAdd) throws SchemaException {
        delegate().addAllReplaceExisting(itemsToAdd);
    }

    @Override
    default void addAll(Collection<? extends Item<?, ?>> itemsToAdd) throws SchemaException {
        delegate().addAll(itemsToAdd);
    }

    @Override
    default void removeAll() {
        delegate().removeAll();
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> void remove(Item<IV, ID> item) {
        delegate().remove(item);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> void addReplaceExisting(Item<IV, ID> item) throws SchemaException {
        delegate().addReplaceExisting(item);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> boolean subtract(Item<IV, ID> item) throws SchemaException {
        return delegate().subtract(item);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> boolean merge(Item<IV, ID> item) throws SchemaException {
        return delegate().merge(item);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> void add(Item<IV, ID> item, boolean checkUniqueness) throws SchemaException {
        delegate().add(item, checkUniqueness);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> void add(Item<IV, ID> item) throws SchemaException {
        delegate().add(item);
    }

    @Override
    default @NotNull Collection<QName> getItemNames() {
        return delegate().getItemNames();
    }

    @Override
    default C asContainerable(Class<C> requiredClass) {
        return delegate().asContainerable(requiredClass);
    }

    @Override
    default boolean canRepresent(Class<?> clazz) {
        return delegate().canRepresent(clazz);
    }

    @Override
    default Class<C> getCompileTimeClass() {
        return delegate().getCompileTimeClass();
    }

    @Override
    default @NotNull C asContainerable() {
        return delegate().asContainerable();
    }

    @Override
    default C getValue() {
        return delegate().getValue();
    }

    @Override
    default PrismContainer<C> getContainer() {
        return delegate().getContainer();
    }

    @Override
    default PrismContainerable<C> getParent() {
        return delegate().getParent();
    }

    @Override
    default void setId(Long id) {
        delegate().setId(id);
    }

    @Override
    default Long getId() {
        return delegate().getId();
    }

    @Unused
    @Override
    default @NotNull Set<PrismProperty<?>> getProperties() {
        return delegate().getProperties();
    }

    @Override
    default int size() {
        return delegate().size();
    }

    @Contract(pure = true)
    @Override
    default @NotNull Collection<Item<?, ?>> getItems() {
        return delegate().getItems();
    }

    @Override
    default void revive(PrismContext prismContext) {
        delegate().revive(prismContext);
    }

    @Override
    default void walk(BiPredicate<? super ItemPath, Boolean> descendPredicate, Predicate<? super ItemPath> consumePredicate, Consumer<? super Item<?, ?>> itemConsumer) throws SchemaException {
        delegate().walk(descendPredicate, consumePredicate, itemConsumer);
    }
}
