package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableContainerable;

import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "MappingSourceType", propOrder = {
        "name",
        "value"
})
public class MappingSourceType extends AbstractMutableContainerable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "MappingSourceType");
    public static final ItemName F_NAME = ItemName.interned(ObjectType.NS_FOO, "name");
    public static final ItemName F_VALUE = ItemName.interned(ObjectType.NS_FOO, "value");
    public static final Producer<MappingSourceType> FACTORY = new Producer<MappingSourceType>() {

        private static final long serialVersionUID = 201105211233L;

        public MappingSourceType run() {
            return new MappingSourceType();
        }

    }
            ;

    public MappingSourceType() {
        super();
    }

    @Deprecated
    public MappingSourceType(PrismContext context) {
        super();
    }

    @XmlElement(name = "name")
    public String getName() {
        return this.prismGetPropertyValue(F_NAME, String.class);
    }

    public void setName(String value) {
        this.prismSetPropertyValue(F_NAME, value);
    }

    @XmlElement(name = "value")
    public Object getValue() {
        return this.prismGetPropertyValue(F_VALUE, Object.class);
    }

    public void setValue(Object value) {
        this.prismSetPropertyValue(F_VALUE, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public MappingSourceType name(String value) {
        setName(value);
        return this;
    }

    public MappingSourceType value(Object value) {
        setValue(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public MappingSourceType clone() {
        return ((MappingSourceType) super.clone());
    }

}
