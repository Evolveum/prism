package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;

public interface SchemaContextResolver {
    SchemaContext computeContext(PrismValue prismValue);
}
