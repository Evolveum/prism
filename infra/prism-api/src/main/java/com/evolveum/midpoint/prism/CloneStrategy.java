/*
 * Copyright (c) 2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

/**
 * @author semancik
 *
 */
public enum CloneStrategy {

    /**
     * Literal clone. All properties of the clone are the same as those of the original.
     */
    LITERAL,

    /**
     * As {@link #LITERAL} but ignores the metadata.
     */
    LITERAL_NO_METADATA,

    /**
     * Clone for reuse.
     * Create clone of the object that is suitable to be reused
     * in a different object or delta. The cloned object will
     * have the same values, but it will not be presented as the
     * same object as was the source of cloning.
     *
     * E.g. in case of containers it will create a container
     * with the same values but with not identifiers.
     * References will not have full object inside them.
     */
    REUSE;

    public boolean ignoreMetadata() {
        return this == LITERAL_NO_METADATA;
    }
}
