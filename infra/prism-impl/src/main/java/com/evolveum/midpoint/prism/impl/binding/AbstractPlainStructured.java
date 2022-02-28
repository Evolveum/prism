package com.evolveum.midpoint.prism.impl.binding;
/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.evolveum.midpoint.prism.JaxbVisitable;
import com.evolveum.midpoint.prism.JaxbVisitor;
import com.evolveum.midpoint.prism.binding.PlainStructured;
import com.evolveum.midpoint.prism.binding.StructuredEqualsStrategy;
import com.evolveum.midpoint.prism.binding.StructuredHashCodeStrategy;

public abstract class AbstractPlainStructured implements PlainStructured, Serializable, JaxbVisitable {

    private static final long serialVersionUID = 1L;


    public AbstractPlainStructured() {
        // NOOP
    }

    public AbstractPlainStructured(AbstractPlainStructured other) {
        if (other == null) {
            throw new NullPointerException("other is null");
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object obj) {
        return equals(obj, StructuredEqualsStrategy.DEFAULT);
    }

    @Override
    public boolean equals(Object other, StructuredEqualsStrategy strategy) {
        if (other == null) {
            return false;
        }
        return (other instanceof AbstractPlainStructured);
    }

    @Override
    public int hashCode() {
        return hashCode(StructuredHashCodeStrategy.DEFAULT);
    }

    @Override
    public int hashCode(StructuredHashCodeStrategy strategy) {
        return 1;
    }

    @Override
    public void accept(JaxbVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public abstract PlainStructured clone();
}
