/*
 * Copyright (c) 2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

/**
 * @param ignoreMetadata Value metadata won't be cloned.
 * @param ignoreContainerValueIds Container value IDs won't be cloned.
 * @param ignoreEmbeddedObjects Objects embedded in {@link PrismReferenceValue}s won't be cloned.
 *
 * @author semancik
 */
public record CloneStrategy(
        boolean ignoreMetadata,
        boolean ignoreContainerValueIds,
        boolean ignoreEmbeddedObjects) {

    /**
     * Literal clone. All properties of the clone are the same as those of the original.
     */
    public static final CloneStrategy LITERAL = literal();

    /**
     * As {@link #LITERAL} but ignores the metadata.
     */
    public static final CloneStrategy LITERAL_NO_METADATA = literal().withIgnoreMetadata();

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
    public static final CloneStrategy REUSE = reuse();

    public static final CloneStrategy LITERAL_IGNORING_EMBEDDED_OBJECTS = literal().withIgnoreEmbeddedObjects();

    public CloneStrategy withIgnoreMetadata() {
        return new CloneStrategy(true, ignoreContainerValueIds, ignoreEmbeddedObjects);
    }

    public CloneStrategy withIgnoreContainerValueIds() {
        return new CloneStrategy(ignoreMetadata, true, ignoreEmbeddedObjects);
    }

    public CloneStrategy withIgnoreEmbeddedObjects() {
        return new CloneStrategy(ignoreMetadata, ignoreContainerValueIds, true);
    }

    static CloneStrategy literal() {
        return new CloneStrategy(false, false, false);
    }

    static CloneStrategy reuse() {
        return literal()
                .withIgnoreContainerValueIds()
                .withIgnoreEmbeddedObjects();
    }

    /** TODO reconsider this method; replace it with something more specific */
    public boolean isLiteral() {
        return equals(LITERAL);
    }
}
