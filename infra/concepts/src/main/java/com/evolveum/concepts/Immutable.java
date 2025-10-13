/*
 * Copyright (C) 2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.concepts;

public interface Immutable extends MutationBehaviourAware<Immutable> {

    /**
     * Always return false.
     */
    @Override
    default boolean mutable() {
        return true;
    }

    static boolean isImmutable(Object object) {
        return KnownImmutables.isImmutable(object);
    }
}
