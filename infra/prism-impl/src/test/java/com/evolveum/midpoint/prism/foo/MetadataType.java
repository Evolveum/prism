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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.List;

import static com.evolveum.midpoint.prism.foo.ObjectType.NS_FOO;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "MetadataType", propOrder = {
        "requestTimestamp",
        "requestorRef",
        "requestorComment",
        "createTimestamp",
        "creatorRef",
        "createApproverRef",
        "createApprovalComment",
        "createApprovalTimestamp",
        "createChannel",
        "createTaskRef",
        "modifyTimestamp",
        "modifierRef",
        "modifyApproverRef",
        "modifyApprovalComment",
        "modifyApprovalTimestamp",
        "modifyChannel",
        "modifyTaskRef",
        "lastProvisioningTimestamp",
        "certificationFinishedTimestamp",
        "certificationOutcome",
        "certifierRef",
        "certifierComment",
        "originMappingName"
})
public class MetadataType extends AbstractMutableContainerable {
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(NS_FOO, "MetadataType");
    public static final ItemName F_REQUEST_TIMESTAMP = ItemName.interned(NS_FOO, "requestTimestamp");
    public static final ItemName F_REQUESTOR_REF = ItemName.interned(NS_FOO, "requestorRef");
    public static final ItemName F_REQUESTOR_COMMENT = ItemName.interned(NS_FOO, "requestorComment");
    public static final ItemName F_CREATE_TIMESTAMP = ItemName.interned(NS_FOO, "createTimestamp");
    public static final ItemName F_CREATOR_REF = ItemName.interned(NS_FOO, "creatorRef");
    public static final ItemName F_CREATE_APPROVER_REF = ItemName.interned(NS_FOO, "createApproverRef");
    public static final ItemName F_CREATE_APPROVAL_COMMENT = ItemName.interned(NS_FOO, "createApprovalComment");
    public static final ItemName F_CREATE_APPROVAL_TIMESTAMP = ItemName.interned(NS_FOO, "createApprovalTimestamp");
    public static final ItemName F_CREATE_CHANNEL = ItemName.interned(NS_FOO, "createChannel");
    public static final ItemName F_CREATE_TASK_REF = ItemName.interned(NS_FOO, "createTaskRef");
    public static final ItemName F_MODIFY_TIMESTAMP = ItemName.interned(NS_FOO, "modifyTimestamp");
    public static final ItemName F_MODIFIER_REF = ItemName.interned(NS_FOO, "modifierRef");
    public static final ItemName F_MODIFY_APPROVER_REF = ItemName.interned(NS_FOO, "modifyApproverRef");
    public static final ItemName F_MODIFY_APPROVAL_COMMENT = ItemName.interned(NS_FOO, "modifyApprovalComment");
    public static final ItemName F_MODIFY_APPROVAL_TIMESTAMP = ItemName.interned(NS_FOO, "modifyApprovalTimestamp");
    public static final ItemName F_MODIFY_CHANNEL = ItemName.interned(NS_FOO, "modifyChannel");
    public static final ItemName F_MODIFY_TASK_REF = ItemName.interned(NS_FOO, "modifyTaskRef");
    public static final ItemName F_LAST_PROVISIONING_TIMESTAMP = ItemName.interned(NS_FOO, "lastProvisioningTimestamp");
    public static final ItemName F_CERTIFICATION_FINISHED_TIMESTAMP = ItemName.interned(NS_FOO, "certificationFinishedTimestamp");
    public static final ItemName F_CERTIFICATION_OUTCOME = ItemName.interned(NS_FOO, "certificationOutcome");
    public static final ItemName F_CERTIFIER_REF = ItemName.interned(NS_FOO, "certifierRef");
    public static final ItemName F_CERTIFIER_COMMENT = ItemName.interned(NS_FOO, "certifierComment");
    public static final ItemName F_ORIGIN_MAPPING_NAME = ItemName.interned(NS_FOO, "originMappingName");
    public static final Producer<MetadataType> FACTORY = new Producer<MetadataType>() {

        private static final long serialVersionUID = 201105211233L;

        public MetadataType run() {
            return new MetadataType();
        }

    }
            ;

