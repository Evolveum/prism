package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.impl.schemaContext.ContextResolverFactory;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dominik.
 */
public class SchemaContextResolverRegister {
    private static final Map<QName, ContextResolverFactory> schemaContextResolver = new HashMap<>();

    public static void register(QName nameResolver, ContextResolverFactory resolver) {
        schemaContextResolver.put(nameResolver, resolver);
    }

    public static SchemaContextResolver createResolver(SchemaContextDefinition schemaContextDefinition) {

        if (schemaContextDefinition.getTypePath() != null) {
            return new TypePropertyContextResolver(schemaContextDefinition);
        }

        return schemaContextResolver.get(new QName(schemaContextDefinition.getAlgorithm())).createResolver(schemaContextDefinition);
    }
}
