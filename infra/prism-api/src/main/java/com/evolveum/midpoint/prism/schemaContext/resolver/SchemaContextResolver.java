/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.schemaContext.resolver;

import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;

/**
 * The interface provides methods of resolver that looks up the schema context based on definition from schema in specific case.
 */
public interface SchemaContextResolver {

    /**
     * The method during find schema context by schema context definition
     *
     * @param prismValue
     * @return schema context annotation if exists, if not exist return null
     */
    SchemaContext computeContext(PrismValue prismValue);

}
