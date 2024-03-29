/*
 * Copyright (c) 2016-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.util;

import java.io.Serializable;

/**
 * Almost the same as {@link java.util.function.Supplier}, but this one is {@link Serializable}.
 * That is very useful especially in use in Wicket models.
 *
 * @author Radovan Semancik
 */
@FunctionalInterface
public interface Producer<T> extends Serializable {

    T run(); // todo shouldn't be 'get'?

}
