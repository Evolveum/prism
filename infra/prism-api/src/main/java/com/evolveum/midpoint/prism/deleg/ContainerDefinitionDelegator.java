/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.deleg;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.delta.ContainerDelta;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;

public interface ContainerDefinitionDelegator<C extends Containerable>
        extends ItemDefinitionDelegator<PrismContainer<C>>, PrismContainerDefinition<C> {

    @Override
    PrismContainerDefinition<C> delegate();

    default @NotNull QName getTypeName() {
        return delegate().getTypeName();
    }

    @Override
    default String getDisplayName() {
        return delegate().getDisplayName();
    }

    @Override
    default Integer getDisplayOrder() {
        return delegate().getDisplayOrder();
    }

    @Override
    default boolean isEmphasized() {
        return delegate().isEmphasized();
    }

    @Override
    default Class<C> getCompileTimeClass() {
        return delegate().getCompileTimeClass();
    }

    @Override
    default ComplexTypeDefinition getComplexTypeDefinition() {
        return delegate().getComplexTypeDefinition();
    }

    @Override
    default @NotNull List<? extends ItemDefinition<?>> getDefinitions() {
        return delegate().getDefinitions();
    }

    @Override
    default Collection<ItemName> getItemNames() {
        return delegate().getItemNames();
    }

    @Override
    default List<PrismPropertyDefinition<?>> getPropertyDefinitions() {
        return delegate().getPropertyDefinitions();
    }

    @Override
    default <T extends ItemDefinition<?>> T findItemDefinition(@NotNull ItemPath path, @NotNull Class<T> clazz) {
        return delegate().findItemDefinition(path, clazz);
    }

    @Override
    default PrismContainerDefinition<C> cloneWithNewDefinition(QName newItemName, ItemDefinition<?> newDefinition) {
        return delegate().cloneWithNewDefinition(newItemName, newDefinition);
    }

    @Override
    default void replaceDefinition(QName itemName, ItemDefinition<?> newDefinition) {
        delegate().replaceDefinition(itemName, newDefinition);
    }

    @Override
    default PrismContainerValue<C> createValue() {
        return delegate().createValue();
    }

    @Override
    default boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    default boolean canRepresent(@NotNull QName type) {
        return delegate().canRepresent(type);
    }

    @Override
    default <C2 extends Containerable> PrismContainerDefinition<C2> findContainerDefinition(@NotNull ItemPath path) {
        return delegate().findContainerDefinition(path);
    }

    @Override
    default @NotNull ContainerDelta<C> createEmptyDelta(ItemPath path) {
        return delegate().createEmptyDelta(path);
    }

    @Override
    default Class<C> getTypeClass() {
        return delegate().getTypeClass();
    }

    @Override
    default Optional<ComplexTypeDefinition> structuredType() {
        return delegate().structuredType();
    }

}
