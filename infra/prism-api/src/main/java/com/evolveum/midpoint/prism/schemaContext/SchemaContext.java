/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.schemaContext;

import java.io.Serializable;

import com.evolveum.midpoint.prism.ItemDefinition;

/**
 * The interface represents the schema context annotation, which provides semantic information about object.
 *
 * See also https://docs.evolveum.com/midpoint/devel/schema-context-annotations/.
 */
public interface SchemaContext extends Serializable {

    ItemDefinition<?> getItemDefinition();

}
