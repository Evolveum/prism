package com.evolveum.midpoint.prism.schemaContext.resolver;

/**
 * List of implemented resolver algorithms for schema context.
 */
public enum AlgorithmName {

    RESOURCE_OBJECT_CONTEXT_RESOLVER("ResourceObjectContextResolver");

    public final String name;

    AlgorithmName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
