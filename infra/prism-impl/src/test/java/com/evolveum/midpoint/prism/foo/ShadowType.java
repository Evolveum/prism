/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.Objectable;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "ShadowType", propOrder = {
        "resourceRef",
        "shadowLifecycleState",
        "purpose",
        "dead",
        "deathTimestamp",
        "pendingOperation",
        "synchronizationSituation",
        "synchronizationTimestamp",
        "fullSynchronizationTimestamp",
        "synchronizationSituationDescription",
        "correlation",
        "objectClass",
        "primaryIdentifierValue",
        "auxiliaryObjectClass",
        "kind",
        "intent",
        "tag",
        "protectedObject",
        "ignored",
        "assigned",
        "exists",
        "iteration",
        "iterationToken",
        "attributes",
        "referenceAttributes",
        "associations",
        "association",
        "activation",
        "credentials",
        "cachingMetadata"
})
public class ShadowType extends ObjectType
        implements Objectable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(NS_FOO, "ShadowType");
    public static final ItemName F_RESOURCE_REF = ItemName.interned(NS_FOO, "resourceRef");
    public static final ItemName F_SHADOW_LIFECYCLE_STATE = ItemName.interned(NS_FOO, "shadowLifecycleState");
    public static final ItemName F_PURPOSE = ItemName.interned(NS_FOO, "purpose");
    public static final ItemName F_DEAD = ItemName.interned(NS_FOO, "dead");
    public static final ItemName F_DEATH_TIMESTAMP = ItemName.interned(NS_FOO, "deathTimestamp");
    public static final ItemName F_PENDING_OPERATION = ItemName.interned(NS_FOO, "pendingOperation");
    public static final ItemName F_SYNCHRONIZATION_SITUATION = ItemName.interned(NS_FOO, "synchronizationSituation");
    public static final ItemName F_SYNCHRONIZATION_TIMESTAMP = ItemName.interned(NS_FOO, "synchronizationTimestamp");
    public static final ItemName F_FULL_SYNCHRONIZATION_TIMESTAMP = ItemName.interned(NS_FOO, "fullSynchronizationTimestamp");
    public static final ItemName F_SYNCHRONIZATION_SITUATION_DESCRIPTION = ItemName.interned(NS_FOO, "synchronizationSituationDescription");
    public static final ItemName F_CORRELATION = ItemName.interned(NS_FOO, "correlation");
    public static final ItemName F_OBJECT_CLASS = ItemName.interned(NS_FOO, "objectClass");
    public static final ItemName F_PRIMARY_IDENTIFIER_VALUE = ItemName.interned(NS_FOO, "primaryIdentifierValue");
    public static final ItemName F_AUXILIARY_OBJECT_CLASS = ItemName.interned(NS_FOO, "auxiliaryObjectClass");
    public static final ItemName F_KIND = ItemName.interned(NS_FOO, "kind");
    public static final ItemName F_INTENT = ItemName.interned(NS_FOO, "intent");
    public static final ItemName F_TAG = ItemName.interned(NS_FOO, "tag");
    public static final ItemName F_PROTECTED_OBJECT = ItemName.interned(NS_FOO, "protectedObject");
    public static final ItemName F_IGNORED = ItemName.interned(NS_FOO, "ignored");
    public static final ItemName F_ASSIGNED = ItemName.interned(NS_FOO, "assigned");
    public static final ItemName F_EXISTS = ItemName.interned(NS_FOO, "exists");
    public static final ItemName F_ITERATION = ItemName.interned(NS_FOO, "iteration");
    public static final ItemName F_ITERATION_TOKEN = ItemName.interned(NS_FOO, "iterationToken");
    public static final ItemName F_ATTRIBUTES = ItemName.interned(NS_FOO, "attributes");
    public static final ItemName F_REFERENCE_ATTRIBUTES = ItemName.interned(NS_FOO, "referenceAttributes");
    public static final ItemName F_ASSOCIATIONS = ItemName.interned(NS_FOO, "associations");
    public static final ItemName F_ASSOCIATION = ItemName.interned(NS_FOO, "association");
    public static final ItemName F_ACTIVATION = ItemName.interned(NS_FOO, "activation");
    public static final ItemName F_CREDENTIALS = ItemName.interned(NS_FOO, "credentials");
    public static final ItemName F_CACHING_METADATA = ItemName.interned(NS_FOO, "cachingMetadata");
    public static final Producer<ShadowType> FACTORY = new Producer<ShadowType>() {

        private static final long serialVersionUID = 201105211233L;

        public ShadowType run() {
            return new ShadowType();
        }

    };

    public ShadowType() {
        super();
    }

    @Deprecated
    public ShadowType(PrismContext context) {
        super();
    }

    public PrismObject<ShadowType> asPrismObject() {
        return super.asPrismContainer();
    }

    @XmlElement(name = "resourceRef")
    public ObjectReferenceType getResourceRef() {
        return this.prismGetReferencable(F_RESOURCE_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setResourceRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_RESOURCE_REF, value);
    }

