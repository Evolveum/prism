/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

/**
 * @author Radovan Semancik
 *
 */
@FunctionalInterface
public interface Handler<T> {

    // returns false if the iteration (if any) has to be stopped
    boolean handle(T t);

}
