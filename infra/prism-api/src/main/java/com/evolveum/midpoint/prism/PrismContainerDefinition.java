/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.delta.ContainerDelta;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Definition of a prism container.
 *
 * Note: a lot is delegated to a {@link ComplexTypeDefinition}.
 */
public interface PrismContainerDefinition<C extends Containerable>
        extends ItemDefinition<PrismContainer<C>>, LocalItemDefinitionStore {

    /**
     * Static (compile-time) class holding the container values.
     *
     * May be null. (Let's not mark it as @Nullable to avoid lots of warnings.)
     */
    Class<C> getCompileTimeClass();

    /**
     * Definition of the container values.
     *
     * May be null. (Let's not mark it as @Nullable to avoid lots of warnings.)
     *
     * Note that individual values can hold their own (more specific) complex type definitions.
     */
    ComplexTypeDefinition getComplexTypeDefinition();

    /**
     * Returns a list of item definitions in this container.
     *
     * It is intentionally a {@link List} because it is ordered. (To provide standard format for serialization.)
     *
     * Usually obtained from {@link ComplexTypeDefinition}. So please do not modify the content of the list!
     */
    @Override
    @NotNull List<? extends ItemDefinition<?>> getDefinitions();

    /**
     * Returns names of items that are defined within this container definition. They do NOT include items that can be put into
     * instantiated container by means of "xsd:any" mechanism.
     */
    default Collection<ItemName> getItemNames() {
        return getDefinitions().stream()
                .map(ItemDefinition::getItemName)
                .collect(Collectors.toSet());
    }

    /**
     * Returns true if the instantiated container can contain only items that are explicitly defined here.
     */
    default boolean isCompletelyDefined() {
        ComplexTypeDefinition complexTypeDefinition = getComplexTypeDefinition();
        return complexTypeDefinition != null && !complexTypeDefinition.isXsdAnyMarker();
    }

    /**
     * Returns set of property definitions.
     *
     * The set contains all property definitions of all types that were parsed.
     * Order of definitions is insignificant.
     *
     * The returned set is immutable! All changes may be lost.
     *
     * @return set of definitions
     */
    List<PrismPropertyDefinition<?>> getPropertyDefinitions();

    @Override
    @NotNull
    ContainerDelta<C> createEmptyDelta(ItemPath path);

    @NotNull
    @Override
    PrismContainerDefinition<C> clone();

    /**
     * TODO
     */
    PrismContainerDefinition<C> cloneWithReplacedDefinition(QName itemName, ItemDefinition<?> newDefinition);

    /**
     * TODO
     */
    void replaceDefinition(QName itemName, ItemDefinition<?> newDefinition);

    /**
     * TODO
     */
    PrismContainerValue<C> createValue();

    /**
     * TODO
     */
    boolean isEmpty();

    /**
     * TODO
     */
    boolean canRepresent(@NotNull QName type);

    @Override
    MutablePrismContainerDefinition<C> toMutable();

    @Override
    Class<C> getTypeClass();

    @Override
    default Optional<ComplexTypeDefinition> structuredType() {
        return Optional.ofNullable(getComplexTypeDefinition());
    }
}
