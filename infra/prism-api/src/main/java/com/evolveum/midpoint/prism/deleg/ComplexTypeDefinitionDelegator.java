/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.deleg;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismReferenceDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;

import com.evolveum.midpoint.prism.annotation.ItemDiagramSpecification;

public interface ComplexTypeDefinitionDelegator extends TypeDefinitionDelegator, ComplexTypeDefinition {

    @Override
    ComplexTypeDefinition delegate();

    @Override
    @NotNull
    default QName getTypeName() {
        return delegate().getTypeName();
    }

    @Override
    @Nullable
    default QName getSuperType() {
        return delegate().getSuperType();
    }

    @Override
    default String getHelp() {
        return delegate().getHelp();
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
    default @Nullable QName getExtensionForType() {
        return delegate().getExtensionForType();
    }

    @Override
    default boolean isReferenceMarker() {
        return delegate().isReferenceMarker();
    }

    @Override
    default boolean isContainerMarker() {
        return delegate().isContainerMarker();
    }

    @Override
    default <ID extends ItemDefinition<?>> ID findItemDefinition(@NotNull ItemPath path, @NotNull Class<ID> clazz) {
        return delegate().findItemDefinition(path, clazz);
    }

    @Override
    default <ID extends ItemDefinition<?>> ID findItemDefinition(@NotNull ItemPath path) {
        return delegate().findItemDefinition(path);
    }

    @Override
    default boolean isObjectMarker() {
        return delegate().isObjectMarker();
    }

    @Override
    default PrismReferenceDefinition findReferenceDefinition(@NotNull ItemPath path) {
        return delegate().findReferenceDefinition(path);
    }

    @Override
    default boolean isXsdAnyMarker() {
        return delegate().isXsdAnyMarker();
    }

    @Override
    default <C extends Containerable> PrismContainerDefinition<C> findContainerDefinition(@NotNull ItemPath path) {
        return delegate().findContainerDefinition(path);
    }

    @Override
    default boolean isListMarker() {
        return delegate().isListMarker();
    }

    @Override
    default @Nullable String getDefaultNamespace() {
        return delegate().getDefaultNamespace();
    }

    @Override
    default @NotNull List<String> getIgnoredNamespaces() {
        return delegate().getIgnoredNamespaces();
    }

    @Override
    default void merge(ComplexTypeDefinition otherComplexTypeDef) {
        delegate().merge(otherComplexTypeDef);
    }

    @Override
    default boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    default void trimTo(@NotNull Collection<ItemPath> paths) {
        delegate().trimTo(paths);
    }

    @Override
    default boolean hasSubstitutions() {
        return delegate().hasSubstitutions();
    }

    @Override
    default Optional<ItemDefinition<?>> substitution(QName name) {
        return delegate().substitution(name);
    }

    @Override
    default Optional<ItemDefinition<?>> itemOrSubstitution(QName name) {
        return delegate().itemOrSubstitution(name);
    }

    @Override
    default @NotNull List<? extends ItemDefinition<?>> getDefinitions() {
        return delegate().getDefinitions();
    }

    @Override
    default List<ItemDiagramSpecification> getDiagrams() {
        return delegate().getDiagrams();
    }

    @Override
    default boolean hasOperationalOnlyItems() {
        return delegate().hasOperationalOnlyItems();
    }

}
