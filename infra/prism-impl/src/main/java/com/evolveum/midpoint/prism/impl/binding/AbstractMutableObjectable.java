/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.binding;

import com.evolveum.midpoint.prism.*;

import com.evolveum.midpoint.prism.impl.EmbeddedPrismObjectImpl;
import com.evolveum.midpoint.prism.impl.PrismContainerImpl;

import jakarta.xml.bind.annotation.XmlAttribute;
import javax.xml.namespace.QName;

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
        if (value instanceof PrismObjectValue objVal) {
            var parent = objVal.getParent();
            if (parent instanceof PrismObject prismObject) {
                if (objVal.isImmutable()) {
                    prismObject.freeze();
                }
                return prismObject;
            } else if (parent == null) {
                //noinspection unchecked
                return new PrismObjectImpl<>(prismGetContainerName(), this.getClass(), objVal);
            } else {
                //noinspection unchecked
                return new EmbeddedPrismObjectImpl<>(prismGetContainerName(), this.getClass(), objVal);
            }
        }
        if (value == null) {
            var object = new PrismObjectImpl<>(prismGetContainerName(), this.getClass());
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
            copyDefinitionFromValue(value);
            PrismForJAXBUtil.setupContainerValue(asPrismContainer(), value);
        }
    }

    // FIXME dirty hack to copy the customized definition
    private void copyDefinitionFromValue(PrismContainerValue value) {
        if (value == null) {
            return;
        }
        var parent = value.getParent();
        if (!(parent instanceof PrismObject<?> prismObject)) {
            return;
        }
        var parentDef = parent.getDefinition();
        if (parentDef == null) {
            return;
        }
        var parentCtd = parentDef.getComplexTypeDefinition();
        ComplexTypeDefinition valueCtd = value.getComplexTypeDefinition();
        if (valueCtd == null || parentCtd != valueCtd) {
            var parentDefClone = parentDef.clone();
            parentDefClone.mutator().setComplexTypeDefinition(valueCtd);
            if (parentDef.isImmutable()) {
                parentDefClone.freeze();
            }
            ((PrismContainerImpl) prismObject).setDefinitionHack(parentDefClone);
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

