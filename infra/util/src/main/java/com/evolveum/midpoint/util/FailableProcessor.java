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
public interface FailableProcessor<T> {

    void process(T object) throws Exception;

}
