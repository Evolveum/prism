/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.xnode.MapXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * TODO cleanup this interface
 */
public interface QueryConverter {

    // 1. Parsing

    // 1a. Parsing filters

    ObjectFilter parseFilter(XNode xnode, Class<? extends Containerable> clazz) throws SchemaException;

    ObjectFilter parseFilter(@NotNull SearchFilterType filter, @NotNull Class<?> clazz) throws SchemaException;

    ObjectFilter parseFilter(@NotNull SearchFilterType filter, @NotNull ItemDefinition<?> objDef) throws SchemaException;

    /**
     * Tries to parse as much from filter as possible, without knowing the definition of object(s) to which the
     * filter will be applied. It is used mainly to parse path specifications, in order to avoid namespace loss
     * when serializing raw (unparsed) paths and QNames - see MID-1969.
     */
    void parseFilterPreliminarily(MapXNode xfilter, ParsingContext pc) throws SchemaException;

    // 1b. Parsing queries

    ObjectQuery createObjectQuery(Class<?> clazz, QueryType queryType) throws SchemaException;

    ObjectQuery createObjectQuery(Class<?> clazz, SearchFilterType filterType) throws SchemaException;

    // 2. Serializing

    // 2a. Serializing filters

    SearchFilterType createSearchFilterType(ObjectFilter filter) throws SchemaException;

    /**
     * Creates search  filter type from Object Filter
     * @param filter Filter to be serialized
     * @param forceAxiom If true, filter will be serialized in Query Language
     * @return
     * @throws SchemaException
     */
    SearchFilterType createSearchFilterType(ObjectFilter filter, boolean forceAxiom) throws SchemaException;

    ObjectFilter createObjectFilter(Class<?> clazz, SearchFilterType filterType)
            throws SchemaException;

    ObjectFilter createObjectFilter(ItemDefinition<?> containerDefinition, SearchFilterType filterType)
            throws SchemaException;

    MapXNode serializeFilter(ObjectFilter filter) throws SchemaException;

    // 2b. Serializing queries

    @Contract("null -> null; !null -> !null")
    QueryType createQueryType(ObjectQuery query) throws SchemaException;

}