//    @XmlElement(name = "shadowLifecycleState")
//    public ShadowLifecycleStateType getShadowLifecycleState() {
//        return this.prismGetPropertyValue(F_SHADOW_LIFECYCLE_STATE, ShadowLifecycleStateType.class);
//    }
//
//    public void setShadowLifecycleState(ShadowLifecycleStateType value) {
//        this.prismSetPropertyValue(F_SHADOW_LIFECYCLE_STATE, value);
//    }

//    @XmlElement(name = "purpose")
//    public ShadowPurposeType getPurpose() {
//        return this.prismGetPropertyValue(F_PURPOSE, ShadowPurposeType.class);
//    }
//
//    public void setPurpose(ShadowPurposeType value) {
//        this.prismSetPropertyValue(F_PURPOSE, value);
//    }

    @XmlElement(name = "dead")
    public Boolean isDead() {
        return this.prismGetPropertyValue(F_DEAD, Boolean.class);
    }

    public Boolean getDead() {
        return this.prismGetPropertyValue(F_DEAD, Boolean.class);
    }

    public void setDead(Boolean value) {
        this.prismSetPropertyValue(F_DEAD, value);
    }

    @XmlElement(name = "deathTimestamp")
    public XMLGregorianCalendar getDeathTimestamp() {
        return this.prismGetPropertyValue(F_DEATH_TIMESTAMP, XMLGregorianCalendar.class);
    }

    public void setDeathTimestamp(XMLGregorianCalendar value) {
        this.prismSetPropertyValue(F_DEATH_TIMESTAMP, value);
    }

//    @XmlElement(name = "pendingOperation")
//    public List<PendingOperationType> getPendingOperation() {
//        return this.prismGetContainerableList(PendingOperationType.FACTORY, F_PENDING_OPERATION, PendingOperationType.class);
//    }

//    public List<PendingOperationType> createPendingOperationList() {
//        PrismForJAXBUtil.createContainer(asPrismContainerValue(), F_PENDING_OPERATION);
//        return getPendingOperation();
//    }
//
//    @XmlElement(name = "synchronizationSituation")
//    public SynchronizationSituationType getSynchronizationSituation() {
//        return this.prismGetPropertyValue(F_SYNCHRONIZATION_SITUATION, SynchronizationSituationType.class);
//    }
//
//    public void setSynchronizationSituation(SynchronizationSituationType value) {
//        this.prismSetPropertyValue(F_SYNCHRONIZATION_SITUATION, value);
//    }

    @XmlElement(name = "synchronizationTimestamp")
    public XMLGregorianCalendar getSynchronizationTimestamp() {
        return this.prismGetPropertyValue(F_SYNCHRONIZATION_TIMESTAMP, XMLGregorianCalendar.class);
    }

    public void setSynchronizationTimestamp(XMLGregorianCalendar value) {
        this.prismSetPropertyValue(F_SYNCHRONIZATION_TIMESTAMP, value);
    }

    @XmlElement(name = "fullSynchronizationTimestamp")
    public XMLGregorianCalendar getFullSynchronizationTimestamp() {
        return this.prismGetPropertyValue(F_FULL_SYNCHRONIZATION_TIMESTAMP, XMLGregorianCalendar.class);
    }

    public void setFullSynchronizationTimestamp(XMLGregorianCalendar value) {
        this.prismSetPropertyValue(F_FULL_SYNCHRONIZATION_TIMESTAMP, value);
    }

