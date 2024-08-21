/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.schema.SchemaBuilder;
import com.evolveum.midpoint.prism.schema.SchemaRegistryState;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

/** Temporary home for a bunch of "build me" methods. */
public class SchemaParsingUtil {

    /** A convenience method for schemas with no potential for circular item definitions. */
    public static PrismSchemaImpl createAndParse(@NotNull Element sourceXsdElement, boolean isRuntime, String shortDescription)
            throws SchemaException {
        return createAndParse(sourceXsdElement, isRuntime, shortDescription, false);
    }

    public static PrismSchemaImpl createAndParse(
            @NotNull Element sourceXsdElement, boolean isRuntime, String shortDescription, boolean allowDelayedItemDefinitions)
            throws SchemaException {
        // We need to synchronize, because the DOM structures are not thread-safe, even for reading.
        // Here, DOMUtil.getSchemaTargetNamespace gets an exception, see MID-8860.
        //
        // We intentionally synchronize on the schema element. Note that synchronizing e.g. on the owning ConnectorType object
        // is not sufficient, because of not cloning the embedded schema (59bee63b1b8eb933db39e8a9b61a4023b25ec4c0 - wrong
        // decision at that time) we usually have different connector objects (in parallel threads) sharing the same schema
        // DOM element.
        //
        // FIXME this should be resolved more seriously; maybe we will have to put the schema cloning back?
        //
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (sourceXsdElement) {
            PrismSchemaImpl schema = new PrismSchemaImpl(DOMUtil.getSchemaTargetNamespace(sourceXsdElement));
            parse(schema, sourceXsdElement, isRuntime, shortDescription, allowDelayedItemDefinitions);
            return schema;
        }
    }

    // main entry point for parsing standard prism schemas
    static void parseSchemas(
            Element sourceWrappingElement, List<PrismSchemaImpl> schemas, SchemaRegistryState schemaRegistryState)
            throws SchemaException {
        new SchemaDomParser()
                .parseSchemas(schemas, sourceWrappingElement, schemaRegistryState);
    }

    public static void parse(
            @NotNull SchemaBuilder schemaBuilder,
            @NotNull Element sourceXsdElement,
            boolean isRuntime,
            String shortDescription,
            boolean allowDelayedItemDefinitions) throws SchemaException {
        parse(schemaBuilder, sourceXsdElement, isRuntime, shortDescription, allowDelayedItemDefinitions, PrismContext.get().getDefaultSchemaLookup());
    }

    static void parse(
            @NotNull SchemaBuilder schemaBuilder,
            @NotNull Element sourceXsdElement,
            boolean isRuntime,
            String shortDescription,
            boolean allowDelayedItemDefinitions,
            SchemaRegistryState schemaRegistryState) throws SchemaException {
        new SchemaDomParser()
                .parseSchema(schemaBuilder, sourceXsdElement, isRuntime, allowDelayedItemDefinitions, shortDescription, schemaRegistryState);
    }
}
