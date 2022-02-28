/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.binding;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.evolveum.midpoint.prism.JaxbVisitable;
import com.evolveum.midpoint.prism.JaxbVisitor;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.google.common.base.Objects;

public abstract class AbstractValueWrapper<T> implements Serializable, Cloneable, JaxbVisitable {

    private static final long serialVersionUID = 201105211233L;

    public abstract T getValue();
    public abstract void setValue(T value);
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public void accept(JaxbVisitor visitor) {
        visitor.visit(this);
        if (getValue() != null) {
            PrismForJAXBUtil.accept(getValue(), visitor);
        }
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }
        return Objects.equal(this.getValue(), ((AbstractValueWrapper<?>) obj).getValue());
    }



}
