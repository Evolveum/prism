package com.evolveum.midpoint.prism.schemaContext.resolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for resolvers from MidPoints.
 *
 * Created by Dominik.
 */
public class SchemaContextResolverRegistry {

    private static final Map<AlgorithmName, SchemaContextResolver> SCHEMA_CONTEXT_RESOLVER = new HashMap<>();

    public static void register(AlgorithmName algorithmName, SchemaContextResolver resolver) {
        SCHEMA_CONTEXT_RESOLVER.put(algorithmName, resolver);
    }

    public static Map<AlgorithmName, SchemaContextResolver> getRegistry() {
        return SCHEMA_CONTEXT_RESOLVER;
    }
}
