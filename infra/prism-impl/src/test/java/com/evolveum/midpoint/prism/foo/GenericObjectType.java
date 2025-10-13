/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.*;

import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.util.Producer;

import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "GenericObjectType", propOrder = {

})
public class GenericObjectType
    extends FocusType
    implements Objectable
{
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(NS_FOO, "GenericObjectType");
    public static final Producer<GenericObjectType> FACTORY = new Producer<GenericObjectType>() {

        private static final long serialVersionUID = 201105211233L;

        public GenericObjectType run() {
            return new GenericObjectType();
        }

    };
    public GenericObjectType() {
        super();
    }

    @Deprecated
    public GenericObjectType(PrismContext context) {
        super();
    }


    @Override
    protected QName prismGetContainerName() {
        return CONTAINER_NAME;
    }

    @Override
    protected QName prismGetContainerType() {
        return COMPLEX_TYPE;
    }



    public PrismObject<GenericObjectType> asPrismObject() {
        return super.asPrismContainer();
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public GenericObjectType version(String value) {
        setVersion(value);
        return this;
    }

    public GenericObjectType oid(String value) {
        setOid(value);
        return this;
    }


    public GenericObjectType linkRef(ObjectReferenceType value) {
        getLinkRef().add(value);
        return this;
    }

    public GenericObjectType linkRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return linkRef(ort);
    }

    public GenericObjectType linkRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return linkRef(ort);
    }

    public ObjectReferenceType beginLinkRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        linkRef(value);
        return value;
    }

//    public GenericObjectType personaRef(ObjectReferenceType value) {
//        getPersonaRef().add(value);
//        return this;
//    }
//
//    public GenericObjectType personaRef(String oid, QName type) {
//        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
//        ObjectReferenceType ort = new ObjectReferenceType();
//        ort.setupReferenceValue(refVal);
//        return personaRef(ort);
//    }
//
//    public GenericObjectType personaRef(String oid, QName type, QName relation) {
//        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
//        refVal.setRelation(relation);
//        ObjectReferenceType ort = new ObjectReferenceType();
//        ort.setupReferenceValue(refVal);
//        return personaRef(ort);
//    }

//    public ObjectReferenceType beginPersonaRef() {
//        ObjectReferenceType value = new ObjectReferenceType();
//        personaRef(value);
//        return value;
//    }

    public GenericObjectType locality(PolyStringType value) {
        setLocality(value);
        return this;
    }

    public GenericObjectType locality(String value) {
        return locality(PolyStringType.fromOrig(value));
    }

    public AssignmentType beginAssignment() {
        AssignmentType value = new AssignmentType();
        assignment(value);
        return value;
    }

    public GenericObjectType iteration(Integer value) {
        setIteration(value);
        return this;
    }

    public GenericObjectType iterationToken(String value) {
        setIterationToken(value);
        return this;
    }

//    public GenericObjectType archetypeRef(ObjectReferenceType value) {
//        getArchetypeRef().add(value);
//        return this;
//    }

//    public GenericObjectType archetypeRef(String oid, QName type) {
//        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
//        ObjectReferenceType ort = new ObjectReferenceType();
//        ort.setupReferenceValue(refVal);
//        return archetypeRef(ort);
//    }
//
//    public GenericObjectType archetypeRef(String oid, QName type, QName relation) {
//        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
//        refVal.setRelation(relation);
//        ObjectReferenceType ort = new ObjectReferenceType();
//        ort.setupReferenceValue(refVal);
//        return archetypeRef(ort);
//    }

    public ObjectReferenceType beginArchetypeRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        archetypeRef(value);
        return value;
    }

    public GenericObjectType roleMembershipRef(ObjectReferenceType value) {
        getRoleMembershipRef().add(value);
        return this;
    }

    public GenericObjectType roleMembershipRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return roleMembershipRef(ort);
    }

    public GenericObjectType roleMembershipRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return roleMembershipRef(ort);
    }

    public ObjectReferenceType beginRoleMembershipRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        roleMembershipRef(value);
        return value;
    }

//    public GenericObjectType delegatedRef(ObjectReferenceType value) {
//        getDelegatedRef().add(value);
//        return this;
//    }
//
//    public GenericObjectType delegatedRef(String oid, QName type) {
//        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
//        ObjectReferenceType ort = new ObjectReferenceType();
//        ort.setupReferenceValue(refVal);
//        return delegatedRef(ort);
//    }
//
//    public GenericObjectType delegatedRef(String oid, QName type, QName relation) {
//        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
//        refVal.setRelation(relation);
//        ObjectReferenceType ort = new ObjectReferenceType();
//        ort.setupReferenceValue(refVal);
//        return delegatedRef(ort);
//    }

    public ObjectReferenceType beginDelegatedRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        delegatedRef(value);
        return value;
    }

//    public GenericObjectType roleInfluenceRef(ObjectReferenceType value) {
//        getRoleInfluenceRef().add(value);
//        return this;
//    }

//    public GenericObjectType roleInfluenceRef(String oid, QName type) {
//        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
//        ObjectReferenceType ort = new ObjectReferenceType();
//        ort.setupReferenceValue(refVal);
//        return roleInfluenceRef(ort);
//    }
//
//    public GenericObjectType roleInfluenceRef(String oid, QName type, QName relation) {
//        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
//        refVal.setRelation(relation);
//        ObjectReferenceType ort = new ObjectReferenceType();
//        ort.setupReferenceValue(refVal);
//        return roleInfluenceRef(ort);
//    }

    public ObjectReferenceType beginRoleInfluenceRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        roleInfluenceRef(value);
        return value;
    }

    public GenericObjectType name(PolyStringType value) {
        setName(value);
        return this;
    }

    public GenericObjectType name(String value) {
        return name(PolyStringType.fromOrig(value));
    }

    public GenericObjectType description(String value) {
        setDescription(value);
        return this;
    }

//    public GenericObjectType parentOrgRef(ObjectReferenceType value) {
//        getParentOrgRef().add(value);
//        return this;
//    }

    public ObjectReferenceType beginParentOrgRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        parentOrgRef(value);
        return value;
    }

    public ObjectReferenceType beginTenantRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        tenantRef(value);
        return value;
    }

    public ObjectReferenceType beginEffectiveMarkRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        effectiveMarkRef(value);
        return value;
    }


    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public GenericObjectType clone() {
        return ((GenericObjectType) super.clone());
    }


}
