/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.util;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Accepts (maybe repeatedly) a value and - on request - passes it to specified {@link Consumer}.
 *
 * @param <T> type of object held
 */
public class PassingHolder<T> extends Holder<T> {

    @Nullable private final Consumer<T> ultimateConsumer;

    public PassingHolder(@Nullable Consumer<T> ultimateConsumer) {
        this.ultimateConsumer = ultimateConsumer;
    }

    /** Passes a value to the consumer (repeatedly if needed). */
    public void passValue() {
        if (ultimateConsumer != null) {
            ultimateConsumer.accept(getValue());
        }
    }
}
