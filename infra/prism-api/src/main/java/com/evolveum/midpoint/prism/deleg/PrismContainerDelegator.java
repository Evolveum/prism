/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.deleg;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ContainerDelta;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;

public interface PrismContainerDelegator<C extends Containerable> extends PrismContainer<C>, ItemDelegator<PrismContainerValue<C>, PrismContainerDefinition<C>> {

    @Override
    PrismContainer<C> delegate();

    @Override
    default PrismContainerDefinition<C> getDefinition() {
        return delegate().getDefinition();
    }

    @Override
    default @Nullable Class<C> getCompileTimeClass() {
        return delegate().getCompileTimeClass();
    }

    @Override
    default boolean canRepresent(@NotNull Class<?> compileTimeClass) {
        return delegate().canRepresent(compileTimeClass);
    }

    @Override
    default boolean canRepresent(QName type) {
        return delegate().canRepresent(type);
    }

    @Override
    default @NotNull Collection<C> getRealValues() {
        return delegate().getRealValues();
    }

    @Override
    default @NotNull C getRealValue() {
        return delegate().getRealValue();
    }

    @Override
    default void setRealValue(C value) throws SchemaException {
        delegate().setRealValue(value);
    }

    @Override
    default void setValue(@NotNull PrismContainerValue<C> value) throws SchemaException {
        delegate().setValue(value);
    }

    @Override
    default @NotNull PrismContainerValue<C> getValue() {
        return delegate().getValue();
    }

    @Override
    default PrismContainerValue<C> getValue(Long id) {
        return delegate().getValue(id);
    }

    @Override
    default @NotNull List<PrismContainerValue<C>> getValues() {
        return delegate().getValues();
    }

    @Override
    default <T> void setPropertyRealValue(QName propertyName, T realValue) throws SchemaException {
        delegate().setPropertyRealValue(propertyName, realValue);
    }

    @Override
    default <C1 extends Containerable> void setContainerRealValue(QName containerName, C1 realValue) throws SchemaException {
        delegate().setContainerRealValue(containerName, realValue);
    }

    @Override
    default <T> void setPropertyRealValues(QName propertyName, T... realValues) throws SchemaException {
        delegate().setPropertyRealValues(propertyName, realValues);
    }

    @Override
    default <T> T getPropertyRealValue(ItemPath propertyPath, Class<T> type) {
        return delegate().getPropertyRealValue(propertyPath, type);
    }

    @Override
    default void add(Item<?, ?> item) throws SchemaException {
        delegate().add(item);
    }

    @Override
    default PrismContainerValue<C> createNewValue() {
        return delegate().createNewValue();
    }

    @Override
    default void mergeValues(PrismContainer<C> other) throws SchemaException {
        delegate().mergeValues(other);
    }

    @Override
    default void mergeValues(Collection<PrismContainerValue<C>> otherValues) throws SchemaException {
        delegate().mergeValues(otherValues);
    }

    @Override
    default void mergeValue(PrismContainerValue<C> otherValue) throws SchemaException {
        delegate().mergeValue(otherValue);
    }

    @Override
    default void trim() {
        delegate().trim();
    }

