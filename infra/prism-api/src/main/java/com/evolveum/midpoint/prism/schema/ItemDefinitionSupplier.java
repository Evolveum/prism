/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.schema;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.util.exception.SchemaException;

/** Used to provide delayed definition building. */
@FunctionalInterface
public interface ItemDefinitionSupplier {
    ItemDefinition<?> get() throws SchemaException;
}
