/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api.stream;

import com.evolveum.concepts.SourceLocation;

abstract public class AbstractStreamAdapter<N,V>  implements AxiomStreamTarget<N, V> {

    protected abstract AxiomStreamTarget<?, ?> target();

    @Override
    public void endItem(SourceLocation loc) {
        target().endItem(loc);
    }

    @Override
    public void endValue(SourceLocation loc) {
        target().endValue(loc);
    }

    @Override
    public void endInfra(SourceLocation loc) {
        target().endInfra(loc);
    }

}
