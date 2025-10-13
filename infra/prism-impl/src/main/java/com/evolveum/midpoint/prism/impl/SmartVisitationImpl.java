/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.SmartVisitation;
import com.evolveum.midpoint.prism.SmartVisitable;

import java.util.IdentityHashMap;

/**
 * A visitation of a structure of SmartVisitables. It remembers what objects were already visited in order to avoid cycles.
 */
public class SmartVisitationImpl<T extends SmartVisitable<T>> implements SmartVisitation<T> {

    private IdentityHashMap<T, Boolean> alreadyVisited = new IdentityHashMap<>();

    @Override
    public boolean alreadyVisited(T visitable) {
        return alreadyVisited.containsKey(visitable);
    }

    @Override
    public void registerVisit(T visitable) {
        alreadyVisited.put(visitable, true);
    }
}
