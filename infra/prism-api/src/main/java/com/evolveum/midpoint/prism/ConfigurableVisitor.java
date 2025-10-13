/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.annotation.Experimental;

/**
 * Visitor with configurable behavior.
 */
@Experimental
public interface ConfigurableVisitor<T extends Visitable<T>> extends Visitor<T> {

    /**
     * Should we visit also objects that are embedded in references?
     */
    boolean shouldVisitEmbeddedObjects();

    /**
     * Helper method.
     */
    static boolean shouldVisitEmbeddedObjects(Visitor<?> visitor) {
        //noinspection SimplifiableIfStatement
        if (visitor instanceof ConfigurableVisitor) {
            return ((ConfigurableVisitor<?>) visitor).shouldVisitEmbeddedObjects();
        } else {
            return false; // This is the behavior before 60328c40b2b99c6cf41ab6ce90145fae941d07bd (March 24th, 2020)
        }
    }
}
