/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.schemaContext.resolver.SchemaContextResolver;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.impl.schemaContext.SchemaContextImpl;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

public class TypeContextResolver implements SchemaContextResolver {

    private final SchemaContextDefinition definition;

    public TypeContextResolver(@NotNull SchemaContextDefinition definition) {
        this.definition = definition;
    }

    @Override
    public SchemaContext computeContext(PrismValue prismValue) {
        SchemaRegistry registry = PrismContext.get().getSchemaRegistry();
        PrismObjectDefinition<?> objDef = registry.findObjectDefinitionByType(definition.getType());
        if (definition.getPath() == null) {
            return new SchemaContextImpl(objDef);
        }
        var def = objDef.findItemDefinition(definition.getPath());
        return new SchemaContextImpl(def);
    }
}
