package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;
import com.evolveum.midpoint.prism.schemaContext.resolver.AlgorithmName;
import com.evolveum.midpoint.prism.schemaContext.resolver.ContextResolverFactory;
import com.evolveum.midpoint.prism.schemaContext.resolver.SchemaContextResolver;
import com.evolveum.midpoint.prism.schemaContext.resolver.SchemaContextResolverRegistry;

/**
 * Created by Dominik.
 */
public class ContextResolverFactoryImpl implements ContextResolverFactory {

    public SchemaContextResolver createResolver(SchemaContextDefinition schemaContextDefinition) {

        if (schemaContextDefinition.getType() != null) {
            return new TypeContextResolver(schemaContextDefinition);
        }

        if (schemaContextDefinition.getTypePath() != null) {
            return new TypePropertyContextResolver(schemaContextDefinition);
        }

        if (schemaContextDefinition.getAlgorithm() != null) {
            return SchemaContextResolverRegistry.getRegister().get(AlgorithmName.valueOf(schemaContextDefinition.getAlgorithm().getLocalPart())).createResolver(schemaContextDefinition);
        }

        return null;
    }
}
