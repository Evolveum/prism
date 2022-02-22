package com.evolveum.midpoint.prism.impl.binding;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.impl.PrismObjectImpl;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;

public abstract class AbstractMutableObjectable extends AbstractBoundContainerable implements Objectable {

    private PrismObject<?> object;

    public PrismObject<?> asPrismContainer() {
        if (object == null) {
            object = new PrismObjectImpl<>(prismGetContainerName(), this.getClass());
        }
        return object;
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
    public void setupContainerValue(PrismContainerValue container) {
        object = PrismForJAXBUtil.setupContainerValue(asPrismContainer(), container);
    }

    @Override
    public PrismObject asPrismObject() {
        return asPrismContainer();
    }

    @Override
    public PrismContainerValue asPrismContainerValue() {
        return asPrismContainer().getValue();
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
        this.object = object;
    }

    @Override
    public String toString() {
        return asPrismContainer().toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AbstractMutableObjectable)) {
            return false;
        }
        AbstractMutableObjectable other = ((AbstractMutableObjectable) object);
        return asPrismContainer().equivalent(other.asPrismContainer());
    }

    @Override
    public int hashCode() {
        return asPrismContainer().hashCode();
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
        builder.append("[");
        builder.append(getOid());
        builder.append(", ");
        builder.append(getName());
        builder.append("]");
        return builder.toString();
    }
}

