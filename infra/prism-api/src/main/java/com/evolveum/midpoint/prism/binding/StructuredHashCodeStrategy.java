/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.binding;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;


/**
 *
 * Strategy for hashCode computation of {@link PlainStructured} objects.
 *
 */
public abstract class StructuredHashCodeStrategy {

    public static final StructuredHashCodeStrategy DEFAULT = new StructuredHashCodeStrategy() {

        @Override
        protected int hashCodeNonNull(Element val) {
            return 1;
        }
    };

    public int hashCode(int current, PlainStructured val) {
        if (val == null) {
            return current;
        }
        return current + val.hashCode(this);
    }

    public <T> int  hashCode(int current, List<T> val) {
        // Make sure both lists are non-null
        val = val != null ? val : Collections.emptyList();
        for(T value : val) {
            current += hashCodeDispatch(value);
        }
        return current;
    }

    private <T> int hashCodeDispatch(T val) {
        if (val == null) {
            return 0;
        }
        if (val instanceof PlainStructured) {
            return ((PlainStructured) val).hashCode(this);
        }
        if (val instanceof String) {
            return hashCodeNonNull((String) val);
        }
        if (val instanceof byte[]) {
            return hashCodeNonNull((byte[]) val);
        }
        if (val instanceof JAXBElement<?>) {
            return hashCodeNonNull((JAXBElement<?>) val);
        }
        if (val instanceof Element) {
            return hashCodeNonNull((Element) val);
        }
        if (val instanceof QName) {
            return hashCodeNonNull(val);
        }
        return hashCodeNonNull(val);
    }

    public int  hashCode(int current, Object val) {
        return current + hashCodeDispatch(val);
    }

    protected int  hashCodeNonNull(Object val) {
        return Objects.hashCode(val);
    }

    protected abstract int  hashCodeNonNull(Element val);

    public int hashCodeNonNull(JAXBElement<?> val) {
        return hashCodeNonNull(0, val);
    }

    protected int  hashCodeNonNull(int current, JAXBElement<?> val) {
        current = hashCode(current, val.getName());
        current = hashCode(current, val.getValue());
        return current;
    }

    public int hashCodeNonNull(String val) {
        return Objects.hashCode(val);
    }

    public int hashCodeNonNull(byte[] val) {
        return Arrays.hashCode(val);
    }


}
