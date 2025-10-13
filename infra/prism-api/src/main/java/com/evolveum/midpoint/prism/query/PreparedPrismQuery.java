/*
 * Copyright (C) 2020-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Prepared Prism Query with placeholders.
 */
public interface PreparedPrismQuery {

    /**
     * Binds next unbound value to provided value
     *
     * @param realValue Real Value to be bound
     * @throws SchemaException If provided value is invalid according to schema definition (type of value
     * @throws IllegalStateException If there is no positional value to be bound
     */
    void bindValue(Object realValue) throws SchemaException;

    /**
     * Binds named placeholder to provided value
     *
     * @param name Name of named placeholder
     * @param realValue Real Value to be bound
     * @throws SchemaException If provided value is invalid according to schema definition (type of value
     * @throws IllegalStateException If there is no positional value to be bound
     */
    void set(String name, Object realValue) throws SchemaException;
    /**
     * Returns complete filter with values bound
     *
     * @return Object Filter parser from Axiom
     * @throws SchemaException If resulting filter with bound values is invalid
     * @throws IllegalStateException If not all placeholders were bound prior to invocation
     */
    ObjectFilter toFilter() throws SchemaException;

    /**
     * Binds positional placeholders and returns filter
     *
     * @param args
     * @return
     * @throws SchemaException
     */
    default ObjectFilter bind(Object... args) throws SchemaException {
        for (Object arg : args) {
            bindValue(arg);
        }
        return toFilter();
    }

    boolean allPlaceholdersBound();

}
