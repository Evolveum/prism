/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ComplexTypeDefinition.ComplexTypeDefinitionLikeBuilder;
import com.evolveum.midpoint.prism.Definition.DefinitionBuilder;

/** Builds both prism and non-prism schemas. */
public interface SchemaBuilder {

    /** Namespace of the schema that is being built. */
    @NotNull String getNamespace();

    /**
     * Returns the builder for complex type definition; does not add anything to the schema (yet).
     *
     * We use this one instead of simply creating a new {@link ComplexTypeDefinition} using the {@link DefinitionFactory}
     * in order to be able to build non-prism definitions, i.e., ones that are not of {@link Definition} type.
     */
    @NotNull ComplexTypeDefinitionLikeBuilder newComplexTypeDefinitionLikeBuilder(String localTypeName);

    /** Adds the definition corresponding to provided builder - potentially incomplete - to the schema. */
    void add(@NotNull DefinitionBuilder builder);

    /**
     * Some containers cannot be added to the schema immediately, because their CTD is not yet parsed.
     * This can occur when multiple schemas with circular dependencies are present. So we add them only
     * after all the schemas are parsed.
     */
    void addDelayedItemDefinition(ItemDefinitionSupplier supplier);

    /**
     * Returns existing type definition (if there's one) in the schema being built. This is used when types can be referenced
     * by other types, e.g., in the case of complex types. Its implementation is optional; currently, it is used only in
     * genuine prism schemas.
     */
    AbstractTypeDefinition findTypeDefinitionByType(@NotNull QName typeName);

    // TEMPORARY
    boolean isRuntime();
    void setRuntime(boolean value);
}
