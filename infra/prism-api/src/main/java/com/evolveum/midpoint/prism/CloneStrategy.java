/*
 * Copyright (c) 2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

/**
 * Describes how cloning (copying) of a prism structure should be done.
 *
 * @param ignoreMetadata Value metadata won't be cloned.
 * @param ignoreContainerValueIds Container value IDs won't be cloned.
 * @param ignoreEmbeddedObjects Objects embedded in {@link PrismReferenceValue}s won't be cloned.
 * @param mutableCopy Should the copy be always mutable? If false, the copy will be immutable if the original is immutable.
 *
 * @author semancik
 */
public record CloneStrategy(
        boolean ignoreMetadata,
        boolean ignoreContainerValueIds,
        boolean ignoreEmbeddedObjects,
        boolean mutableCopy) {

    /**
     * Literal mutable clone. All properties of the clone are the same as those of the original. The result is mutable.
     */
    public static final CloneStrategy LITERAL_MUTABLE = literalAny().withMutableCopy();

    @Deprecated // use LITERAL_MUTABLE
    public static final CloneStrategy LITERAL = LITERAL_MUTABLE;

    /**
     * As {@link #LITERAL_MUTABLE} but ignores the metadata.
     */
    public static final CloneStrategy LITERAL_NO_METADATA_MUTABLE = LITERAL_MUTABLE.withIgnoreMetadata();

    /**
     * As {@link #LITERAL_MUTABLE} but ignores the embedded objects.
     */
    public static final CloneStrategy LITERAL_IGNORING_EMBEDDED_OBJECTS_MUTABLE = LITERAL_MUTABLE.withIgnoreEmbeddedObjects();

    /**
     * Clone for reuse.
     *
     * Create clone of the object that is suitable to be reused in a different object or delta. The cloned object will
     * have the same values, but it will not be presented as the same object as was the source of cloning.
     *
     * E.g. in case of containers it will create a container with the same values but with not identifiers.
     * References will not have full object inside them.
     *
     * The result is mutable.
     */
    public static final CloneStrategy REUSE = reuse();

    /**
     * Literal copy, not necessarily mutable.
     *
     * If the original is immutable, then the copy will be immutable as well. However, the copy will have a modifiable parent,
     * so it can be attached to a different prism structure.
     *
     * If the original is mutable, a {@link #LITERAL_MUTABLE} clone will be done.
     */
    public static final CloneStrategy LITERAL_ANY = literalAny();

    private CloneStrategy withIgnoreMetadata() {
        return new CloneStrategy(true, ignoreContainerValueIds, ignoreEmbeddedObjects, mutableCopy);
    }

    private CloneStrategy withIgnoreContainerValueIds() {
        return new CloneStrategy(ignoreMetadata, true, ignoreEmbeddedObjects, mutableCopy);
    }

    private CloneStrategy withIgnoreEmbeddedObjects() {
        return new CloneStrategy(ignoreMetadata, ignoreContainerValueIds, true, mutableCopy);
    }

    private CloneStrategy withMutableCopy() {
        return new CloneStrategy(ignoreMetadata, ignoreContainerValueIds, ignoreEmbeddedObjects, true);
    }

    private static CloneStrategy literalAny() {
        return new CloneStrategy(false, false, false, false);
    }

    private static CloneStrategy reuse() {
        return literalAny()
                .withMutableCopy()
                .withIgnoreContainerValueIds()
                .withIgnoreEmbeddedObjects();
    }

    /** TODO reconsider this method; replace it with something more specific */
    public boolean isLiteral() {
        return equals(LITERAL_MUTABLE);
    }
}
