/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.concepts.func;

public interface FailableConsumer<I, E extends Exception> {

    void accept(I input) throws E;

}
