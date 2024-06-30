package com.evolveum.midpoint.prism.schemaContext.resolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for resolvers from MidPoints.
 *
 * Created by Dominik.
 */
public class SchemaContextResolverRegistry {

    private static final Map<Algorithm, SchemaContextResolver> SCHEMA_CONTEXT_RESOLVER = new HashMap<>();

    public static void register(Algorithm algorithm, SchemaContextResolver resolver) {
        SCHEMA_CONTEXT_RESOLVER.put(algorithm, resolver);
    }

    public static Map<Algorithm, SchemaContextResolver> getRegistry() {
        return SCHEMA_CONTEXT_RESOLVER;
    }
}
