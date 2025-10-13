/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.query;

/**
 *
 */
public interface NotFilter extends UnaryLogicalFilter {

    @Override
    NotFilter clone();

    @Override
    NotFilter cloneEmpty();

    void setFilter(ObjectFilter inner);

    //    @Override
//    protected String getDebugDumpOperationName() {
//        return "AND";
//    }

}
