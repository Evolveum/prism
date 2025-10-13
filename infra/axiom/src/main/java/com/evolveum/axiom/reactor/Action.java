/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.reactor;

import java.util.Optional;

public interface Action<E extends Exception> extends Dependency<Void> {

    void apply();

    @Override
    default boolean isSatisfied() {
        return successful();
    }

    @Override
    default Void get() {
        return null;
    }

    boolean successful();

    /**
     * Returns true if action can be applied.
     *
     * Return false if action application of action failed with exception, which is non-retryable.
     */
    boolean canApply();

    /**
     *
     * @param e Exception which occurred during call of {@link #apply()}
     * @throws E If action specific exception if failed critically and all computation should be stopped.
     */
    void fail(Exception e) throws E;

    Optional<E> error();

}
