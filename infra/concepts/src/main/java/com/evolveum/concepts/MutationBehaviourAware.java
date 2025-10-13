/*
 * Copyright (C) 2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.concepts;

/**
 *
 * @author tony
 *
 * @param <M> Final version of mutation behaviour
 *
 * @see Immutable
 * @see Mutable
 * @see Freezable
 */
public interface MutationBehaviourAware<M extends MutationBehaviourAware<M>> {

    /**
     * Return true if object is currently mutable (can change publicly visible state)
     * @return true if object is currently mutable
     */
    boolean mutable();
}
