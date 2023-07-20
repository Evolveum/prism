/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.util;

import java.io.Serializable;

import com.evolveum.midpoint.prism.PrismPropertyDefinition;

import com.evolveum.midpoint.prism.Structured;
import com.evolveum.midpoint.prism.polystring.PolyString;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Common supertype for both {@link ItemDeltaItem} and {@link ObjectDeltaObject}.
 *
 * They have less in common that originally expected; in particular, the latter is *not* a subtype (nor subclass) of the former.
 */
public interface AbstractItemDeltaItem<D extends ItemDefinition<?>> extends DebugDumpable, Serializable {

    /**
     * Is the IDI empty, i.e. is everything null?
     *
     * FIXME the implementation of this method may not be 100% precise. Should be reviewed.
     */
    boolean isNull();

    /** The definition of the item/object in question. */
    @NotNull D getDefinition();

    /** Recomputes the new state of the IDI. */
    void recompute() throws SchemaException;

    /** Finds a child IDI related to given `path`. */
    default <IV extends PrismValue, ID extends ItemDefinition<?>> ItemDeltaItem<IV,ID> findIdi(@NotNull ItemPath path)
            throws SchemaException {
        return findIdi(path, null);
    }

    /** As #findIdi(ItemPath) but with additional definition resolver that provides definitions for sub-items found. */
    <IV extends PrismValue, ID extends ItemDefinition<?>> ItemDeltaItem<IV,ID> findIdi(
            @NotNull ItemPath path, @Nullable DefinitionResolver<D, ID> additionalDefinitionResolver) throws SchemaException;

    /** Is the item in question a prism container? */
    boolean isContainer();

    /** Is the item in question a prism property? */
    boolean isProperty();

    /**
     * Is the item in question a structured property - {@link Structured}? I.e. a prism property that has visible components.
     * (E.g., a {@link PolyString}.)
     *
     * See {@link ItemDeltaItem#resolveStructuredProperty(ItemPath, PrismPropertyDefinition)}.
     */
    boolean isStructuredProperty();
}
