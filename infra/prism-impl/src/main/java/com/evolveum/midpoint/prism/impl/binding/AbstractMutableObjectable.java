/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.binding;

import jakarta.xml.bind.annotation.XmlAttribute;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismObjectValue;
import com.evolveum.midpoint.prism.impl.PrismObjectImpl;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.prism.xml.ns._public.types_3.ObjectType;

public abstract class AbstractMutableObjectable extends ObjectType implements ContainerablePrismBinding, Objectable, Cloneable {

    /**
     * Always set up if we are connected to a value.
     *  May be {@link PrismObjectValue} or "only" {@link PrismContainerValue}.
     **/
    private PrismContainerValue<?> value;

    public AbstractMutableObjectable() {
        asPrismContainer();
    }

    @SuppressWarnings("rawtypes")
    public PrismObject asPrismContainer() {
        if (value instanceof PrismObjectValue) {
            var objVal = (PrismObjectValue) value;
            var parent = value.getParent();
            if (parent instanceof PrismObject) {
                return (PrismObject) parent;
            }
            if (parent == null) {
                var object = new PrismObjectImpl<>(prismGetContainerName(), this.getClass(), PrismContext.get(), objVal);
                return object;
            }

        }
        if (value == null) {
            var object =new PrismObjectImpl<>(prismGetContainerName(), this.getClass(), PrismContext.get());
            value = object.getValue();
            return object;
        }
        return null;
    }

    private boolean isContainerValueOnly() {
        return value != null && !(value instanceof PrismObjectValue);
    }

    @Override
    @XmlAttribute(name = "oid")
    public String getOid() {
        return asPrismContainer().getOid();
    }

    @Override
    public void setOid(String value) {
        asPrismContainer().setOid(value);
    }

    @Override
    public void setupContainerValue(PrismContainerValue value) {
        this.value = value;
        if (!isContainerValueOnly()) {
            PrismForJAXBUtil.setupContainerValue(asPrismContainer(), value);
        }
    }

    @Override
    public PrismObject asPrismObject() {
        return asPrismContainer();
    }

    @Override
    public PrismContainerValue asPrismContainerValue() {
        return value;
    }

    protected abstract QName prismGetContainerName();

    protected abstract QName prismGetContainerType();

    @Override
    public String getVersion() {
        return asPrismContainer().getVersion();
    }

    @Override
    public void setVersion(String version) {
        asPrismContainer().setVersion(version);
    }

    @Override
    public void setupContainer(PrismObject object) {
        this.value = object != null ? object.getValue() : null;
    }

    @Override
    public String toString() {
        return isContainerValueOnly() ?
                value.toString() : asPrismContainer().toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AbstractMutableObjectable)) {
            return false;
        }
        AbstractMutableObjectable other = ((AbstractMutableObjectable) object);
        return value.equivalent(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toDebugType() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        return builder.toString();
    }

    @Override
    public String toDebugName() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        if (!isContainerValueOnly()) {
            builder.append("[");
            builder.append(getOid());
            builder.append(", ");
            builder.append(getName());
            builder.append("]");
        }
        return builder.toString();
    }

    @Override
    protected Object clone() {
        return value.clone().asContainerable();
    }
}

