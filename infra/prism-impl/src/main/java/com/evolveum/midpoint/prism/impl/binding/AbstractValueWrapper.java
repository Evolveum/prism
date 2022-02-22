package com.evolveum.midpoint.prism.impl.binding;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.evolveum.midpoint.prism.JaxbVisitable;
import com.evolveum.midpoint.prism.JaxbVisitor;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;

public class AbstractValueWrapper<T> implements Serializable, Cloneable, JaxbVisitable {

    private final static long serialVersionUID = 201105211233L;

    @XmlValue
    protected T value;


    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public void accept(JaxbVisitor visitor) {
        visitor.visit(this);
        PrismForJAXBUtil.accept(value, visitor);
    }

}
