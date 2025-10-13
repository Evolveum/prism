/*
 * Copyright (C) 2020-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.util.exception.SchemaException;

public interface PrismQueryLanguageParser {

    /**
     * Parses Axiom Query filter with definition derived from the provided type class.
     *
     * For reference search filters use any {@link com.evolveum.midpoint.prism.Referencable}
     * and follow the rules for reference search filter:
     *
     * * exactly one ownedBy to define the reference context,
     * * any number of additional REF on the SELF path, that is `. matches (...)`.
     */
    <T> ObjectFilter parseFilter(Class<T> typeClass, String query) throws SchemaException;

    ObjectFilter parseFilter(ItemDefinition<?> definition, String query) throws SchemaException;

    PreparedPrismQuery parse(ItemDefinition<?> definition, String query) throws SchemaException;

    <T> PreparedPrismQuery parse(Class<T> typeClass, String query) throws SchemaException;
}
