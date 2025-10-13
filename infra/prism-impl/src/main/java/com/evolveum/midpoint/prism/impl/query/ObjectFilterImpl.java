/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.query;

import com.evolveum.midpoint.prism.AbstractFreezable;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.Visitor;

public abstract class ObjectFilterImpl extends AbstractFreezable implements ObjectFilter {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void revive(PrismContext prismContext) {
        // NOOP
    }

    public void setPrismContext(PrismContext prismContext) {
    }

    @Override
    protected abstract void performFreeze();

    @Override
    public abstract ObjectFilterImpl clone();
}
