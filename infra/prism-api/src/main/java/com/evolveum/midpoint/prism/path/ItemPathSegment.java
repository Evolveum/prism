/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.path;

import com.evolveum.midpoint.util.DebugUtil;

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * @author semancik
 *
 */
public abstract class ItemPathSegment implements Serializable, Cloneable {

    private boolean wildcard = false;

    /**
     * Returns the string representation of full or simplified (POJO) item path segment.
     *
     * Enhance as necessary.
     *
     * @see NameItemPathSegment#toString()
     */
    public static String toString(Object segment) {
        if (segment instanceof ItemPathSegment) {
            return segment.toString();
        } else if (segment instanceof QName) {
            return DebugUtil.formatElementName((QName) segment);
        } else {
            return String.valueOf(segment);
        }
    }

    public boolean isWildcard() {
        return wildcard;
    }

    protected void setWildcard(boolean wildcard) {
        this.wildcard = wildcard;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (wildcard ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)  return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ItemPathSegment other = (ItemPathSegment) obj;
        if (wildcard != other.wildcard) return false;
        return true;
    }

    public abstract boolean equivalent(Object obj);

    public abstract ItemPathSegment clone();
}
