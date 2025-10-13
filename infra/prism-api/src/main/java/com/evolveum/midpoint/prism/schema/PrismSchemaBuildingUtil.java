/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.PrismContainerDefinition;

import com.evolveum.midpoint.prism.PrismContext;

import com.evolveum.midpoint.prism.PrismPropertyDefinition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import javax.xml.namespace.QName;
import java.util.Objects;

/** Methods that help non-standard (lazy?) clients with schema building. Moved here to avoid polluting the standard interfaces. */
public class PrismSchemaBuildingUtil {

    /**
     * Creates a new top-level {@link PrismContainerDefinition} and adds it to the schema. It tries to find
     * a {@link ComplexTypeDefinition} for the specified type name, and creates (and adds) an empty one if it doesn't exist.
     *
     * @param localItemName item (container) name "relative" to schema namespace
     * @param localTypeName type name "relative" to schema namespace
     * @return new container definition
     */
    public static PrismContainerDefinition<?> addNewContainerDefinition(
            @NotNull PrismSchema schema, @NotNull String localItemName, @NotNull String localTypeName) {
        QName typeName = qualify(schema, localTypeName);
        QName itemName = qualify(schema, localItemName);
        ComplexTypeDefinition ctd =
                Objects.requireNonNullElseGet(
                        schema.findComplexTypeDefinitionByType(typeName),
                        () -> addNewComplexTypeDefinition(schema, localTypeName));
        PrismContainerDefinition<?> itemDef = PrismContext.get().definitionFactory().newContainerDefinition(itemName, ctd);
        schema.mutator().add(itemDef);
        return itemDef;
    }

    /** Creates and adds a new {@link ComplexTypeDefinition}. */
    public static ComplexTypeDefinition addNewComplexTypeDefinition(@NotNull PrismSchema schema, @NotNull String localTypeName) {
        var ctd = PrismContext.get().definitionFactory().newComplexTypeDefinition(qualify(schema, localTypeName));
        schema.mutator().add(ctd);
        return ctd;
    }

    private static QName qualify(PrismSchema schema, @NotNull String localTypeName) {
        return new QName(schema.getNamespace(), localTypeName);
    }

    /**
     * Creates a top-level property definition and adds it to the schema.
     *
     * This is a preferred way how to create definition in the schema.
     *
     * @param localItemName element name "relative" to schema namespace
     * @param typeName XSD type name of the element
     * @return new property definition
     */
    @SuppressWarnings("UnusedReturnValue")
    @VisibleForTesting
    public static PrismPropertyDefinition<?> addNewPropertyDefinition(PrismSchema schema, String localItemName, QName typeName) {
        var def = PrismContext.get().definitionFactory().newPropertyDefinition(
                qualify(schema, localItemName),
                typeName);
        schema.mutator().add(def);
        return def;
    }
}
