package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dominik.
 */
public class SchemaContextResolverRegister {
    private static final Map<String, Object> schemaContextResolver = new HashMap<>();

    public static void register(String nameResolver, Object resolver) {
        schemaContextResolver.put(nameResolver, resolver);
    }

    public SchemaContextResolver createResolver(SchemaContextDefinition schemaContextDefinition) {
        SchemaContextResolver resolver;
        return null;
    }
}
