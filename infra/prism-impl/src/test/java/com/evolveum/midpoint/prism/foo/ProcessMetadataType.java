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
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.util.Producer;

import jakarta.xml.bind.annotation.XmlElement;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */
public class ProcessMetadataType extends AbstractMutableContainerable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "ProcessMetadataType");
    public static final ItemName F_REQUEST_TIMESTAMP = ItemName.interned(ObjectType.NS_FOO, "requestTimestamp");
    public static final ItemName F_REQUESTOR_REF = ItemName.interned(ObjectType.NS_FOO, "requestorRef");
    public static final ItemName F_REQUESTOR_COMMENT = ItemName.interned(ObjectType.NS_FOO, "requestorComment");
    public static final ItemName F_CREATE_APPROVER_REF = ItemName.interned(ObjectType.NS_FOO, "createApproverRef");
    public static final ItemName F_CREATE_APPROVAL_COMMENT = ItemName.interned(ObjectType.NS_FOO, "createApprovalComment");
    public static final ItemName F_CREATE_APPROVAL_TIMESTAMP = ItemName.interned(ObjectType.NS_FOO, "createApprovalTimestamp");
    public static final ItemName F_MODIFY_APPROVER_REF = ItemName.interned(ObjectType.NS_FOO, "modifyApproverRef");
    public static final ItemName F_MODIFY_APPROVAL_COMMENT = ItemName.interned(ObjectType.NS_FOO, "modifyApprovalComment");
    public static final ItemName F_MODIFY_APPROVAL_TIMESTAMP = ItemName.interned(ObjectType.NS_FOO, "modifyApprovalTimestamp");
    public static final ItemName F_CERTIFICATION_FINISHED_TIMESTAMP = ItemName.interned(ObjectType.NS_FOO, "certificationFinishedTimestamp");
    public static final ItemName F_CERTIFICATION_OUTCOME = ItemName.interned(ObjectType.NS_FOO, "certificationOutcome");
    public static final ItemName F_CERTIFIER_REF = ItemName.interned(ObjectType.NS_FOO, "certifierRef");
    public static final ItemName F_CERTIFIER_COMMENT = ItemName.interned(ObjectType.NS_FOO, "certifierComment");
    public static final Producer<ProcessMetadataType> FACTORY = new Producer<ProcessMetadataType>() {

        private static final long serialVersionUID = 201105211233L;

        public ProcessMetadataType run() {
            return new ProcessMetadataType();
        }

    }
            ;

    public ProcessMetadataType() {
        super();
    }

    @Deprecated
    public ProcessMetadataType(PrismContext context) {
        super();
    }

    @XmlElement(name = "requestTimestamp")
    public XMLGregorianCalendar getRequestTimestamp() {
        return this.prismGetPropertyValue(F_REQUEST_TIMESTAMP, XMLGregorianCalendar.class);
    }

    public void setRequestTimestamp(XMLGregorianCalendar value) {
        this.prismSetPropertyValue(F_REQUEST_TIMESTAMP, value);
    }

    @XmlElement(name = "requestorRef")
    public ObjectReferenceType getRequestorRef() {
        return this.prismGetReferencable(F_REQUESTOR_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setRequestorRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_REQUESTOR_REF, value);
    }

    @XmlElement(name = "requestorComment")
    public String getRequestorComment() {
        return this.prismGetPropertyValue(F_REQUESTOR_COMMENT, String.class);
    }

    public void setRequestorComment(String value) {
        this.prismSetPropertyValue(F_REQUESTOR_COMMENT, value);
    }

    @XmlElement(name = "createApproverRef")
    public List<ObjectReferenceType> getCreateApproverRef() {
        return this.prismGetReferencableList(ObjectReferenceType.FACTORY, F_CREATE_APPROVER_REF, ObjectReferenceType.class);
    }

    @XmlElement(name = "createApprovalComment")
    public List<String> getCreateApprovalComment() {
        return this.prismGetPropertyValues(F_CREATE_APPROVAL_COMMENT, String.class);
    }

    @XmlElement(name = "createApprovalTimestamp")
    public XMLGregorianCalendar getCreateApprovalTimestamp() {
        return this.prismGetPropertyValue(F_CREATE_APPROVAL_TIMESTAMP, XMLGregorianCalendar.class);
    }

    public void setCreateApprovalTimestamp(XMLGregorianCalendar value) {
        this.prismSetPropertyValue(F_CREATE_APPROVAL_TIMESTAMP, value);
    }

    @XmlElement(name = "modifyApproverRef")
    public List<ObjectReferenceType> getModifyApproverRef() {
        return this.prismGetReferencableList(ObjectReferenceType.FACTORY, F_MODIFY_APPROVER_REF, ObjectReferenceType.class);
    }

    @XmlElement(name = "modifyApprovalComment")
    public List<String> getModifyApprovalComment() {
        return this.prismGetPropertyValues(F_MODIFY_APPROVAL_COMMENT, String.class);
    }

    @XmlElement(name = "modifyApprovalTimestamp")
    public XMLGregorianCalendar getModifyApprovalTimestamp() {
        return this.prismGetPropertyValue(F_MODIFY_APPROVAL_TIMESTAMP, XMLGregorianCalendar.class);
    }

    public void setModifyApprovalTimestamp(XMLGregorianCalendar value) {
        this.prismSetPropertyValue(F_MODIFY_APPROVAL_TIMESTAMP, value);
    }

    @XmlElement(name = "certificationFinishedTimestamp")
    public XMLGregorianCalendar getCertificationFinishedTimestamp() {
        return this.prismGetPropertyValue(F_CERTIFICATION_FINISHED_TIMESTAMP, XMLGregorianCalendar.class);
    }

    public void setCertificationFinishedTimestamp(XMLGregorianCalendar value) {
        this.prismSetPropertyValue(F_CERTIFICATION_FINISHED_TIMESTAMP, value);
    }

    @XmlElement(name = "certificationOutcome")
    public String getCertificationOutcome() {
        return this.prismGetPropertyValue(F_CERTIFICATION_OUTCOME, String.class);
    }

    public void setCertificationOutcome(String value) {
        this.prismSetPropertyValue(F_CERTIFICATION_OUTCOME, value);
    }

    @XmlElement(name = "certifierRef")
    public List<ObjectReferenceType> getCertifierRef() {
        return this.prismGetReferencableList(ObjectReferenceType.FACTORY, F_CERTIFIER_REF, ObjectReferenceType.class);
    }

    @XmlElement(name = "certifierComment")
    public List<String> getCertifierComment() {
        return this.prismGetPropertyValues(F_CERTIFIER_COMMENT, String.class);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public ProcessMetadataType requestTimestamp(XMLGregorianCalendar value) {
        setRequestTimestamp(value);
        return this;
    }

    public ProcessMetadataType requestTimestamp(String value) {
        return requestTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public ProcessMetadataType requestorRef(ObjectReferenceType value) {
        setRequestorRef(value);
        return this;
    }

    public ProcessMetadataType requestorRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return requestorRef(ort);
    }

    public ProcessMetadataType requestorRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return requestorRef(ort);
    }

    public ObjectReferenceType beginRequestorRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        requestorRef(value);
        return value;
    }

    public ProcessMetadataType requestorComment(String value) {
        setRequestorComment(value);
        return this;
    }

    public ProcessMetadataType createApproverRef(ObjectReferenceType value) {
        getCreateApproverRef().add(value);
        return this;
    }

    public ProcessMetadataType createApproverRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return createApproverRef(ort);
    }

    public ProcessMetadataType createApproverRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return createApproverRef(ort);
    }

    public ObjectReferenceType beginCreateApproverRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        createApproverRef(value);
        return value;
    }

    public ProcessMetadataType createApprovalComment(String value) {
        getCreateApprovalComment().add(value);
        return this;
    }

    public ProcessMetadataType createApprovalTimestamp(XMLGregorianCalendar value) {
        setCreateApprovalTimestamp(value);
        return this;
    }

    public ProcessMetadataType createApprovalTimestamp(String value) {
        return createApprovalTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public ProcessMetadataType modifyApproverRef(ObjectReferenceType value) {
        getModifyApproverRef().add(value);
        return this;
    }

    public ProcessMetadataType modifyApproverRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return modifyApproverRef(ort);
    }

    public ProcessMetadataType modifyApproverRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return modifyApproverRef(ort);
    }

    public ObjectReferenceType beginModifyApproverRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        modifyApproverRef(value);
        return value;
    }

    public ProcessMetadataType modifyApprovalComment(String value) {
        getModifyApprovalComment().add(value);
        return this;
    }

    public ProcessMetadataType modifyApprovalTimestamp(XMLGregorianCalendar value) {
        setModifyApprovalTimestamp(value);
        return this;
    }

    public ProcessMetadataType modifyApprovalTimestamp(String value) {
        return modifyApprovalTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public ProcessMetadataType certificationFinishedTimestamp(XMLGregorianCalendar value) {
        setCertificationFinishedTimestamp(value);
        return this;
    }

    public ProcessMetadataType certificationFinishedTimestamp(String value) {
        return certificationFinishedTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public ProcessMetadataType certificationOutcome(String value) {
        setCertificationOutcome(value);
        return this;
    }

    public ProcessMetadataType certifierRef(ObjectReferenceType value) {
        getCertifierRef().add(value);
        return this;
    }

    public ProcessMetadataType certifierRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return certifierRef(ort);
    }

    public ProcessMetadataType certifierRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return certifierRef(ort);
    }

    public ObjectReferenceType beginCertifierRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        certifierRef(value);
        return value;
    }

    public ProcessMetadataType certifierComment(String value) {
        getCertifierComment().add(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public ProcessMetadataType clone() {
        return ((ProcessMetadataType) super.clone());
    }
}
