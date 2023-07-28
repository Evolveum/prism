/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
