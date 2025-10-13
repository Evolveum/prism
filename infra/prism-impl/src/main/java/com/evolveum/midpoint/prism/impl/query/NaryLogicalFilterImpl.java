/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.query;

import com.evolveum.midpoint.prism.query.NaryLogicalFilter;
import com.evolveum.midpoint.prism.query.ObjectFilter;

import java.util.List;

public abstract class NaryLogicalFilterImpl extends LogicalFilterImpl implements NaryLogicalFilter {

    public NaryLogicalFilterImpl() {
        super();
    }

    public NaryLogicalFilterImpl(List<ObjectFilter> conditions) {
        setConditions(conditions);
    }

    public ObjectFilter getLastCondition() {
        List<ObjectFilter> conditions = getConditions();
        if (conditions.isEmpty()) {
            return null;
        } else {
            return conditions.get(conditions.size()-1);
        }
    }

    @Override
    public abstract NaryLogicalFilterImpl clone();

}
