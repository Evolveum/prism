/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.schemaContext.resolver;

import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Looks up the schema context for given prism value (usually container value).
 * The resolver is usually derived from {@link SchemaContextDefinition} that points e.g. to specific {@link Algorithm}.
 */
public interface SchemaContextResolver {

    /**
     * Returns the schema context for given prism value, if known.
     *
     * @return schema context annotation if exists, if not exist return null
     */
    @Nullable SchemaContext computeContext(@NotNull PrismValue prismValue);

}
