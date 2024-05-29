package com.evolveum.midpoint.prism.impl.schemaContext;

import com.evolveum.midpoint.prism.impl.schemaContext.resolver.SchemaContextResolver;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

import javax.xml.namespace.QName;

public interface ContextResolverFactory {
    QName getAlgorithmName();
    SchemaContextResolver createResolver(SchemaContextDefinition schemaContext);
}
