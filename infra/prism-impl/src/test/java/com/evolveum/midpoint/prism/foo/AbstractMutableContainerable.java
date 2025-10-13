/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.impl.PrismContainerValueImpl;
import com.evolveum.midpoint.prism.impl.binding.ContainerablePrismBinding;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;

import jakarta.xml.bind.annotation.XmlAnyElement;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Dominik.
 */
public class AbstractMutableContainerable  implements ContainerablePrismBinding, Cloneable, Containerable {
    private static final long serialVersionUID = 7082764147545316106L;
    private PrismContainerValue value;

    public AbstractMutableContainerable() {
    }

    public void setupContainerValue(PrismContainerValue container) {
        this.value = container;
    }

    public Long getId() {
        return this.asPrismContainerValue().getId();
    }

    public void setId(Long value) {
        this.asPrismContainerValue().setId(value);
    }

    public PrismContainerValue asPrismContainerValue() {
        if (this.value == null) {
            this.value = new PrismContainerValueImpl(this);
        }

        return this.value;
    }

    public String toString() {
        return this.asPrismContainerValue().toString();
    }

    public boolean equals(@Nullable Object object) {
        if (object == null) {
            return false;
        } else if (!(object instanceof Containerable)) {
            return false;
        } else if (!this.getClass().equals(object.getClass())) {
            return false;
        } else {
            Containerable other = (Containerable)object;
            return this.asPrismContainerValue().equivalent(other.asPrismContainerValue());
        }
    }

    public int hashCode() {
        return this.asPrismContainerValue().hashCode();
    }

    protected Containerable clone() {
        return this.asPrismContainerValue().clone().asContainerable();
    }

    public static class Any extends com.evolveum.midpoint.prism.impl.binding.AbstractMutableContainerable {
        public Any() {
        }

        @XmlAnyElement(
                lax = true
        )
        public List<Object> getAny() {
            return PrismForJAXBUtil.getAny(this.asPrismContainerValue(), Object.class);
        }
    }
}
