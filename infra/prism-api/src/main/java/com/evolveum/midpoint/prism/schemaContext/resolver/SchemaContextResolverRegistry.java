package com.evolveum.midpoint.prism.schemaContext.resolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for resolvers from MidPoints.
 *
 * Created by Dominik.
 */
public class SchemaContextResolverRegistry {

    private static final Map<Algorithm, ContextResolverFactory> SCHEMA_CONTEXT_RESOLVER = new HashMap<>();

    public static void register(Algorithm algorithm, ContextResolverFactory contextResolverFactory) {
        SCHEMA_CONTEXT_RESOLVER.put(algorithm, contextResolverFactory);
    }

    public static Map<Algorithm, ContextResolverFactory> getRegistry() {
        return SCHEMA_CONTEXT_RESOLVER;
    }
}