    public MetadataType() {
        super();
    }

    @Deprecated
    public MetadataType(PrismContext context) {
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

    @XmlElement(name = "createTimestamp")
    public XMLGregorianCalendar getCreateTimestamp() {
        return this.prismGetPropertyValue(F_CREATE_TIMESTAMP, XMLGregorianCalendar.class);
    }

    public void setCreateTimestamp(XMLGregorianCalendar value) {
        this.prismSetPropertyValue(F_CREATE_TIMESTAMP, value);
    }

    @XmlElement(name = "creatorRef")
    public ObjectReferenceType getCreatorRef() {
        return this.prismGetReferencable(F_CREATOR_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setCreatorRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_CREATOR_REF, value);
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

    @XmlElement(name = "createChannel")
    public String getCreateChannel() {
        return this.prismGetPropertyValue(F_CREATE_CHANNEL, String.class);
    }

    public void setCreateChannel(String value) {
        this.prismSetPropertyValue(F_CREATE_CHANNEL, value);
    }

    @XmlElement(name = "createTaskRef")
    public ObjectReferenceType getCreateTaskRef() {
        return this.prismGetReferencable(F_CREATE_TASK_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setCreateTaskRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_CREATE_TASK_REF, value);
    }

    @XmlElement(name = "modifyTimestamp")
    public XMLGregorianCalendar getModifyTimestamp() {
        return this.prismGetPropertyValue(F_MODIFY_TIMESTAMP, XMLGregorianCalendar.class);
    }

    public void setModifyTimestamp(XMLGregorianCalendar value) {
        this.prismSetPropertyValue(F_MODIFY_TIMESTAMP, value);
    }

    @XmlElement(name = "modifierRef")
    public ObjectReferenceType getModifierRef() {
        return this.prismGetReferencable(F_MODIFIER_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setModifierRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_MODIFIER_REF, value);
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

    @XmlElement(name = "modifyChannel")
    public String getModifyChannel() {
        return this.prismGetPropertyValue(F_MODIFY_CHANNEL, String.class);
    }

    public void setModifyChannel(String value) {
        this.prismSetPropertyValue(F_MODIFY_CHANNEL, value);
    }

    @XmlElement(name = "modifyTaskRef")
    public ObjectReferenceType getModifyTaskRef() {
        return this.prismGetReferencable(F_MODIFY_TASK_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setModifyTaskRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_MODIFY_TASK_REF, value);
    }

    @XmlElement(name = "lastProvisioningTimestamp")
    public XMLGregorianCalendar getLastProvisioningTimestamp() {
        return this.prismGetPropertyValue(F_LAST_PROVISIONING_TIMESTAMP, XMLGregorianCalendar.class);
    }

    public void setLastProvisioningTimestamp(XMLGregorianCalendar value) {
        this.prismSetPropertyValue(F_LAST_PROVISIONING_TIMESTAMP, value);
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

    @XmlElement(name = "originMappingName")
    public String getOriginMappingName() {
        return this.prismGetPropertyValue(F_ORIGIN_MAPPING_NAME, String.class);
    }

    public void setOriginMappingName(String value) {
        this.prismSetPropertyValue(F_ORIGIN_MAPPING_NAME, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public MetadataType requestTimestamp(XMLGregorianCalendar value) {
        setRequestTimestamp(value);
        return this;
    }

    public MetadataType requestTimestamp(String value) {
        return requestTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public MetadataType requestorRef(ObjectReferenceType value) {
        setRequestorRef(value);
        return this;
    }

    public MetadataType requestorRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return requestorRef(ort);
    }

    public MetadataType requestorRef(String oid, QName type, QName relation) {
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

    public MetadataType requestorComment(String value) {
        setRequestorComment(value);
        return this;
    }

    public MetadataType createTimestamp(XMLGregorianCalendar value) {
        setCreateTimestamp(value);
        return this;
    }

    public MetadataType createTimestamp(String value) {
        return createTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public MetadataType creatorRef(ObjectReferenceType value) {
        setCreatorRef(value);
        return this;
    }

    public MetadataType creatorRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return creatorRef(ort);
    }

    public MetadataType creatorRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return creatorRef(ort);
    }

    public ObjectReferenceType beginCreatorRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        creatorRef(value);
        return value;
    }

    public MetadataType createApproverRef(ObjectReferenceType value) {
        getCreateApproverRef().add(value);
        return this;
    }

    public MetadataType createApproverRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return createApproverRef(ort);
    }

    public MetadataType createApproverRef(String oid, QName type, QName relation) {
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

    public MetadataType createApprovalComment(String value) {
        getCreateApprovalComment().add(value);
        return this;
    }

    public MetadataType createApprovalTimestamp(XMLGregorianCalendar value) {
        setCreateApprovalTimestamp(value);
        return this;
    }

    public MetadataType createApprovalTimestamp(String value) {
        return createApprovalTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public MetadataType createChannel(String value) {
        setCreateChannel(value);
        return this;
    }

    public MetadataType createTaskRef(ObjectReferenceType value) {
        setCreateTaskRef(value);
        return this;
    }

    public MetadataType createTaskRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return createTaskRef(ort);
    }

    public MetadataType createTaskRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return createTaskRef(ort);
    }

    public ObjectReferenceType beginCreateTaskRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        createTaskRef(value);
        return value;
    }

    public MetadataType modifyTimestamp(XMLGregorianCalendar value) {
        setModifyTimestamp(value);
        return this;
    }

    public MetadataType modifyTimestamp(String value) {
        return modifyTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public MetadataType modifierRef(ObjectReferenceType value) {
        setModifierRef(value);
        return this;
    }

    public MetadataType modifierRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return modifierRef(ort);
    }

    public MetadataType modifierRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return modifierRef(ort);
    }

    public ObjectReferenceType beginModifierRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        modifierRef(value);
        return value;
    }

    public MetadataType modifyApproverRef(ObjectReferenceType value) {
        getModifyApproverRef().add(value);
        return this;
    }

    public MetadataType modifyApproverRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return modifyApproverRef(ort);
    }

