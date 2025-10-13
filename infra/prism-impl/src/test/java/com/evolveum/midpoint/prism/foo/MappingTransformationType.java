/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableContainerable;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "MappingTransformationType", propOrder = {
        "mappingSpecification",
        "source"
})
public class MappingTransformationType  extends AbstractMutableContainerable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "MappingTransformationType");
    public static final ItemName F_MAPPING_SPECIFICATION = ItemName.interned(ObjectType.NS_FOO, "mappingSpecification");
    public static final ItemName F_SOURCE = ItemName.interned(ObjectType.NS_FOO, "source");
    public static final Producer<MappingTransformationType> FACTORY = new Producer<MappingTransformationType>() {

        private static final long serialVersionUID = 201105211233L;

        public MappingTransformationType run() {
            return new MappingTransformationType();
        }

    }
            ;

    public MappingTransformationType() {
        super();
    }

    @Deprecated
    public MappingTransformationType(PrismContext context) {
        super();
    }

    @XmlElement(name = "mappingSpecification")
    public MappingSpecificationType getMappingSpecification() {
        return this.prismGetSingleContainerable(F_MAPPING_SPECIFICATION, MappingSpecificationType.class);
    }

    public void setMappingSpecification(MappingSpecificationType value) {
        this.prismSetSingleContainerable(F_MAPPING_SPECIFICATION, value);
    }

    @XmlElement(name = "source")
    public List<MappingSourceType> getSource() {
        return this.prismGetContainerableList(MappingSourceType.FACTORY, F_SOURCE, MappingSourceType.class);
    }

    public List<MappingSourceType> createSourceList() {
        PrismForJAXBUtil.createContainer(asPrismContainerValue(), F_SOURCE);
        return getSource();
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public MappingTransformationType mappingSpecification(MappingSpecificationType value) {
        setMappingSpecification(value);
        return this;
    }

    public MappingSpecificationType beginMappingSpecification() {
        MappingSpecificationType value = new MappingSpecificationType();
        mappingSpecification(value);
        return value;
    }

    public MappingTransformationType source(MappingSourceType value) {
        getSource().add(value);
        return this;
    }

    public MappingSourceType beginSource() {
        MappingSourceType value = new MappingSourceType();
        source(value);
        return value;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public MappingTransformationType clone() {
        return ((MappingTransformationType) super.clone());
    }

}
