package com.evolveum.midpoint.prism.schemaContext.resolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for resolvers from MidPoints.
 *
 * Created by Dominik.
 */
public class SchemaContextResolverRegistry {

    private static final Map<AlgorithmName, ContextResolverFactory> SCHEMA_CONTEXT_RESOLVER = new HashMap<>();

    public static void register(AlgorithmName algorithmName, ContextResolverFactory resolverFactory) {
        SCHEMA_CONTEXT_RESOLVER.put(algorithmName, resolverFactory);
    }

    public static Map<AlgorithmName, ContextResolverFactory> getRegister() {
        return SCHEMA_CONTEXT_RESOLVER;
    }
}
