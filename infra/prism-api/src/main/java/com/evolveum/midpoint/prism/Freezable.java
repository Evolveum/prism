/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 *  Something that can be made immutable.
 */
public interface Freezable {

    boolean isImmutable();

    /** Should be no-op (or very quick) if the object is already immutable. */
    void freeze();

    default void checkMutable() {
        if (isImmutable()) {
            throw new IllegalStateException("An attempt to modify an immutable: " + toString());
        }
    }

    default void checkImmutable() {
        if (!isImmutable()) {
            throw new IllegalStateException("Item is not immutable even if it should be: " + toString());
        }
    }

    /**
     * Convenience variant to be used in fluent interfaces.
     * The name is different from {@link #checkImmutable()} to allow method references.
     */
    @Contract("null -> null; !null -> !null")
    static <T extends Freezable> T checkIsImmutable(T freezable) {
        if (freezable != null) {
            freezable.checkImmutable();
        }
        return freezable;
    }

    /**
     * Convenience variant to be used in fluent interfaces. The name is different from {@link #freeze()}
     * to allow method references. TODO better name!
     */
    @Contract("null -> null; !null -> !null")
    static <T extends Freezable> T doFreeze(T freezable) {
        if (freezable != null) {
            freezable.freeze();
        }
        return freezable;
    }

    static void freezeNullable(Freezable target) {
        if (target != null) {
            target.freeze();
        }
    }

    static <C extends Collection<? extends Freezable>> @NotNull C freezeAll(@NotNull C collection) {
        for (Freezable freezable : collection) {
            freezable.freeze();
        }
        return collection;
    }
}
