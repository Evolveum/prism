/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.binding;


import java.util.List;

import jakarta.xml.bind.annotation.XmlAnyElement;

import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.PrismContainerValueImpl;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;

public abstract class AbstractMutableContainerable implements ContainerablePrismBinding , Cloneable, Containerable {

    /**
     *
     */
    private static final long serialVersionUID = 7082764147545316106L;

    private PrismContainerValue value;

    @Override
    public void setupContainerValue(PrismContainerValue container) {
        this.value = container;
    }

    public Long getId() {
        return asPrismContainerValue().getId();
    }

    public void setId(Long value) {
        asPrismContainerValue().setId(value);
    }

    @Override
    public PrismContainerValue asPrismContainerValue() {
        if (value == null) {
            value = new PrismContainerValueImpl<>(this);
        }
        return value;
    }

    @Override
    public String toString() {
        return asPrismContainerValue().toString();
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof Containerable)) {
            return false;
        }
        if (!this.getClass().equals(object.getClass())) {
            return false;
        }
        Containerable other = ((Containerable) object);
        return asPrismContainerValue().equivalent(other.asPrismContainerValue());
    }

    @Override
    public int hashCode() {
        return asPrismContainerValue().hashCode();
    }

    @Override
    protected Containerable clone() {
        return asPrismContainerValue().clone().asContainerable();
    }

    public static class Any extends AbstractMutableContainerable {

        @XmlAnyElement(lax = true)
        public List<Object> getAny() {
            return PrismForJAXBUtil.getAny(asPrismContainerValue(), Object.class);
        }


    }

}
