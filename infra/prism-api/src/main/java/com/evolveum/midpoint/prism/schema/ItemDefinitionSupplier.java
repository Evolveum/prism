/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.util.exception.SchemaException;

/** Used to provide delayed definition building. */
@FunctionalInterface
public interface ItemDefinitionSupplier {
    ItemDefinition<?> get() throws SchemaException;
}
