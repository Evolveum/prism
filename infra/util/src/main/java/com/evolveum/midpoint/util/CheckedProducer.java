/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

import java.io.Serializable;

import com.evolveum.midpoint.util.exception.CommonException;

/**
 * Almost the same as {@link Producer} but this one can throw {@link CommonException}.
 */
@FunctionalInterface
public interface CheckedProducer<T> extends Serializable {
    T get() throws CommonException;
}
