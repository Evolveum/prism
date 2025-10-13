/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.PrismConstants;

public interface AnyInFilter<T> extends PropertyValueFilter<T> {

    QName ELEMENT_NAME = new QName(PrismConstants.NS_QUERY, "anyIn");

    @Override
    AnyInFilter<T> clone();

}
