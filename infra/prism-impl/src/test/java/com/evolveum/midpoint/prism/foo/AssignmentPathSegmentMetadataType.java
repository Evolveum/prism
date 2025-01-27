package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
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
@XmlType(name = "AssignmentPathSegmentMetadataType", propOrder = {
        "segmentOrder",
        "assignmentId",
        "inducementId",
        "targetRef",
        "matchingOrder"
})
public class AssignmentPathSegmentMetadataType extends AbstractMutableContainerable {
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "AssignmentPathSegmentMetadataType");
    public static final ItemName F_SEGMENT_ORDER = ItemName.interned(ObjectType.NS_FOO, "segmentOrder");
    public static final ItemName F_ASSIGNMENT_ID = ItemName.interned(ObjectType.NS_FOO, "assignmentId");
    public static final ItemName F_INDUCEMENT_ID = ItemName.interned(ObjectType.NS_FOO, "inducementId");
    public static final ItemName F_TARGET_REF = ItemName.interned(ObjectType.NS_FOO, "targetRef");
    public static final ItemName F_MATCHING_ORDER = ItemName.interned(ObjectType.NS_FOO, "matchingOrder");
    public static final Producer<AssignmentPathSegmentMetadataType> FACTORY = new Producer<AssignmentPathSegmentMetadataType>() {

        private static final long serialVersionUID = 201105211233L;

        public AssignmentPathSegmentMetadataType run() {
            return new AssignmentPathSegmentMetadataType();
        }

    }
            ;

    public AssignmentPathSegmentMetadataType() {
        super();
    }

    @Deprecated
    public AssignmentPathSegmentMetadataType(PrismContext context) {
        super();
    }

    @XmlElement(name = "segmentOrder")
    public Integer getSegmentOrder() {
        return this.prismGetPropertyValue(F_SEGMENT_ORDER, Integer.class);
    }

    public void setSegmentOrder(Integer value) {
        this.prismSetPropertyValue(F_SEGMENT_ORDER, value);
    }

    @XmlElement(name = "assignmentId")
    public Long getAssignmentId() {
        return this.prismGetPropertyValue(F_ASSIGNMENT_ID, Long.class);
    }

    public void setAssignmentId(Long value) {
        this.prismSetPropertyValue(F_ASSIGNMENT_ID, value);
    }

    @XmlElement(name = "inducementId")
    public Long getInducementId() {
        return this.prismGetPropertyValue(F_INDUCEMENT_ID, Long.class);
    }

    public void setInducementId(Long value) {
        this.prismSetPropertyValue(F_INDUCEMENT_ID, value);
    }

    @XmlElement(name = "targetRef")
    public ObjectReferenceType getTargetRef() {
        return this.prismGetReferencable(F_TARGET_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setTargetRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_TARGET_REF, value);
    }

    @XmlElement(name = "matchingOrder")
    public Boolean isMatchingOrder() {
        return this.prismGetPropertyValue(F_MATCHING_ORDER, Boolean.class);
    }

    public Boolean getMatchingOrder() {
        return this.prismGetPropertyValue(F_MATCHING_ORDER, Boolean.class);
    }

    public void setMatchingOrder(Boolean value) {
        this.prismSetPropertyValue(F_MATCHING_ORDER, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public AssignmentPathSegmentMetadataType segmentOrder(Integer value) {
        setSegmentOrder(value);
        return this;
    }

    public AssignmentPathSegmentMetadataType assignmentId(Long value) {
        setAssignmentId(value);
        return this;
    }

    public AssignmentPathSegmentMetadataType inducementId(Long value) {
        setInducementId(value);
        return this;
    }

    public AssignmentPathSegmentMetadataType targetRef(ObjectReferenceType value) {
        setTargetRef(value);
        return this;
    }

    public AssignmentPathSegmentMetadataType targetRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return targetRef(ort);
    }

    public AssignmentPathSegmentMetadataType targetRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return targetRef(ort);
    }

    public ObjectReferenceType beginTargetRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        targetRef(value);
        return value;
    }

    public AssignmentPathSegmentMetadataType matchingOrder(Boolean value) {
        setMatchingOrder(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public AssignmentPathSegmentMetadataType clone() {
        return ((AssignmentPathSegmentMetadataType) super.clone());
    }

}
