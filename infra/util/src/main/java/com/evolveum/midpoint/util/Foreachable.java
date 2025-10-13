/*
 * Copyright (c) 2016 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

/**
 * @author semancik
 *
 */
@FunctionalInterface
public interface Foreachable<T> {

    /**
     * Will call processor for every element in the instance.
     * This is NOT recursive. E.g. in case of collection of collections
     * the processor will NOT be called for elements of the inner collections.
     * If you need recursion please have a look at Visitor.
     */
    void foreach(Processor<T> processor);

}
