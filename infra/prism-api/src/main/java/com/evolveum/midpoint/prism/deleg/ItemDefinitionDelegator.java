/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.deleg;

import java.util.List;
import java.util.Optional;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.annotation.ItemDiagramSpecification;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.exception.SchemaException;

public interface ItemDefinitionDelegator<I extends Item<?,?>>
        extends DefinitionDelegator, ItemDefinition<I>, PrismItemBasicDefinition {

    @Override
    ItemDefinition<I> delegate();

    @Override
    default @NotNull ItemName getItemName() {
        return delegate().getItemName();
    }

    @Override
    default @NotNull QName getTypeName() {
        return delegate().getTypeName();
    }

    @Override
    default String getHelp() {
        return delegate().getHelp();
    }

    @Override
    default boolean canRead() {
        return delegate().canRead();
    }

    @Override
    default int getMinOccurs() {
        return delegate().getMinOccurs();
    }

    @Override
    default int getMaxOccurs() {
        return delegate().getMaxOccurs();
    }

    @Override
    default boolean isOperational() {
        return delegate().isOperational();
    }

    @Override
    default boolean isIndexOnly() {
        return delegate().isIndexOnly();
    }

    @Override
    default Boolean isIndexed() {
        return delegate().isIndexed();
    }

    @Override
    default boolean canModify() {
        return delegate().canModify();
    }

    @Override
    default boolean isInherited() {
        return delegate().isInherited();
    }

    @Override
    default boolean isDynamic() {
        return delegate().isDynamic();
    }

    @Override
    default boolean canAdd() {
        return delegate().canAdd();
    }

    @Override
    default List<ItemDiagramSpecification> getDiagrams() {
        return delegate().getDiagrams();
    }

    @Override
    default QName getSubstitutionHead() {
        return delegate().getSubstitutionHead();
    }

    @Override
    default boolean isHeterogeneousListItem() {
        return delegate().isHeterogeneousListItem();
    }

    @Override
    default PrismReferenceValue getValueEnumerationRef() {
        return delegate().getValueEnumerationRef();
    }

    @Override
    default boolean isValidFor(@NotNull QName elementQName, @NotNull Class<? extends ItemDefinition<?>> clazz,
            boolean caseInsensitive) {
        return delegate().isValidFor(elementQName, clazz, caseInsensitive);
    }

    @Override
    default @NotNull I instantiate() throws SchemaException {
        return delegate().instantiate();
    }

    @Override
    default @NotNull I instantiate(QName name) throws SchemaException {
        return delegate().instantiate(name);
    }

    @Override
    default <T extends ItemDefinition<?>> T findItemDefinition(@NotNull ItemPath path, @NotNull Class<T> clazz) {
        //noinspection unchecked
        return LivePrismItemDefinition.matchesThisDefinition(path, clazz, this) ? (T) this : null;
    }

    @Override
    default @NotNull ItemDelta<?, ?> createEmptyDelta(ItemPath path) {
        return delegate().createEmptyDelta(path);
    }

    @Override
    default ItemDefinition<I> deepClone(@NotNull DeepCloneOperation operation) {
        return delegate().deepClone(operation);
    }

    @Override
    default void debugDumpShortToString(StringBuilder sb) {
        delegate().debugDumpShortToString(sb);
    }

    @Override
    default Optional<ComplexTypeDefinition> structuredType() {
        return delegate().structuredType();
    }

    @Override
    default boolean isSearchable() {
        return delegate().isSearchable();
    }
}
