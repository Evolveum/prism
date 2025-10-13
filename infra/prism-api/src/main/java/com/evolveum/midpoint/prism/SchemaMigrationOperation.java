/*
 * Copyright (c) 2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * @author semancik
 */
public enum SchemaMigrationOperation {

    REMOVED, MOVED, RENAMED_PARENT;

    public static SchemaMigrationOperation parse(String s) throws SchemaException {
        if (s == null) {
            return null;
        }
        return switch (s) {
            case "removed" -> REMOVED;
            case "moved" -> MOVED;
            case "renamedParent" -> RENAMED_PARENT;
            default -> throw new SchemaException("Unknown schema migration operation " + s);
        };
    }
}