//    @XmlElement(name = "synchronizationSituationDescription")
//    public List<SynchronizationSituationDescriptionType> getSynchronizationSituationDescription() {
//        return this.prismGetPropertyValues(F_SYNCHRONIZATION_SITUATION_DESCRIPTION, SynchronizationSituationDescriptionType.class);
//    }
//
//    @XmlElement(name = "correlation")
//    public ShadowCorrelationStateType getCorrelation() {
//        return this.prismGetSingleContainerable(F_CORRELATION, ShadowCorrelationStateType.class);
//    }
//
//    public void setCorrelation(ShadowCorrelationStateType value) {
//        this.prismSetSingleContainerable(F_CORRELATION, value);
//    }

    @XmlElement(name = "objectClass")
    public QName getObjectClass() {
        return this.prismGetPropertyValue(F_OBJECT_CLASS, QName.class);
    }

    public void setObjectClass(QName value) {
        this.prismSetPropertyValue(F_OBJECT_CLASS, value);
    }

    @XmlElement(name = "primaryIdentifierValue")
    public String getPrimaryIdentifierValue() {
        return this.prismGetPropertyValue(F_PRIMARY_IDENTIFIER_VALUE, String.class);
    }

    public void setPrimaryIdentifierValue(String value) {
        this.prismSetPropertyValue(F_PRIMARY_IDENTIFIER_VALUE, value);
    }

    @XmlElement(name = "auxiliaryObjectClass")
    public List<QName> getAuxiliaryObjectClass() {
        return this.prismGetPropertyValues(F_AUXILIARY_OBJECT_CLASS, QName.class);
    }

//    @XmlElement(name = "kind")
//    public ShadowKindType getKind() {
//        return this.prismGetPropertyValue(F_KIND, ShadowKindType.class);
//    }

//    public void setKind(ShadowKindType value) {
//        this.prismSetPropertyValue(F_KIND, value);
//    }

    @XmlElement(name = "intent")
    public String getIntent() {
        return this.prismGetPropertyValue(F_INTENT, String.class);
    }

    public void setIntent(String value) {
        this.prismSetPropertyValue(F_INTENT, value);
    }

    @XmlElement(name = "tag")
    public String getTag() {
        return this.prismGetPropertyValue(F_TAG, String.class);
    }

    public void setTag(String value) {
        this.prismSetPropertyValue(F_TAG, value);
    }

    @XmlElement(name = "protectedObject")
    public Boolean isProtectedObject() {
        return this.prismGetPropertyValue(F_PROTECTED_OBJECT, Boolean.class);
    }

    public Boolean getProtectedObject() {
        return this.prismGetPropertyValue(F_PROTECTED_OBJECT, Boolean.class);
    }

    public void setProtectedObject(Boolean value) {
        this.prismSetPropertyValue(F_PROTECTED_OBJECT, value);
    }

    @XmlElement(name = "ignored")
    public Boolean isIgnored() {
        return this.prismGetPropertyValue(F_IGNORED, Boolean.class);
    }

    public Boolean getIgnored() {
        return this.prismGetPropertyValue(F_IGNORED, Boolean.class);
    }

    public void setIgnored(Boolean value) {
        this.prismSetPropertyValue(F_IGNORED, value);
    }

    @XmlElement(name = "assigned")
    public Boolean isAssigned() {
        return this.prismGetPropertyValue(F_ASSIGNED, Boolean.class);
    }

    public Boolean getAssigned() {
        return this.prismGetPropertyValue(F_ASSIGNED, Boolean.class);
    }

    public void setAssigned(Boolean value) {
        this.prismSetPropertyValue(F_ASSIGNED, value);
    }

    @XmlElement(name = "exists")
    public Boolean isExists() {
        return this.prismGetPropertyValue(F_EXISTS, Boolean.class);
    }

    public Boolean getExists() {
        return this.prismGetPropertyValue(F_EXISTS, Boolean.class);
    }

    public void setExists(Boolean value) {
        this.prismSetPropertyValue(F_EXISTS, value);
    }

    @XmlElement(name = "iteration")
    public Integer getIteration() {
        return this.prismGetPropertyValue(F_ITERATION, Integer.class);
    }

    public void setIteration(Integer value) {
        this.prismSetPropertyValue(F_ITERATION, value);
    }

    @XmlElement(name = "iterationToken")
    public String getIterationToken() {
        return this.prismGetPropertyValue(F_ITERATION_TOKEN, String.class);
    }

    public void setIterationToken(String value) {
        this.prismSetPropertyValue(F_ITERATION_TOKEN, value);
    }

