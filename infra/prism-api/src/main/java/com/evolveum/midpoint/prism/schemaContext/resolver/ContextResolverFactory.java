package com.evolveum.midpoint.prism.schemaContext.resolver;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

/***
 * The interface provides methods for the register of resolver.
 */
public interface ContextResolverFactory {

    void register(AlgorithmName name, SchemaContextResolver resolver);

    AlgorithmName getAlgorithmName();

    SchemaContextResolver createResolver(SchemaContextDefinition schemaContext);

}
