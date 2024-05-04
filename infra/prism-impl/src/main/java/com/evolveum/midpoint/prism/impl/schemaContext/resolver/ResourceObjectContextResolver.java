package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

/**
 * Created by Dominik.
 */
public class ResourceObjectContextResolver implements SchemaContextResolver {

    SchemaContextDefinition schemaContextDefinition;

    public ResourceObjectContextResolver(SchemaContextDefinition schemaContextDefinition) {
        this.schemaContextDefinition = schemaContextDefinition;
    }

    @Override
    public SchemaContext computeContext(PrismValue prismValue) {
        return null;
    }
}
