/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.concepts.func;

public interface FailableSupplier<O,E extends Exception> {

    O get();

    default <I> FailableFunction<I, O, E> toConstantFunction() {
        return i -> get();
    }
}
