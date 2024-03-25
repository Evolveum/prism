/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.schema;

import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Schema as a collection of definitions. This is a midPoint-specific view of
 * schema definition. It is just a collection of definitions grouped under a
 * specific namespace.
 *
 * The schema and all the public classes in this package define a schema
 * meta-model. It is supposed to be used for run-time schema interpretation. It
 * will not be a convenient tool to work with static data model objects such as
 * user or role. But it is needed for interpreting dynamic schemas for resource
 * objects, extensions and so on.
 *
 * @author semancik
 */
public interface PrismSchema
        extends DebugDumpable, GlobalDefinitionsStore, DefinitionSearchImplementation,
        Freezable, Cloneable {

    /** All top-level definitions (types, items) are in this namespace. */
    @NotNull String getNamespace();

    /**
     * Returns all definitions: both types and items. Their order is insignificant.
     *
     * The collection is unmodifiable. The returned value should not be used for looking up specific definitions,
     * as there may be thousands of them. Use more specific lookup methods instead.
     */
    @NotNull Collection<Definition> getDefinitions();

    default int size() {
        return getDefinitions().size();
    }

    default boolean isEmpty() {
        return getDefinitions().isEmpty();
    }

    static boolean isNullOrEmpty(PrismSchema schema) {
        return schema == null || schema.isEmpty();
    }

    static boolean isNotEmpty(PrismSchema schema) {
        return !isNullOrEmpty(schema);
    }

    /**
     * Returns a collection of definitions of a given type. Similar to {@link #getDefinitions()}.
     */
    <T extends Definition> @NotNull List<T> getDefinitions(@NotNull Class<T> type);

    default @NotNull List<? extends PrismObjectDefinition<?>> getObjectDefinitions() {
        //noinspection unchecked,RedundantCast,rawtypes
        return (List<? extends PrismObjectDefinition<?>>) (List) getDefinitions(PrismObjectDefinition.class);
    }

    default @NotNull List<ComplexTypeDefinition> getComplexTypeDefinitions() {
        return getDefinitions(ComplexTypeDefinition.class);
    }

    @NotNull Document serializeToXsd() throws SchemaException;

    Multimap<QName, ItemDefinition<?>> getSubstitutions();

    PrismSchemaMutator mutator();

    SchemaBuilder builder();

    /** Object that allows modifying a {@link PrismSchema} - unless it's immutable. */
    interface PrismSchemaMutator {

        /** Adds any definition (item or type). */
        void add(@NotNull Definition def);

    }
}
