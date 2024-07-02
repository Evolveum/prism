package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;
import com.evolveum.midpoint.prism.schemaContext.resolver.Algorithm;
import com.evolveum.midpoint.prism.schemaContext.resolver.ContextResolverFactory;
import com.evolveum.midpoint.prism.schemaContext.resolver.SchemaContextResolver;
import com.evolveum.midpoint.prism.schemaContext.resolver.SchemaContextResolverRegistry;

/**
 * Created by Dominik.
 */
public class ContextResolverFactoryImpl {

    public static SchemaContextResolver createResolver(SchemaContextDefinition schemaContextDefinition) {

        if (schemaContextDefinition.getType() != null) {
            return new TypeContextResolver(schemaContextDefinition);
        }

        if (schemaContextDefinition.getTypePath() != null) {
            return new TypePropertyContextResolver(schemaContextDefinition);
        }

        if (schemaContextDefinition.getAlgorithm() != null) {
            ContextResolverFactory contextResolverFactory = SchemaContextResolverRegistry.getRegistry().get(Algorithm.getAlgorithmByName(schemaContextDefinition.getAlgorithm().getLocalPart()));
            return contextResolverFactory.createResolver(schemaContextDefinition);
        }

        return null;
    }
}
