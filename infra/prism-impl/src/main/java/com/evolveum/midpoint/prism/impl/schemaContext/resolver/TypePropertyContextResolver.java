/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.schemaContext.SchemaContextImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;
import com.evolveum.midpoint.prism.schemaContext.resolver.SchemaContextResolver;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class TypePropertyContextResolver implements SchemaContextResolver {

    private final SchemaContextDefinition schemaContextDefinition;

    public TypePropertyContextResolver(SchemaContextDefinition schemaContextDefinition) {
        this.schemaContextDefinition = schemaContextDefinition;
    }

    @Override
    public SchemaContext computeContext(PrismValue prismValue) {
        if (prismValue instanceof PrismContainerValue<?> container) {
            var typeProp = container.findItem(ItemPath.create(schemaContextDefinition.getTypePath()), PrismProperty.class);

            if (typeProp != null && typeProp.getAnyValue() != null) {
                if (typeProp.getAnyValue().getRealValue() instanceof QName typeName) {
                    PrismObjectDefinition<?> objectDefinition = container.schemaLookup().findObjectDefinitionByType(typeName);
                    return new SchemaContextImpl(objectDefinition);
                }
            }
        }

        return null;
    }
}
