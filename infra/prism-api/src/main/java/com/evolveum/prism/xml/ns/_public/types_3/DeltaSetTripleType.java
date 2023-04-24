/*
 * Copyright (c) 2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.prism.xml.ns._public.types_3;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.binding.PlainStructured;
import com.evolveum.midpoint.prism.delta.PrismValueDeltaSetTriple;
import com.evolveum.midpoint.prism.util.CloneUtil;

/**
 * Experimental.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeltaSetTripleType", propOrder = {
        "zero",
        "plus",
        "minus"
})
public class DeltaSetTripleType implements PlainStructured.WithoutStrategy, JaxbVisitable, Cloneable {

    @XmlElement
    @Raw
    private final List<Object> zero = new ArrayList<>();

    @XmlElement
    @Raw
    private final List<Object> plus = new ArrayList<>();

    @XmlElement
    @Raw
    private final List<Object> minus = new ArrayList<>();

    public static final QName COMPLEX_TYPE = new QName(PrismConstants.NS_TYPES, "DeltaSetTripleType");
    public static final QName F_ZERO = new QName(PrismConstants.NS_TYPES, "zero");
    public static final QName F_PLUS = new QName(PrismConstants.NS_TYPES, "plus");
    public static final QName F_MINUS = new QName(PrismConstants.NS_TYPES, "minus");

    public List<Object> getZero() {
        return zero;
    }

    public List<Object> getPlus() {
        return plus;
    }

    public List<Object> getMinus() {
        return minus;
    }

    @Override
    public void accept(JaxbVisitor visitor) {
        visitor.visit(this);
        visit(zero, visitor);
        visit(plus, visitor);
        visit(minus, visitor);
    }

    public void visit(List<Object> set, JaxbVisitor visitor) {
        for (Object o : set) {
            if (o instanceof JaxbVisitable) {
                visitor.visit((JaxbVisitable) o);
            }
        }
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public DeltaSetTripleType clone() {
        DeltaSetTripleType clone = new DeltaSetTripleType();
        for (Object v : zero) {
            clone.zero.add(CloneUtil.clone(v));
        }
        for (Object v : plus) {
            clone.plus.add(CloneUtil.clone(v));
        }
        for (Object v : minus) {
            clone.minus.add(CloneUtil.clone(v));
        }
        return clone;
    }

    @Override
    public String toString() {
        return "DeltaSetTripleType{" +
                "zero=" + zero +
                ", plus=" + plus +
                ", minus=" + minus +
                '}';
    }

    public static <V extends PrismValue> DeltaSetTripleType fromDeltaSetTriple(PrismValueDeltaSetTriple<V> triple) {
        PrismContext prismContext = PrismContext.get();
        DeltaSetTripleType rv = new DeltaSetTripleType();
        for (V v : triple.getZeroSet()) {
            rv.zero.add(v != null ? v.getRealValueOrRawType(prismContext) : "null"); // fixme
        }
        for (V v : triple.getPlusSet()) {
            rv.plus.add(v != null ? v.getRealValueOrRawType(prismContext) : "null"); // fixme
        }
        for (V v : triple.getMinusSet()) {
            rv.minus.add(v != null ? v.getRealValueOrRawType(prismContext) : "null"); // fixme
        }
        return rv;
    }
}
