/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;
import com.evolveum.midpoint.prism.schemaContext.resolver.Algorithm;
import com.evolveum.midpoint.prism.schemaContext.resolver.ContextResolverFactory;
import com.evolveum.midpoint.prism.schemaContext.resolver.SchemaContextResolver;
import com.evolveum.midpoint.prism.schemaContext.resolver.SchemaContextResolverRegistry;

import com.evolveum.midpoint.util.MiscUtil;

import org.jetbrains.annotations.NotNull;

/** Creates {@link SchemaContextResolver} instances. */
public class ContextResolverFactoryImpl {

    /**
     * Creates {@link SchemaContextResolver} instance, given a static definition ({@link SchemaContextDefinition}).
     *
     * @throws IllegalArgumentException E.g. if the static definition is wrong
     * @throws IllegalStateException E.g. if the algorithm is known but its definition is missing
     */
    public static @NotNull SchemaContextResolver createResolver(SchemaContextDefinition schemaContextDefinition) {

        if (schemaContextDefinition.getType() != null) {
            return new TypeContextResolver(schemaContextDefinition);
        }

        if (schemaContextDefinition.getTypePath() != null) {
            return new TypePropertyContextResolver(schemaContextDefinition);
        }

        if (schemaContextDefinition.getAlgorithm() != null) {
            String algorithmName = schemaContextDefinition.getAlgorithm().getLocalPart();
            Algorithm algorithm = MiscUtil.argNonNull(
                    Algorithm.findAlgorithmByName(algorithmName),
                    "Unknown algorithm: %s",
                    algorithmName);
            ContextResolverFactory contextResolverFactory = MiscUtil.stateNonNull(
                    SchemaContextResolverRegistry.getRegistry().get(algorithm),
                    "No implementation for algorithm: %s",
                    algorithm);
            return contextResolverFactory.createResolver(schemaContextDefinition);
        }

        throw new IllegalArgumentException(
                "Invalid schema context definition - cannot derive context resolver from it. Use type, typePath, or algorithm. "
                        + "Definition: " + schemaContextDefinition);
    }
}
