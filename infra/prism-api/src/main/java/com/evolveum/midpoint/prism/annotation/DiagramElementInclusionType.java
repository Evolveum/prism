/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.annotation;

import com.evolveum.midpoint.util.exception.SchemaException;

public enum DiagramElementInclusionType {
    AUTO, INCLUDE, EXCLUDE;

    public static DiagramElementInclusionType parse(String s) throws SchemaException {
        if (s == null) {
            return null;
        }
        switch (s) {
            case "auto":
                return AUTO;
            case "include":
                return INCLUDE;
            case "exclude":
                return EXCLUDE;
            default:
                throw new SchemaException("Unknown diagram inclusion "+s);
        }
    }
}
