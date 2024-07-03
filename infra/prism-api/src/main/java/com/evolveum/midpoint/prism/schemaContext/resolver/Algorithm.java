package com.evolveum.midpoint.prism.schemaContext.resolver;

/**
 * List of implemented resolver algorithms for schema context.
 */
public enum Algorithm {

    RESOURCE_OBJECT_CONTEXT_RESOLVER("ResourceObjectContextResolver"),
    SHADOW_CONSTRUCTION_CONTEXT_RESOLVER("ShadowConstructionContextResolver");

    public final String name;

    Algorithm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Algorithm findAlgorithmByName(String name) {
        for (Algorithm algorithm : values()) {
            if (algorithm.getName().equals(name)) return algorithm;
        }

        return null;
    }
}
