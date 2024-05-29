package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.impl.schemaContext.ContextResolverFactory;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**3
 * Created by Dominik.
 */
public class SchemaContextResolverRegister {
    private static final Map<QName, ContextResolverFactory> SCHEMA_CONTEXT_RESOLVER = new HashMap<>();

    public static void register(QName nameResolver, ContextResolverFactory resolver) {
        SCHEMA_CONTEXT_RESOLVER.put(nameResolver, resolver);
    }

    public static SchemaContextResolver createResolver(SchemaContextDefinition schemaContextDefinition) {

        if (schemaContextDefinition.getTypePath() != null) {
            return new TypePropertyContextResolver(schemaContextDefinition);
        }

        if (Objects.equals(schemaContextDefinition.getAlgorithm(), new QName("ResourceObjectContextResolver"))) {
            return new ResourceObjectContextResolver(schemaContextDefinition);
        }

        return SCHEMA_CONTEXT_RESOLVER.get(schemaContextDefinition.getAlgorithm()).createResolver(schemaContextDefinition);
    }
}
