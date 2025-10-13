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
@XmlType(name = "TransformationMetadataType", propOrder = {
        "mappingTransformation"
})
public class TransformationMetadataType  extends AbstractMutableContainerable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "TransformationMetadataType");
    public static final ItemName F_MAPPING_TRANSFORMATION = ItemName.interned(ObjectType.NS_FOO, "mappingTransformation");
    public static final Producer<TransformationMetadataType> FACTORY = new Producer<TransformationMetadataType>() {

        private static final long serialVersionUID = 201105211233L;

        public TransformationMetadataType run() {
            return new TransformationMetadataType();
        }

    }
            ;

    public TransformationMetadataType() {
        super();
    }

    @Deprecated
    public TransformationMetadataType(PrismContext context) {
        super();
    }

    @XmlElement(name = "mappingTransformation")
    public MappingTransformationType getMappingTransformation() {
        return this.prismGetSingleContainerable(F_MAPPING_TRANSFORMATION, MappingTransformationType.class);
    }

    public void setMappingTransformation(MappingTransformationType value) {
        this.prismSetSingleContainerable(F_MAPPING_TRANSFORMATION, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public TransformationMetadataType mappingTransformation(MappingTransformationType value) {
        setMappingTransformation(value);
        return this;
    }

    public MappingTransformationType beginMappingTransformation() {
        MappingTransformationType value = new MappingTransformationType();
        mappingTransformation(value);
        return value;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public TransformationMetadataType clone() {
        return ((TransformationMetadataType) super.clone());
    }

}
