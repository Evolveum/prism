/*
 * Copyright (C) 2024 Evolveum and contributors
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
import com.evolveum.midpoint.prism.schema.SchemaLookup;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.annotation.Unused;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface PrismContainerValueDelegator<C extends Containerable> extends PrismContainerValue<C> {

    PrismContainerValue<C> delegate();

    default boolean isImmutable() {
        return delegate().isImmutable();
    }

    default void freeze() {
        delegate().freeze();
    }

    default void checkMutable() {
        delegate().checkMutable();
    }

    default void checkImmutable() {
        delegate().checkImmutable();
    }

    default void revive(PrismContext prismContext) {
        delegate().revive(prismContext);
    }

    default Class<C> getCompileTimeClass() {
        return delegate().getCompileTimeClass();
    }

    @Unused
    default @NotNull Set<PrismProperty<?>> getProperties() {
        return delegate().getProperties();
    }

    @Contract(pure = true)
    default @NotNull Collection<Item<?, ?>> getItems() {
        return delegate().getItems();
    }

    default void setOriginTypeRecursive(OriginType originType) {
        delegate().setOriginTypeRecursive(originType);
    }

    default void keepPaths(List<? extends ItemPath> keep) throws SchemaException {
        delegate().keepPaths(keep);
    }

    default int size() {
        return delegate().size();
    }

    default boolean deleteRawElement(Object element) throws SchemaException {
        return delegate().deleteRawElement(element);
    }

    default <X extends Containerable> PrismContainer<X> findContainer(QName containerName) {
        return delegate().findContainer(containerName);
    }

    default <X> PrismProperty<X> findOrCreateProperty(ItemPath propertyPath) throws SchemaException {
        return delegate().findOrCreateProperty(propertyPath);
    }

    default <T> T getPropertyRealValue(QName propertyName, Class<T> type) {
        return delegate().getPropertyRealValue(propertyName, type);
    }

    default boolean equivalent(PrismContainerValue<?> other) {
        return delegate().equivalent(other);
    }

    default <X> PrismProperty<X> findOrCreateProperty(PrismPropertyDefinition propertyDef) throws SchemaException {
        return delegate().findOrCreateProperty(propertyDef);
    }

    default boolean removeRawElement(Object element) {
        return delegate().removeRawElement(element);
    }

    default PrismContainerable<C> getParent() {
        return delegate().getParent();
    }

    default void removeItem(@NotNull ItemPath path) {
        delegate().removeItem(path);
    }

    default void removeProperty(ItemPath path) {
        delegate().removeProperty(path);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>> void remove(Item<IV, ID> item) {
        delegate().remove(item);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findItem(ItemDefinition itemDefinition, Class<I> type) {
        return delegate().findItem(itemDefinition, type);
    }

    default boolean hasNoItems() {
        return delegate().hasNoItems();
    }

    default PrismReference findOrCreateReference(QName referenceName) throws SchemaException {
        return delegate().findOrCreateReference(referenceName);
    }

    default void accept(Visitor visitor, ItemPath path, boolean recursive) {
        delegate().accept(visitor, path, recursive);
    }

    @Nullable
    default ComplexTypeDefinition getComplexTypeDefinition() {
        return delegate().getComplexTypeDefinition();
    }

    default boolean hasCompleteDefinition() {
        return delegate().hasCompleteDefinition();
    }

    default void accept(Visitor visitor) {
        delegate().accept(visitor);
    }

    default <X> PrismProperty<X> findProperty(ItemPath propertyPath) {
        return delegate().findProperty(propertyPath);
    }

    default void recompute(PrismContext prismContext) {
        delegate().recompute(prismContext);
    }

    default void removeReference(ItemPath path) {
        delegate().removeReference(path);
    }

    default PrismContainerValue<C> applyDefinition(@NotNull ItemDefinition<?> itemDefinition) throws SchemaException {
        return delegate().applyDefinition(itemDefinition);
    }

    default PrismContainerValue<C> applyDefinition(@NotNull ItemDefinition<?> itemDefinition, boolean force) throws SchemaException {
        return delegate().applyDefinition(itemDefinition, force);
    }

    default void addAll(Collection<? extends Item<?, ?>> itemsToAdd) throws SchemaException {
        delegate().addAll(itemsToAdd);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>> void add(Item<IV, ID> item) throws SchemaException {
        delegate().add(item);
    }

    default void addAllReplaceExisting(Collection<? extends Item<?, ?>> itemsToAdd) throws SchemaException {
        delegate().addAllReplaceExisting(itemsToAdd);
    }

    default void removeAll() {
        delegate().removeAll();
    }

    default PrismContainerDefinition<C> getDefinition() {
        return delegate().getDefinition();
    }

    default PrismContainerValue<C> createImmutableClone() {
        return delegate().createImmutableClone();
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>> void add(Item<IV, ID> item, boolean checkUniqueness) throws SchemaException {
        delegate().add(item, checkUniqueness);
    }

    default C asContainerable(Class<C> requiredClass) {
        return delegate().asContainerable(requiredClass);
    }

    default void assertDefinitions(Supplier<String> sourceDescriptionSupplier) throws SchemaException {
        delegate().assertDefinitions(sourceDescriptionSupplier);
    }

    default void removeOperationalItems() {
        delegate().removeOperationalItems();
    }

    default boolean contains(ItemName itemName) {
        return delegate().contains(itemName);
    }

    @NotNull
    default PrismContainerValue<?> getRootValue() {
        return delegate().getRootValue();
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>> boolean subtract(Item<IV, ID> item) throws SchemaException {
        return delegate().subtract(item);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>> void addReplaceExisting(Item<IV, ID> item) throws SchemaException {
        delegate().addReplaceExisting(item);
    }

    default SchemaLookup schemaLookup() {
        return delegate().schemaLookup();
    }

    default void clear() {
        delegate().clear();
    }

    default void removeContainer(ItemPath path) {
        delegate().removeContainer(path);
    }

    default void assertDefinitions(boolean tolerateRaw, Supplier<String> sourceDescriptionSupplier) throws SchemaException {
        delegate().assertDefinitions(tolerateRaw, sourceDescriptionSupplier);
    }

    default <T> T getItemRealValue(ItemName itemName, Class<T> type) {
        return delegate().getItemRealValue(itemName, type);
    }

    default void mergeContent(@NotNull PrismContainerValue<?> other, @NotNull List<QName> overwrite) throws SchemaException {
        delegate().mergeContent(other, overwrite);
    }

    default boolean addRawElement(Object element) throws SchemaException {
        return delegate().addRawElement(element);
    }

    default @NotNull Collection<QName> getItemNames() {
        return delegate().getItemNames();
    }

    default PrismContainer<C> getContainer() {
        return delegate().getContainer();
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findOrCreateItem(ItemPath path, Class<I> type, ID definition) throws SchemaException {
        return delegate().findOrCreateItem(path, type, definition);
    }

    default C getValue() {
        return delegate().getValue();
    }

    default PrismContainerValue<C> cloneComplex(@NotNull CloneStrategy strategy) {
        return delegate().cloneComplex(strategy);
    }

    default <T extends Containerable> PrismContainer<T> findOrCreateContainer(QName containerName) throws SchemaException {
        return delegate().findOrCreateContainer(containerName);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> findItem(ItemPath itemPath) {
        return delegate().findItem(itemPath);
    }

    default boolean containsItem(ItemPath propPath, boolean acceptEmptyItem) throws SchemaException {
        return delegate().containsItem(propPath, acceptEmptyItem);
    }

    default boolean contains(Item item) {
        return delegate().contains(item);
    }

    default void removeItems(List<? extends ItemPath> itemsToRemove) {
        delegate().removeItems(itemsToRemove);
    }

    default <X> PrismProperty<X> findProperty(PrismPropertyDefinition<X> propertyDefinition) {
        return delegate().findProperty(propertyDefinition);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>> PartiallyResolvedItem<IV, ID> findPartial(ItemPath path) {
        return delegate().findPartial(path);
    }

    @NotNull
    default C asContainerable() {
        return delegate().asContainerable();
    }

    default <X> PrismProperty<X> createProperty(QName propertyName) throws SchemaException {
        return delegate().createProperty(propertyName);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findOrCreateItem(QName containerName, Class<I> type) throws SchemaException {
        return delegate().findOrCreateItem(containerName, type);
    }

    default void removePaths(List<? extends ItemPath> remove) throws SchemaException {
        delegate().removePaths(remove);
    }

    default void removeMetadataFromPaths(List<? extends ItemPath> pathsToRemoveMetadata) throws SchemaException {
        delegate().removeMetadataFromPaths(pathsToRemoveMetadata);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>> boolean merge(Item<IV, ID> item) throws SchemaException {
        return delegate().merge(item);
    }

    default boolean canRepresent(Class<?> clazz) {
        return delegate().canRepresent(clazz);
    }

    default <T> void setPropertyRealValue(QName propertyName, T realValue) throws SchemaException {
        delegate().setPropertyRealValue(propertyName, realValue);
    }

    default void checkNothingExceptFor(QName... allowedItemNames) {
        delegate().checkNothingExceptFor(allowedItemNames);
    }

    default PrismContainerValue<C> applyDefinition(@NotNull PrismContainerDefinition<C> containerDef, boolean force) throws SchemaException {
        return delegate().applyDefinition(containerDef, force);
    }

    default PrismReference findReference(QName elementName) {
        return delegate().findReference(elementName);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findItem(ItemPath itemPath, Class<I> type) {
        return delegate().findItem(itemPath, type);
    }

    default void acceptParentVisitor(Visitor visitor) {
        delegate().acceptParentVisitor(visitor);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I createDetachedSubItem(QName name, Class<I> type, ID itemDefinition, boolean immutable) throws SchemaException, PrismContainerValue.RemovedItemDefinitionException {
        return delegate().createDetachedSubItem(name, type, itemDefinition, immutable);
    }

    default <X> PrismProperty<X> createProperty(PrismPropertyDefinition<X> propertyDefinition) throws SchemaException {
        return delegate().createProperty(propertyDefinition);
    }

    default PrismContainer<C> asSingleValuedContainer(@NotNull QName itemName) throws SchemaException {
        return delegate().asSingleValuedContainer(itemName);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> findOrCreateItem(QName containerName) throws SchemaException {
        return delegate().findOrCreateItem(containerName);
    }

    default <IV extends PrismValue, ID extends ItemDefinition<?>> void replace(Item<IV, ID> oldItem, Item<IV, ID> newItem) throws SchemaException {
        delegate().replace(oldItem, newItem);
    }

    default void setOriginObject(Objectable source) {
        delegate().setOriginObject(source);
    }

    default void setOriginType(OriginType type) {
        delegate().setOriginType(type);
    }

    default OriginType getOriginType() {
        return delegate().getOriginType();
    }

    default Objectable getOriginObject() {
        return delegate().getOriginObject();
    }

    default Object getUserData(@NotNull String key) {
        return delegate().getUserData(key);
    }

    default Map<String, Object> getUserData() {
        return delegate().getUserData();
    }

    default boolean isRaw() {
        return delegate().isRaw();
    }

    @Experimental
    default boolean isTransient() {
        return delegate().isTransient();
    }

    default void normalize() {
        delegate().normalize();
    }

    @Experimental
    @Nullable
    default Object getRealValueIfExists() {
        return delegate().getRealValueIfExists();
    }

    @Experimental
    default boolean isOfType(@NotNull QName expectedTypeName) {
        return delegate().isOfType(expectedTypeName);
    }

    @Experimental
    default boolean hasRealClass() {
        return delegate().hasRealClass();
    }

    default void deleteValueMetadata() {
        delegate().deleteValueMetadata();
    }

    default void clearParent() {
        delegate().clearParent();
    }

    default boolean equals(PrismValue otherValue, @NotNull EquivalenceStrategy strategy) {
        return delegate().equals(otherValue, strategy);
    }

    default boolean equals(PrismValue otherValue, @NotNull ParameterizedEquivalenceStrategy strategy) {
        return delegate().equals(otherValue, strategy);
    }

    default @NotNull Collection<PrismValue> getAllValues(ItemPath path) {
        return delegate().getAllValues(path);
    }

    default void setUserData(@NotNull String key, Object value) {
        delegate().setUserData(key, value);
    }

    default void setParent(Itemable parent) {
        delegate().setParent(parent);
    }

    default @NotNull ValueMetadata getValueMetadata() {
        return delegate().getValueMetadata();
    }

    default @Nullable ValueMetadata getValueMetadataIfExists() {
        return delegate().getValueMetadataIfExists();
    }

    @Experimental
    default void setTransient(boolean value) {
        delegate().setTransient(value);
    }

    default boolean isEmpty() {
        return delegate().isEmpty();
    }

    default void applyDefinitionLegacy(@NotNull ItemDefinition<?> definition, boolean force) throws SchemaException {
        delegate().applyDefinitionLegacy(definition, force);
    }

    @Experimental
    @Nullable
    default Object getRealValueOrRawType() {
        return delegate().getRealValueOrRawType();
    }

    @Experimental
    default @NotNull <C extends Containerable> PrismContainer<C> getValueMetadataAsContainer() {
        return delegate().getValueMetadataAsContainer();
    }

    default SchemaContext getSchemaContext() {
        return delegate().getSchemaContext();
    }

    default <T> @Nullable T getNearestValueOfType(@NotNull Class<T> type) {
        return delegate().getNearestValueOfType(type);
    }

    default @NotNull PrismValue cloneIfImmutable() {
        return delegate().cloneIfImmutable();
    }

    default Collection<? extends ItemDelta> diff(PrismValue otherValue, ParameterizedEquivalenceStrategy strategy) {
        return delegate().diff(otherValue, strategy);
    }

    @Nullable
    default Class<?> getRealClass() {
        return delegate().getRealClass();
    }

    default @NotNull Collection<Item<?, ?>> getAllItems(@NotNull ItemPath path) {
        return delegate().getAllItems(path);
    }

    default boolean hasValueMetadata() {
        return delegate().hasValueMetadata();
    }

    @Experimental
    default void setValueMetadata(ValueMetadata valueMetadata) {
        delegate().setValueMetadata(valueMetadata);
    }

    @Experimental
    default void setValueMetadata(PrismContainer<?> valueMetadata) {
        delegate().setValueMetadata(valueMetadata);
    }

    @Experimental
    default void setValueMetadata(Containerable realValue) throws SchemaException {
        delegate().setValueMetadata(realValue);
    }

    default int hashCode(@NotNull ParameterizedEquivalenceStrategy equivalenceStrategy) {
        return delegate().hashCode(equivalenceStrategy);
    }

    default Object find(ItemPath path) {
        return delegate().find(path);
    }

    default boolean isObjectable() {
        return delegate().isObjectable();
    }

    default PrismContainerValue<?> getParentContainerValue() {
        return delegate().getParentContainerValue();
    }

    default @NotNull ItemPath getPath() {
        return delegate().getPath();
    }

    default void applyDefinitionLegacy(@NotNull ItemDefinition<?> definition) throws SchemaException {
        delegate().applyDefinitionLegacy(definition);
    }

    default void checkConsistenceInternal(Itemable rootItem, boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope) {
        delegate().checkConsistenceInternal(rootItem, requireDefinitions, prohibitRaw, scope);
    }

    default int hashCode(@NotNull EquivalenceStrategy equivalenceStrategy) {
        return delegate().hashCode(equivalenceStrategy);
    }

    default boolean representsSameValue(PrismValue other, EquivalenceStrategy strategy, boolean lax) {
        return delegate().representsSameValue(other, strategy, lax);
    }

    default void recompute() {
        delegate().recompute();
    }

    default String toHumanReadableString() {
        return delegate().toHumanReadableString();
    }

    default void setId(Long id) {
        delegate().setId(id);
    }

    default <T> @Nullable T getRealValue() {
        return delegate().getRealValue();
    }

    @Override
    default void walk(BiPredicate<? super ItemPath, Boolean> descendPredicate, Predicate<? super ItemPath> consumePredicate,
            Consumer<? super Item<?, ?>> itemConsumer) throws SchemaException {
        delegate().walk(descendPredicate, consumePredicate, itemConsumer);
    }

}