    @Deprecated
    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findItem(QName itemQName, Class<I> type) {
        return delegate().findItem(itemQName, type);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> PartiallyResolvedItem<IV, ID> findPartial(ItemPath path) {
        return delegate().findPartial(path);
    }

    @Deprecated
    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findCreateItem(QName itemQName, Class<I> type, boolean create) throws SchemaException {
        return delegate().findCreateItem(itemQName, type, create);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findItem(ItemPath path, Class<I> type) {
        return delegate().findItem(path, type);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> findItem(ItemPath path) {
        return delegate().findItem(path);
    }

    @Override
    default boolean containsItem(ItemPath itemPath, boolean acceptEmptyItem) throws SchemaException {
        return delegate().containsItem(itemPath, acceptEmptyItem);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findCreateItem(ItemPath itemPath, Class<I> type, ID itemDefinition, boolean create) throws SchemaException {
        return delegate().findCreateItem(itemPath, type, itemDefinition, create);
    }

    @Override
    default PrismContainerValue<C> findValue(long id) {
        return delegate().findValue(id);
    }

    @Override
    default <T extends Containerable> PrismContainer<T> findContainer(ItemPath path) {
        return delegate().findContainer(path);
    }

    @Override
    default <T> PrismProperty<T> findProperty(ItemPath path) {
        return delegate().findProperty(path);
    }

    @Override
    default PrismReference findReference(ItemPath path) {
        return delegate().findReference(path);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findOrCreateItem(ItemPath containerPath, Class<I> type) throws SchemaException {
        return delegate().findOrCreateItem(containerPath, type);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findOrCreateItem(ItemPath containerPath, Class<I> type, ID definition) throws SchemaException {
        return delegate().findOrCreateItem(containerPath, type, definition);
    }

    @Override
    default <T extends Containerable> PrismContainer<T> findOrCreateContainer(ItemPath containerPath) throws SchemaException {
        return delegate().findOrCreateContainer(containerPath);
    }

    @Override
    default <T> PrismProperty<T> findOrCreateProperty(ItemPath propertyPath) throws SchemaException {
        return delegate().findOrCreateProperty(propertyPath);
    }

    @Override
    default PrismReference findOrCreateReference(ItemPath propertyPath) throws SchemaException {
        return delegate().findOrCreateReference(propertyPath);
    }

    @Override
    default void remove(Item<?, ?> item) {
        delegate().remove(item);
    }

    @Override
    default void removeProperty(ItemPath path) {
        delegate().removeProperty(path);
    }

    @Override
    default void removeContainer(ItemPath path) {
        delegate().removeContainer(path);
    }

    @Override
    default void removeReference(ItemPath path) {
        delegate().removeReference(path);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> void removeItem(ItemPath path, Class<I> itemType) {
        delegate().removeItem(path, itemType);
    }

    @Override
    default ContainerDelta<C> createDelta() {
        return delegate().createDelta();
    }

    @Override
    default ContainerDelta<C> createDelta(ItemPath path) {
        return delegate().createDelta(path);
    }

    @Override
    default ContainerDelta<C> diff(PrismContainer<C> other) {
        return delegate().diff(other);
    }

    @Override
    default ContainerDelta<C> diff(PrismContainer<C> other, ParameterizedEquivalenceStrategy strategy) {
        return delegate().diff(other, strategy);
    }

    @Override
    default List<? extends ItemDelta> diffModifications(PrismContainer<C> other, ParameterizedEquivalenceStrategy strategy) {
        return delegate().diffModifications(other, strategy);
    }

    @Override
    default PrismContainer<C> clone() {
        return delegate().clone();
    }

    @Override
    default PrismContainer<C> createImmutableClone() {
        return delegate().createImmutableClone();
    }

    @Override
    default @NotNull PrismContainer<C> cloneComplex(@NotNull CloneStrategy strategy) {
        return delegate().cloneComplex(strategy);
    }

    @Override
    default PrismContainerDefinition<C> deepCloneDefinition(@NotNull DeepCloneOperation operation) {
        return delegate().deepCloneDefinition(operation);
    }

    @Override
    default void accept(Visitor visitor, ItemPath path, boolean recursive) {
        delegate().accept(visitor, path, recursive);
    }

    @Override
    default boolean equivalent(Object obj) {
        return delegate().equivalent(obj);
    }

    @Override
    default void trimDefinitionTree(Collection<? extends ItemPath> alwaysKeep) {
        delegate().trimDefinitionTree(alwaysKeep);
    }

    @Override
    default ComplexTypeDefinition getComplexTypeDefinition() {
        return delegate().getComplexTypeDefinition();
    }

}
