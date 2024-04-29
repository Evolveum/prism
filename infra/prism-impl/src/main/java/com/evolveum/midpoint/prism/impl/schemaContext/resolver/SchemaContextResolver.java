package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.schema.SchemaContextDefinition;

public interface SchemaContextResolver {
    SchemaContextDefinition computeContext(PrismValue prismValue);
}
