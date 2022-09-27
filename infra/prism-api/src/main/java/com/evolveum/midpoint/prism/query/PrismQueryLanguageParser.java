/*
 * Copyright (C) 2020-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.util.exception.SchemaException;

public interface PrismQueryLanguageParser {

    <C extends Containerable> ObjectFilter parseFilter(Class<C> typeClass, String query) throws SchemaException;

    ObjectFilter parseFilter(PrismContainerDefinition<?> definition, String query) throws SchemaException;
}
