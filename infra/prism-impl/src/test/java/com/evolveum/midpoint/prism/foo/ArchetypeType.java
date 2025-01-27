package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.*;

import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;

import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "ArchetypeType", propOrder = {
        "archetypePolicy",
        "superArchetypeRef",
        "archetypeType",
        "securityPolicyRef"
})
public class ArchetypeType
        extends AbstractRoleType
        implements Objectable {
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(NS_FOO, "ArchetypeType");
    public static final ItemName F_ARCHETYPE_POLICY = ItemName.interned(NS_FOO, "archetypePolicy");
    public static final ItemName F_SUPER_ARCHETYPE_REF = ItemName.interned(NS_FOO, "superArchetypeRef");
    public static final ItemName F_ARCHETYPE_TYPE = ItemName.interned(NS_FOO, "archetypeType");
    public static final ItemName F_SECURITY_POLICY_REF = ItemName.interned(NS_FOO, "securityPolicyRef");
    public static final Producer<ArchetypeType> FACTORY = new Producer<ArchetypeType>() {

        private static final long serialVersionUID = 201105211233L;

        public ArchetypeType run() {
            return new ArchetypeType();
        }

    }
            ;
    public static final QName CONTAINER_NAME = new QName("http://midpoint.evolveum.com/xml/ns/public/common/common-3", "archetype");

    public ArchetypeType() {
        super();
    }

    @Deprecated
    public ArchetypeType(PrismContext context) {
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

    public PrismObject<ArchetypeType> asPrismObject() {
        return super.asPrismContainer();
    }

//    @XmlElement(name = "archetypePolicy")
//    public ArchetypePolicyType getArchetypePolicy() {
//        return this.prismGetSingleContainerable(F_ARCHETYPE_POLICY, ArchetypePolicyType.class);
//    }
//
//    public void setArchetypePolicy(ArchetypePolicyType value) {
//        this.prismSetSingleContainerable(F_ARCHETYPE_POLICY, value);
//    }

    @XmlElement(name = "superArchetypeRef")
    public ObjectReferenceType getSuperArchetypeRef() {
        return this.prismGetReferencable(F_SUPER_ARCHETYPE_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setSuperArchetypeRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_SUPER_ARCHETYPE_REF, value);
    }

    @XmlElement(name = "archetypeType")
    public ArchetypeTypeType getArchetypeType() {
        return this.prismGetPropertyValue(F_ARCHETYPE_TYPE, ArchetypeTypeType.class);
    }

    public void setArchetypeType(ArchetypeTypeType value) {
        this.prismSetPropertyValue(F_ARCHETYPE_TYPE, value);
    }

    @XmlElement(name = "securityPolicyRef")
    public ObjectReferenceType getSecurityPolicyRef() {
        return this.prismGetReferencable(F_SECURITY_POLICY_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setSecurityPolicyRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_SECURITY_POLICY_REF, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public ArchetypeType version(String value) {
        setVersion(value);
        return this;
    }

    public ArchetypeType oid(String value) {
        setOid(value);
        return this;
    }

//    public ArchetypeType archetypePolicy(ArchetypePolicyType value) {
//        setArchetypePolicy(value);
//        return this;
//    }

//    public ArchetypePolicyType beginArchetypePolicy() {
//        ArchetypePolicyType value = new ArchetypePolicyType();
//        archetypePolicy(value);
//        return value;
//    }

    public ArchetypeType superArchetypeRef(ObjectReferenceType value) {
        setSuperArchetypeRef(value);
        return this;
    }

    public ArchetypeType superArchetypeRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return superArchetypeRef(ort);
    }

    public ArchetypeType superArchetypeRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return superArchetypeRef(ort);
    }

    public ObjectReferenceType beginSuperArchetypeRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        superArchetypeRef(value);
        return value;
    }

    public ArchetypeType archetypeType(ArchetypeTypeType value) {
        setArchetypeType(value);
        return this;
    }

    public ArchetypeType securityPolicyRef(ObjectReferenceType value) {
        setSecurityPolicyRef(value);
        return this;
    }

    public ArchetypeType securityPolicyRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return securityPolicyRef(ort);
    }

    public ArchetypeType securityPolicyRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return securityPolicyRef(ort);
    }

    public ObjectReferenceType beginSecurityPolicyRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        securityPolicyRef(value);
        return value;
    }

    public ArchetypeType linkRef(ObjectReferenceType value) {
        getLinkRef().add(value);
        return this;
    }

    public ArchetypeType linkRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return linkRef(ort);
    }

    public ArchetypeType linkRef(String oid, QName type, QName relation) {
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

    public ArchetypeType locality(PolyStringType value) {
        setLocality(value);
        return this;
    }

    public ArchetypeType locality(String value) {
        return locality(PolyStringType.fromOrig(value));
    }

    public AssignmentType beginAssignment() {
        AssignmentType value = new AssignmentType();
        assignment(value);
        return value;
    }

    public ArchetypeType iteration(Integer value) {
        setIteration(value);
        return this;
    }

    public ArchetypeType iterationToken(String value) {
        setIterationToken(value);
        return this;
    }

    public ArchetypeType archetypeRef(ObjectReferenceType value) {
        getArchetypeRef().add(value);
        return this;
    }

    public ArchetypeType archetypeRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return archetypeRef(ort);
    }

    public ArchetypeType archetypeRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return archetypeRef(ort);
    }

    public ObjectReferenceType beginArchetypeRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        archetypeRef(value);
        return value;
    }

    public ArchetypeType roleMembershipRef(ObjectReferenceType value) {
        getRoleMembershipRef().add(value);
        return this;
    }

    public ArchetypeType roleMembershipRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return roleMembershipRef(ort);
    }

    public ArchetypeType roleMembershipRef(String oid, QName type, QName relation) {
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

    public ArchetypeType delegatedRef(ObjectReferenceType value) {
        getDelegatedRef().add(value);
        return this;
    }

    public ArchetypeType delegatedRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return delegatedRef(ort);
    }

    public ArchetypeType delegatedRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return delegatedRef(ort);
    }

    public ObjectReferenceType beginDelegatedRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        delegatedRef(value);
        return value;
    }

    public ArchetypeType roleInfluenceRef(ObjectReferenceType value) {
        getRoleInfluenceRef().add(value);
        return this;
    }

    public ArchetypeType roleInfluenceRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return roleInfluenceRef(ort);
    }

    public ArchetypeType roleInfluenceRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return roleInfluenceRef(ort);
    }

    public ObjectReferenceType beginRoleInfluenceRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        roleInfluenceRef(value);
        return value;
    }

    public ArchetypeType name(PolyStringType value) {
        setName(value);
        return this;
    }

    public ArchetypeType name(String value) {
        return name(PolyStringType.fromOrig(value));
    }

    public ArchetypeType description(String value) {
        setDescription(value);
        return this;
    }

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
    public ArchetypeType clone() {
        return ((ArchetypeType) super.clone());
    }

    public ArchetypeType metadata(MetadataType value) {
        setMetadata(value);
        return this;
    }

    public MetadataType beginMetadata() {
        MetadataType value = new MetadataType();
        metadata(value);
        return value;
    }
}
