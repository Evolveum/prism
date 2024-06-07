package com.evolveum.midpoint.prism.impl.schemaContext;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.impl.schemaContext.resolver.SchemaContextResolver;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

public interface ContextResolverFactory {

    QName getAlgorithmName();

    SchemaContextResolver createResolver(SchemaContextDefinition schemaContext);
}
