package com.evolveum.midpoint.prism.schemaContext.resolver;

import java.util.Objects;

/**
 * List of implemented resolver algorithms for schema context.
 */
public enum Algorithm {

    RESOURCE_OBJECT_CONTEXT_RESOLVER("ResourceObject"),
    SHADOW_CONSTRUCTION_CONTEXT_RESOLVER("ShadowConstructionContextResolver"),;

    public final String name;

    Algorithm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Algorithm getAlgorithmNameByValue(String name) {
        for (Algorithm algorithm : values()) {
            return Objects.equals(name, algorithm.getName()) ? algorithm : null;
        }
        return null;
    }
}
