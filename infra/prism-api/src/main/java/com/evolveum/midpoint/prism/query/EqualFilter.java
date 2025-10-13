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
public interface EqualFilter<T> extends PropertyValueFilter<T> {

    QName ELEMENT_NAME = new QName(PrismConstants.NS_QUERY, "equal");

    @Override
    EqualFilter<T> clone();

}
