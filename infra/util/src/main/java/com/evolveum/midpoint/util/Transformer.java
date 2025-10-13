/*
 * Copyright (c) 2014 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

/**
 * @author semancik
 */
@FunctionalInterface
public interface Transformer<T,X> {

    X transform(T in);

}
