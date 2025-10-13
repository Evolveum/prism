/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

/**
 *  Keeps the state of the visitation in order to avoid visiting one object multiple times.
 */
public interface SmartVisitation<T extends SmartVisitable<T>> {

    boolean alreadyVisited(T visitable);

    void registerVisit(T visitable);
}
