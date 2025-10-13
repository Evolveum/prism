/*
 * Copyright (c) 2016 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

@FunctionalInterface
public interface CheckedProcessor<T,E extends Exception> {

    void process(T value) throws E;
}
