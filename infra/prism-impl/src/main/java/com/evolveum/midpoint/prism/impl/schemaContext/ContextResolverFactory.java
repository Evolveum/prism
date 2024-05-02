package com.evolveum.midpoint.prism.impl.schemaContext;

import com.evolveum.midpoint.prism.impl.schemaContext.resolver.SchemaContextResolver;

import javax.xml.namespace.QName;

public interface ContextResolverFactory {
    QName getAlgorithmName();
//    SchemaContextResolver createResolver(SchemaContext schemaContext);
}
