package com.evolveum.midpoint.prism.schemaContext.resolver;

public enum ResolverName {

    RESOURCE_OBJECT_CONTEXT_RESOLVER("ResourceObjectContextResolver");

    public final String name;

    ResolverName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
