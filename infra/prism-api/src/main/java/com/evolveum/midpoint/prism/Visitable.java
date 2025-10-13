/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

/**
 * @author semancik
 *
 */
@FunctionalInterface
public interface Visitable<T extends Visitable<T>> {

    void accept(Visitor<T> visitor);

}
