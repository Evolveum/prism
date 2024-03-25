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
import com.evolveum.midpoint.util.annotation.Experimental;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Collections;
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
     * The returned collection is immutable or detached from the source. Don't try to modify it.
     * It may fail or the changes may be lost.
     */
    List<PrismPropertyDefinition<?>> getPropertyDefinitions();

    @Override
    @NotNull
    ContainerDelta<C> createEmptyDelta(ItemPath path);

    @NotNull
    @Override
    PrismContainerDefinition<C> clone();

    /** Changes the type name and definition for this PCD. Use only in special cases. */
    @NotNull PrismContainerDefinition<?> cloneWithNewType(@NotNull QName newTypeName, @NotNull ComplexTypeDefinition newCtd);

    /**
     * TODO
     */
    PrismContainerDefinition<C> cloneWithNewDefinition(QName newItemName, ItemDefinition<?> newDefinition);

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
    PrismContainerDefinitionMutator<C> mutator();

    @Override
    Class<C> getTypeClass();

    @Override
    default Optional<ComplexTypeDefinition> structuredType() {
        return Optional.ofNullable(getComplexTypeDefinition());
    }

    /**
     * Returns true, if item is explicitly specified mentioned to be used for equals
     */
    @Experimental
    default boolean isAlwaysUseForEquals(QName name) {
        return getAlwaysUseForEquals().contains(name);
    }

    /**
     * Returns list of items which should be always used for equals, even if
     * they are operational, and equivalence strategy does not consider
     * operational data.
     */
    @Experimental
    default Collection<QName> getAlwaysUseForEquals() {
        return Collections.emptySet();
    }

    /**
     * The "createXXX" methods also add the new definition into this container.
     */
    interface PrismContainerDefinitionMutator<C extends Containerable>
            extends ItemDefinitionMutator {

        void setCompileTimeClass(Class<C> compileTimeClass);

        PrismPropertyDefinition<?> createPropertyDefinition(QName name, QName propType, int minOccurs, int maxOccurs);

        PrismPropertyDefinition<?> createPropertyDefinition(QName name, QName propType);

        PrismPropertyDefinition<?> createPropertyDefinition(String localName, QName propType);

        PrismContainerDefinition<?> createContainerDefinition(QName name, QName typeName, int minOccurs, int maxOccurs);

        PrismContainerDefinition<?> createContainerDefinition(
                @NotNull QName name, @NotNull ComplexTypeDefinition ctd, int minOccurs, int maxOccurs);

        void setComplexTypeDefinition(ComplexTypeDefinition complexTypeDefinition);

        /**
         * Experimental: Use only with care, this overrides behavior of listed operational=true items in equivalence strategies
         * for containers.
         */
        @Experimental
        default void setAlwaysUseForEquals(@NotNull Collection<QName> keysElem) {
            // NOOP
        }
    }
}
