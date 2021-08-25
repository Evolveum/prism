/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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

    @Override
    public PrismContext getPrismContext() {
        return PrismContext.get();
    }

    public void setPrismContext(PrismContext prismContext) {
    }

    @Override
    protected abstract void performFreeze();

    @Override
    public abstract ObjectFilterImpl clone();
}
