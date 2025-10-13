/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.path;

import com.evolveum.midpoint.prism.PrismConstants;

import javax.xml.namespace.QName;

/**
 * Denotes identifier of the object or container (i.e. OID or container ID).
 * Currently supported only for sorting (not even for filtering!).
 */
public class IdentifierPathSegment extends ItemPathSegment {

    public static final String SYMBOL = "#";
    public static final QName QNAME = PrismConstants.T_ID;

    @Override
    public boolean equivalent(Object obj) {
        return equals(obj);
    }

    @Override
    public ItemPathSegment clone() {
        return new IdentifierPathSegment();
    }

    @Override
    public String toString() {
        return SYMBOL;
    }
}