    public MetadataType modifyApproverRef(String oid, QName type, QName relation) {
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

    public MetadataType modifyApprovalComment(String value) {
        getModifyApprovalComment().add(value);
        return this;
    }

    public MetadataType modifyApprovalTimestamp(XMLGregorianCalendar value) {
        setModifyApprovalTimestamp(value);
        return this;
    }

    public MetadataType modifyApprovalTimestamp(String value) {
        return modifyApprovalTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public MetadataType modifyChannel(String value) {
        setModifyChannel(value);
        return this;
    }

    public MetadataType modifyTaskRef(ObjectReferenceType value) {
        setModifyTaskRef(value);
        return this;
    }

    public MetadataType modifyTaskRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return modifyTaskRef(ort);
    }

    public MetadataType modifyTaskRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return modifyTaskRef(ort);
    }

    public ObjectReferenceType beginModifyTaskRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        modifyTaskRef(value);
        return value;
    }

    public MetadataType lastProvisioningTimestamp(XMLGregorianCalendar value) {
        setLastProvisioningTimestamp(value);
        return this;
    }

    public MetadataType lastProvisioningTimestamp(String value) {
        return lastProvisioningTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public MetadataType certificationFinishedTimestamp(XMLGregorianCalendar value) {
        setCertificationFinishedTimestamp(value);
        return this;
    }

    public MetadataType certificationFinishedTimestamp(String value) {
        return certificationFinishedTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public MetadataType certificationOutcome(String value) {
        setCertificationOutcome(value);
        return this;
    }

    public MetadataType certifierRef(ObjectReferenceType value) {
        getCertifierRef().add(value);
        return this;
    }

    public MetadataType certifierRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return certifierRef(ort);
    }

    public MetadataType certifierRef(String oid, QName type, QName relation) {
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

    public MetadataType certifierComment(String value) {
        getCertifierComment().add(value);
        return this;
    }

    public MetadataType originMappingName(String value) {
        setOriginMappingName(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public MetadataType clone() {
        return ((MetadataType) super.clone());
    }
}
