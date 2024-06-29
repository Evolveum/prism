package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import java.util.HashMap;
import java.util.Map;

import com.evolveum.midpoint.prism.schemaContext.resolver.ContextResolverFactory;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;
import com.evolveum.midpoint.prism.schemaContext.resolver.AlgorithmName;
import com.evolveum.midpoint.prism.schemaContext.resolver.SchemaContextResolver;

/**
 *
 * Created by Dominik.
 */
public class SchemaContextResolverRegistry {

    private static final Map<AlgorithmName, ContextResolverFactory> SCHEMA_CONTEXT_RESOLVER = new HashMap<>();

    public static void register(AlgorithmName algorithmName, ContextResolverFactory resolver) {
        SCHEMA_CONTEXT_RESOLVER.put(algorithmName, resolver);
    }

    public static SchemaContextResolver createResolver(SchemaContextDefinition schemaContextDefinition) {
        if (schemaContextDefinition.getType() != null) {
            return new TypeContextResolver(schemaContextDefinition);
        }

        if (schemaContextDefinition.getTypePath() != null) {
            return new TypePropertyContextResolver(schemaContextDefinition);
        }

        if (schemaContextDefinition.getAlgorithm() != null) {
            ContextResolverFactory resolverFactory = SCHEMA_CONTEXT_RESOLVER.get(AlgorithmName.valueOf(schemaContextDefinition.getAlgorithm().getLocalPart()));

            if (resolverFactory != null) {
                return resolverFactory.createResolver(schemaContextDefinition);
            }
        }

        return null;
    }
}
