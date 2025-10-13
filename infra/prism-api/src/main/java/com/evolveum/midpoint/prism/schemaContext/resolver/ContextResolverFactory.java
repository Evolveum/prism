/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.schemaContext.resolver;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

/***
 * The interface provides methods for create resolvers.
 */
public interface ContextResolverFactory {

    SchemaContextResolver createResolver(SchemaContextDefinition definition);

}
