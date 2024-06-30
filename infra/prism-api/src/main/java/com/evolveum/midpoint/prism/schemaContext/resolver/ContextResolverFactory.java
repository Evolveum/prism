package com.evolveum.midpoint.prism.schemaContext.resolver;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

/***
 * The interface provides methods for create resolvers.
 */
public interface ContextResolverFactory {

    SchemaContextResolver createResolver(SchemaContextDefinition definition);

}
