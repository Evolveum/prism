/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.impl.schemaContext.SchemaContextImpl;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;
import com.evolveum.midpoint.prism.schemaContext.resolver.SchemaContextResolver;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

/** Provides context based on `typePath` property in given container value. */
public class TypePropertyContextResolver implements SchemaContextResolver {

    private final SchemaContextDefinition schemaContextDefinition;

    public TypePropertyContextResolver(SchemaContextDefinition schemaContextDefinition) {
        this.schemaContextDefinition = schemaContextDefinition;
    }

    @Override
    public SchemaContext computeContext(@NotNull PrismValue prismValue) {
        if (prismValue instanceof PrismContainerValue<?> container) {
            var typeProp = container.findProperty(schemaContextDefinition.getTypePath());
            var typeRealValue = typeProp != null ? typeProp.getAnyRealValue() : null; // TODO what if there are more values?
            if (typeRealValue instanceof QName typeName) {
                PrismObjectDefinition<?> objectDefinition = container.schemaLookup().findObjectDefinitionByType(typeName);
                return new SchemaContextImpl(objectDefinition);
            }
        }

        return null;
    }
}
