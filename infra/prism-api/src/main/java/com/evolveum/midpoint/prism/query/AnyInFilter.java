/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.PrismConstants;

public interface AnyInFilter<T> extends PropertyValueFilter<T> {

    QName ELEMENT_NAME = new QName(PrismConstants.NS_QUERY, "anyIn");

    @Override
    AnyInFilter<T> clone();

}
