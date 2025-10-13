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
@XmlType(name = "ProvenanceAcquisitionType", propOrder = {
        "originRef",
        "resourceRef",
        "actorRef",
        "channel",
        "timestamp"
})
public class ProvenanceAcquisitionType extends AbstractMutableContainerable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "ProvenanceAcquisitionType");
    public static final ItemName F_ORIGIN_REF = ItemName.interned(ObjectType.NS_FOO, "originRef");
    public static final ItemName F_RESOURCE_REF = ItemName.interned(ObjectType.NS_FOO, "resourceRef");
    public static final ItemName F_ACTOR_REF = ItemName.interned(ObjectType.NS_FOO, "actorRef");
    public static final ItemName F_CHANNEL = ItemName.interned(ObjectType.NS_FOO, "channel");
    public static final ItemName F_TIMESTAMP = ItemName.interned(ObjectType.NS_FOO, "timestamp");
    public static final Producer<ProvenanceAcquisitionType> FACTORY = new Producer<ProvenanceAcquisitionType>() {

        private static final long serialVersionUID = 201105211233L;

        public ProvenanceAcquisitionType run() {
            return new ProvenanceAcquisitionType();
        }

    }
            ;

    public ProvenanceAcquisitionType() {
        super();
    }

    @Deprecated
    public ProvenanceAcquisitionType(PrismContext context) {
        super();
    }

    @XmlElement(name = "originRef")
    public ObjectReferenceType getOriginRef() {
        return this.prismGetReferencable(F_ORIGIN_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setOriginRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_ORIGIN_REF, value);
    }

    @XmlElement(name = "resourceRef")
    public ObjectReferenceType getResourceRef() {
        return this.prismGetReferencable(F_RESOURCE_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setResourceRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_RESOURCE_REF, value);
    }

    @XmlElement(name = "actorRef")
    public ObjectReferenceType getActorRef() {
        return this.prismGetReferencable(F_ACTOR_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setActorRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_ACTOR_REF, value);
    }

    @XmlElement(name = "channel")
    public String getChannel() {
        return this.prismGetPropertyValue(F_CHANNEL, String.class);
    }

    public void setChannel(String value) {
        this.prismSetPropertyValue(F_CHANNEL, value);
    }

    @XmlElement(name = "timestamp")
    public XMLGregorianCalendar getTimestamp() {
        return this.prismGetPropertyValue(F_TIMESTAMP, XMLGregorianCalendar.class);
    }

    public void setTimestamp(XMLGregorianCalendar value) {
        this.prismSetPropertyValue(F_TIMESTAMP, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public ProvenanceAcquisitionType originRef(ObjectReferenceType value) {
        setOriginRef(value);
        return this;
    }

    public ProvenanceAcquisitionType originRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return originRef(ort);
    }

    public ProvenanceAcquisitionType originRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return originRef(ort);
    }

    public ObjectReferenceType beginOriginRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        originRef(value);
        return value;
    }

    public ProvenanceAcquisitionType resourceRef(ObjectReferenceType value) {
        setResourceRef(value);
        return this;
    }

    public ProvenanceAcquisitionType resourceRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return resourceRef(ort);
    }

    public ProvenanceAcquisitionType resourceRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return resourceRef(ort);
    }

    public ObjectReferenceType beginResourceRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        resourceRef(value);
        return value;
    }

    public ProvenanceAcquisitionType actorRef(ObjectReferenceType value) {
        setActorRef(value);
        return this;
    }

    public ProvenanceAcquisitionType actorRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return actorRef(ort);
    }

    public ProvenanceAcquisitionType actorRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return actorRef(ort);
    }

    public ObjectReferenceType beginActorRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        actorRef(value);
        return value;
    }

    public ProvenanceAcquisitionType channel(String value) {
        setChannel(value);
        return this;
    }

    public ProvenanceAcquisitionType timestamp(XMLGregorianCalendar value) {
        setTimestamp(value);
        return this;
    }

    public ProvenanceAcquisitionType timestamp(String value) {
        return timestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public ProvenanceAcquisitionType clone() {
        return ((ProvenanceAcquisitionType) super.clone());
    }

}
