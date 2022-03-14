/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.binding;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.evolveum.midpoint.util.DOMUtil;

/**
 * Strategy for equals computation of {@link PlainStructured} objects
 *
 */
public abstract class StructuredEqualsStrategy {

    /**
     * DOM Aware strategy, which ignores differences in namespace prefixes.
     */
    public static final StructuredEqualsStrategy DOM_AWARE = new StructuredEqualsStrategy() {

        @Override
        protected boolean equalsNonNull(Element left, Element right) {
            return DOMUtil.compareElement(left, right, false);
        }

        @Override
        public boolean equalsNonNull(String left, String right) {
            return DOMUtil.compareTextNodeValues(left, right);
        }

        @Override
        protected boolean equalsNonNull(QName left, QName right) {
            return left.equals(right);
        }
    };

    /**
     * DOM Aware strategy, which takes into account also differences in namespace prefixes.
     */
    public static final StructuredEqualsStrategy LITERAL = new StructuredEqualsStrategy() {

        @Override
        protected boolean equalsNonNull(Element left, Element right) {
            return DOMUtil.compareElement(left, right, true);
        }

        @Override
        public boolean equalsNonNull(String left, String right) {
            return DOMUtil.compareTextNodeValues(left, right);
        }

        @Override
        protected boolean equalsNonNull(QName left, QName right) {
            if (!left.equals(right)) {
                return false;
            }
            return Objects.equals(left.getPrefix(), right.getPrefix());
        }
    };


    public static final StructuredEqualsStrategy DEFAULT = DOM_AWARE;

    public boolean equals(PlainStructured left, PlainStructured right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.equals(right, this);
    }

    public <T> boolean equals(List<T> left, List<T> right) {
        // Make sure both lists are non-null
        left = left != null ? left : Collections.emptyList();
        right = right != null ? right : Collections.emptyList();

        if (left.size() != right.size()) {
            return false;
        }

        Iterator<T> leftIter = left.iterator();
        Iterator<T> rightIter = right.iterator();

        while(leftIter.hasNext()) {
            if (!equalsDispatch(leftIter.next(), rightIter.next())) {
                return false;
            }
        }
        return true;
    }

    private <T> boolean equalsDispatch(T left, T right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (left instanceof PlainStructured && right instanceof PlainStructured) {
            return ((PlainStructured) left).equals(right, this);
        }
        if (left instanceof String && right instanceof String) {
            return equalsNonNull((String) left, (String) right);
        }
        if (left instanceof byte[] && right instanceof byte[]) {
            return equals((byte[]) left, (byte[]) right);
        }
        if (left instanceof JAXBElement<?> && right instanceof JAXBElement<?>) {
            return equalsNonNull((JAXBElement<?>) left, (JAXBElement<?>) right);
        }
        if (left instanceof Element && right instanceof Element) {
            return equalsNonNull((Element) left, (Element) right);
        }
        if (left instanceof QName && right instanceof QName) {
            return equalsNonNull((QName) left, (QName) right);
        }
        return equalsNonNull(left, right);
    }

    public boolean equals(Object left, Object right) {
        return equalsDispatch(left, right);
    }

    protected boolean equalsNonNull(Object left, Object right) {
        return Objects.equals(left, right);
    }

    protected abstract boolean equalsNonNull(Element left, Element right);

    protected abstract boolean equalsNonNull(QName left, QName right);


    protected boolean equalsNonNull(JAXBElement<?> left, JAXBElement<?> right) {
        if (!equals(left.getName(), right.getName())) {
            return false;
        }
        return equals(left.getValue(), right.getValue());
    }

    public boolean equalsNonNull(String left, String right) {
        return Objects.equals(left, right);
    }

    public boolean equals(byte[] left, byte[] right) {
        return Arrays.equals(left, right);
    }


}
