/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.path.ItemName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.path.ItemPath;

import java.util.Collection;

/**
 * Used to retrieve item definition from 'local definition store' - i.e. store that contains definition(s)
 * related to one parent item.
 *
 * Such stores are prism containers and complex types (and their subtypes like attribute containers or object class definitions).
 *
 * Methods `findLocalItemDefinition(...)` never try to resolve item names globally in the schema registry.
 * On the other hand, path-based methods do that if they come across `xsd:any`-type container during the resolution.
 *
 * Note: Although these methods can return null, they are not marked as `@Nullable`. It is because we want avoid false warnings
 * about possible NPEs when used e.g. to find definitions that certainly exist (like `c:user` etc).
 *
 * TODO What to do with "ID extends ItemDefinition"? It looks pretty bad.
 */
public interface LocalItemDefinitionStore {

    /**
     * Returns all item definitions in this store.
     */
    @NotNull Collection<? extends ItemDefinition<?>> getDefinitions();

    /**
     * Returns the local item definition corresponding to given item name (optionally case-insensitive)
     * and definition class.
     *
     * Does not try to resolve items globally (in the case of "any" content).
     *
     * BEWARE: In the case of ambiguities, returns any suitable definition. (This may change.)
     */
     default <ID extends ItemDefinition<?>> ID findLocalItemDefinition(
            @NotNull QName name, @NotNull Class<ID> clazz, boolean caseInsensitive) {
        for (ItemDefinition<?> def : getDefinitions()) {
            if (def.isValidFor(name, clazz, caseInsensitive)) {
                //noinspection unchecked
                return (ID) def;
            }
        }
        return null;
    }

    /**
     * Returns the local {@link ItemDefinition} corresponding to given item name (in case-sensitive manner).
     *
     * Does not try to resolve items globally (in the case of "any" content).
     *
     * Note: some implementors provide optimized implementations of this method.
     */
    @SuppressWarnings("unchecked")
    default <ID extends ItemDefinition<?>> ID findLocalItemDefinition(@NotNull QName name) {
        //noinspection unchecked
        return (ID) findLocalItemDefinition(name, ItemDefinition.class, false);
    }

    /**
     * Returns {@link ItemDefinition} corresponding to given path (rooted at this store).
     *
     * Tries the global resolution in the case of "any" content.
     */
    @SuppressWarnings("unchecked")
    default <ID extends ItemDefinition<?>> ID findItemDefinition(@NotNull ItemPath path) {
        return (ID) findItemDefinition(path, ItemDefinition.class);
    }

    /**
     * Returns {@link PrismPropertyDefinition} corresponding to given path (rooted at this store).
     *
     * Tries the global resolution in the case of "any" content.
     */
    @SuppressWarnings("unchecked")
    default <T> PrismPropertyDefinition<T> findPropertyDefinition(@NotNull ItemPath path) {
        return findItemDefinition(path, PrismPropertyDefinition.class);
    }

    /**
     * Returns {@link PrismReferenceDefinition} corresponding to given path (rooted at this store).
     *
     * Tries the global resolution in the case of "any" content.
     */
    default PrismReferenceDefinition findReferenceDefinition(@NotNull ItemPath path) {
        return findItemDefinition(path, PrismReferenceDefinition.class);
    }

    /**
     * Returns {@link PrismContainerDefinition} corresponding to given path (rooted at this store).
     *
     * Tries the global resolution in the case of "any" content.
     */
    @SuppressWarnings("unchecked")
    default <C extends Containerable> PrismContainerDefinition<C> findContainerDefinition(@NotNull ItemPath path) {
        return findItemDefinition(path, PrismContainerDefinition.class);
    }

    /**
     * Returns a definition of given type corresponding to given path (rooted at this store).
     *
     * Tries the global resolution in the case of "any" content.
     */
    <ID extends ItemDefinition<?>> ID findItemDefinition(@NotNull ItemPath path, @NotNull Class<ID> clazz);

    /**
     * Returns true if the store contains a definition of an item with given name.
     *
     * TODO what about global names? Current implementation resolves them, but is this expected by clients?
     */
    default boolean containsItemDefinition(@NotNull QName itemName) {
        return findItemDefinition(ItemName.fromQName(itemName)) != null;
    }
}
