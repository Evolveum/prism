/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

public interface Matchable<T extends Matchable<T>> {

    boolean match(T other);

    boolean matches(String regex);
}