//    @XmlElement(name = "attributes")
//    public ShadowAttributesType getAttributes() {
//        return this.prismGetSingleContainerable(F_ATTRIBUTES, ShadowAttributesType.class);
//    }
//
//    public void setAttributes(ShadowAttributesType value) {
//        this.prismSetSingleContainerable(F_ATTRIBUTES, value);
//    }

//    @XmlElement(name = "referenceAttributes")
//    public ShadowReferenceAttributesType getReferenceAttributes() {
//        return this.prismGetSingleContainerable(F_REFERENCE_ATTRIBUTES, ShadowReferenceAttributesType.class);
//    }
//
//    public void setReferenceAttributes(ShadowReferenceAttributesType value) {
//        this.prismSetSingleContainerable(F_REFERENCE_ATTRIBUTES, value);
//    }
//
//    @XmlElement(name = "associations")
//    public ShadowAssociationsType getAssociations() {
//        return this.prismGetSingleContainerable(F_ASSOCIATIONS, ShadowAssociationsType.class);
//    }

//    public void setAssociations(ShadowAssociationsType value) {
//        this.prismSetSingleContainerable(F_ASSOCIATIONS, value);
//    }
//
//    @XmlElement(name = "association")
//    public List<ShadowAssociationType> getAssociation() {
//        return this.prismGetContainerableList(ShadowAssociationType.FACTORY, F_ASSOCIATION, ShadowAssociationType.class);
//    }
//
//    public List<ShadowAssociationType> createAssociationList() {
//        PrismForJAXBUtil.createContainer(asPrismContainerValue(), F_ASSOCIATION);
//        return getAssociation();
//    }

    @XmlElement(name = "activation")
    public ActivationType getActivation() {
        return this.prismGetSingleContainerable(F_ACTIVATION, ActivationType.class);
    }

    public void setActivation(ActivationType value) {
        this.prismSetSingleContainerable(F_ACTIVATION, value);
    }

//    @XmlElement(name = "credentials")
//    public CredentialsType getCredentials() {
//        return this.prismGetSingleContainerable(F_CREDENTIALS, CredentialsType.class);
//    }

//    public void setCredentials(CredentialsType value) {
//        this.prismSetSingleContainerable(F_CREDENTIALS, value);
//    }

//    @XmlElement(name = "cachingMetadata")
//    public CachingMetadataType getCachingMetadata() {
//        return this.prismGetPropertyValue(F_CACHING_METADATA, CachingMetadataType.class);
//    }

    @Override
    protected QName prismGetContainerName() {
        return null;
    }

    @Override
    protected QName prismGetContainerType() {
        return null;
    }
}
