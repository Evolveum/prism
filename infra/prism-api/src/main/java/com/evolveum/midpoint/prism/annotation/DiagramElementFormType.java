/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.annotation;

import com.evolveum.midpoint.util.exception.SchemaException;

public enum DiagramElementFormType {
    EXPANDED, COLLAPSED;

    public static DiagramElementFormType parse(String s) throws SchemaException {
        if (s == null) {
            return null;
        }
        switch (s) {
            case "expanded":
                return EXPANDED;
            case "collapsed":
                return COLLAPSED;
            default:
                throw new SchemaException("Unknown diagram form "+s);
        }
    }
}
