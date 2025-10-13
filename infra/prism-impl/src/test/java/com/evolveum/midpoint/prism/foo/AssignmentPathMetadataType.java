/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
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
@XmlType(name = "AssignmentPathMetadataType", propOrder = {
        "sourceRef",
        "segment"
})
public class AssignmentPathMetadataType extends AbstractMutableContainerable {
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "AssignmentPathMetadataType");
    public static final ItemName F_SOURCE_REF = ItemName.interned(ObjectType.NS_FOO, "sourceRef");
    public static final ItemName F_SEGMENT = ItemName.interned(ObjectType.NS_FOO, "segment");
    public static final Producer<AssignmentPathMetadataType> FACTORY = new Producer<AssignmentPathMetadataType>() {

        private static final long serialVersionUID = 201105211233L;

        public AssignmentPathMetadataType run() {
            return new AssignmentPathMetadataType();
        }

    }
            ;

    public AssignmentPathMetadataType() {
        super();
    }

    @Deprecated
    public AssignmentPathMetadataType(PrismContext context) {
        super();
    }

    @XmlElement(name = "sourceRef")
    public ObjectReferenceType getSourceRef() {
        return this.prismGetReferencable(F_SOURCE_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setSourceRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_SOURCE_REF, value);
    }

    @XmlElement(name = "segment")
    public List<AssignmentPathSegmentMetadataType> getSegment() {
        return this.prismGetContainerableList(AssignmentPathSegmentMetadataType.FACTORY, F_SEGMENT, AssignmentPathSegmentMetadataType.class);
    }

    public List<AssignmentPathSegmentMetadataType> createSegmentList() {
        PrismForJAXBUtil.createContainer(asPrismContainerValue(), F_SEGMENT);
        return getSegment();
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public AssignmentPathMetadataType sourceRef(ObjectReferenceType value) {
        setSourceRef(value);
        return this;
    }

    public AssignmentPathMetadataType sourceRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return sourceRef(ort);
    }

    public AssignmentPathMetadataType sourceRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return sourceRef(ort);
    }

    public ObjectReferenceType beginSourceRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        sourceRef(value);
        return value;
    }

    public AssignmentPathMetadataType segment(AssignmentPathSegmentMetadataType value) {
        getSegment().add(value);
        return this;
    }

    public AssignmentPathSegmentMetadataType beginSegment() {
        AssignmentPathSegmentMetadataType value = new AssignmentPathSegmentMetadataType();
        segment(value);
        return value;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public AssignmentPathMetadataType clone() {
        return ((AssignmentPathMetadataType) super.clone());
    }

}
