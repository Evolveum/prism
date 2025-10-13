/*
 * Copyright (c) 2016-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
