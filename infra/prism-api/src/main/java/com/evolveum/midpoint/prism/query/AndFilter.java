/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.PrismConstants;

import javax.xml.namespace.QName;

/**
 *
 */
public interface AndFilter extends NaryLogicalFilter {

    QName ELEMENT_NAME = new QName(PrismConstants.NS_QUERY, "and");

    @Override
    AndFilter clone();

    @Override
    AndFilter cloneEmpty();

//    @Override
//    protected String getDebugDumpOperationName() {
//        return "AND";
//    }

}
