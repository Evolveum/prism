/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.schemaContext.resolver;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

import org.jetbrains.annotations.NotNull;

public interface ContextResolverFactory {

    /**
     * Creates {@link SchemaContextResolver} instance, given a static definition ({@link SchemaContextDefinition}).
     */
    @NotNull SchemaContextResolver createResolver(SchemaContextDefinition definition);

}
