/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

/**
 * @author semancik
 *
 */
@FunctionalInterface
public interface SimpleVisitable<T> {

    void simpleAccept(SimpleVisitor<T> visitor);

}
