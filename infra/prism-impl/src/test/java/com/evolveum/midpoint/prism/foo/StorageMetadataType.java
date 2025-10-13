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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "StorageMetadataType", propOrder = {
        "createTimestamp",
        "creatorRef",
        "createChannel",
        "createTaskRef",
        "modifyTimestamp",
        "modifierRef",
        "modifyChannel",
        "modifyTaskRef"
})
public class StorageMetadataType extends AbstractMutableContainerable
{
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "StorageMetadataType");
    public static final ItemName F_CREATE_TIMESTAMP = ItemName.interned(ObjectType.NS_FOO, "createTimestamp");
    public static final ItemName F_CREATOR_REF = ItemName.interned(ObjectType.NS_FOO, "creatorRef");
    public static final ItemName F_CREATE_CHANNEL = ItemName.interned(ObjectType.NS_FOO, "createChannel");
    public static final ItemName F_CREATE_TASK_REF = ItemName.interned(ObjectType.NS_FOO, "createTaskRef");
    public static final ItemName F_MODIFY_TIMESTAMP = ItemName.interned(ObjectType.NS_FOO, "modifyTimestamp");
    public static final ItemName F_MODIFIER_REF = ItemName.interned(ObjectType.NS_FOO, "modifierRef");
    public static final ItemName F_MODIFY_CHANNEL = ItemName.interned(ObjectType.NS_FOO, "modifyChannel");
    public static final ItemName F_MODIFY_TASK_REF = ItemName.interned(ObjectType.NS_FOO, "modifyTaskRef");
    public static final Producer<StorageMetadataType> FACTORY = new Producer<StorageMetadataType>() {

        private static final long serialVersionUID = 201105211233L;

        public StorageMetadataType run() {
            return new StorageMetadataType();
        }

    }
            ;

    public StorageMetadataType() {
        super();
    }

    @Deprecated
    public StorageMetadataType(PrismContext context) {
        super();
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

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public StorageMetadataType createTimestamp(XMLGregorianCalendar value) {
        setCreateTimestamp(value);
        return this;
    }

    public StorageMetadataType createTimestamp(String value) {
        return createTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public StorageMetadataType creatorRef(ObjectReferenceType value) {
        setCreatorRef(value);
        return this;
    }

    public StorageMetadataType creatorRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return creatorRef(ort);
    }

    public StorageMetadataType creatorRef(String oid, QName type, QName relation) {
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

    public StorageMetadataType createChannel(String value) {
        setCreateChannel(value);
        return this;
    }

    public StorageMetadataType createTaskRef(ObjectReferenceType value) {
        setCreateTaskRef(value);
        return this;
    }

    public StorageMetadataType createTaskRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return createTaskRef(ort);
    }

    public StorageMetadataType createTaskRef(String oid, QName type, QName relation) {
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

    public StorageMetadataType modifyTimestamp(XMLGregorianCalendar value) {
        setModifyTimestamp(value);
        return this;
    }

    public StorageMetadataType modifyTimestamp(String value) {
        return modifyTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public StorageMetadataType modifierRef(ObjectReferenceType value) {
        setModifierRef(value);
        return this;
    }

    public StorageMetadataType modifierRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return modifierRef(ort);
    }

    public StorageMetadataType modifierRef(String oid, QName type, QName relation) {
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

    public StorageMetadataType modifyChannel(String value) {
        setModifyChannel(value);
        return this;
    }

    public StorageMetadataType modifyTaskRef(ObjectReferenceType value) {
        setModifyTaskRef(value);
        return this;
    }

    public StorageMetadataType modifyTaskRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return modifyTaskRef(ort);
    }

    public StorageMetadataType modifyTaskRef(String oid, QName type, QName relation) {
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

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public StorageMetadataType clone() {
        return ((StorageMetadataType) super.clone());
    }

}
