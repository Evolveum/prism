package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.*;

import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.prism.path.ItemName;

import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

import jakarta.xml.bind.annotation.*;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "AssignmentHolderType", propOrder = {
        "assignment",
        "iteration",
        "iterationToken",
        "archetypeRef",
        "roleMembershipRef",
        "delegatedRef",
        "roleInfluenceRef"
})
@XmlSeeAlso({
        ConnectorType.class,
        ResourceType.class,
        FocusType.class,
        ResourceType.class,
        FocusType.class,
})
public abstract class AssignmentHolderType extends ObjectType implements Objectable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(NS_FOO, "AssignmentHolderType");
    public static final ItemName F_ASSIGNMENT = ItemName.interned(NS_FOO, "assignment");
    public static final ItemName F_ITERATION = ItemName.interned(NS_FOO, "iteration");
    public static final ItemName F_ITERATION_TOKEN = ItemName.interned(NS_FOO, "iterationToken");
    public static final ItemName F_ARCHETYPE_REF = ItemName.interned(NS_FOO, "archetypeRef");
    public static final ItemName F_ROLE_MEMBERSHIP_REF = ItemName.interned(NS_FOO, "roleMembershipRef");
    public static final ItemName F_DELEGATED_REF = ItemName.interned(NS_FOO, "delegatedRef");
    public static final ItemName F_ROLE_INFLUENCE_REF = ItemName.interned(NS_FOO, "roleInfluenceRef");
    public static final QName CONTAINER_NAME = new QName("http://midpoint.evolveum.com/xml/ns/public/common/common-3", "assignmentHolder");

    public AssignmentHolderType() {
        super();
    }

    @Deprecated
    public AssignmentHolderType(PrismContext context) {
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

    public PrismObject<? extends AssignmentHolderType> asPrismObject() {
        return super.asPrismContainer();
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

    @XmlElement(name = "roleMembershipRef")
    public List<ObjectReferenceType> getRoleMembershipRef() {
        return this.prismGetReferencableList(ObjectReferenceType.FACTORY, F_ROLE_MEMBERSHIP_REF, ObjectReferenceType.class);
    }

    @XmlElement(name = "archetypeRef")
    public List<ObjectReferenceType> getArchetypeRef() {
        return this.prismGetReferencableList(ObjectReferenceType.FACTORY, F_ARCHETYPE_REF, ObjectReferenceType.class);
    }

    @XmlElement(name = "delegatedRef")
    public List<ObjectReferenceType> getDelegatedRef() {
        return this.prismGetReferencableList(ObjectReferenceType.FACTORY, F_DELEGATED_REF, ObjectReferenceType.class);
    }

    @XmlElement(name = "roleInfluenceRef")
    public List<ObjectReferenceType> getRoleInfluenceRef() {
        return this.prismGetReferencableList(ObjectReferenceType.FACTORY, F_ROLE_INFLUENCE_REF, ObjectReferenceType.class);
    }

    public void setIterationToken(String value) {
        this.prismSetPropertyValue(F_ITERATION_TOKEN, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public AssignmentHolderType version(String value) {
        setVersion(value);
        return this;
    }

    public AssignmentHolderType oid(String value) {
        setOid(value);
        return this;
    }

    public AssignmentHolderType assignment(AssignmentType value) {
        return this;
    }

    public AssignmentType beginAssignment() {
        AssignmentType value = new AssignmentType();
        assignment(value);
        return value;
    }

    public AssignmentHolderType iteration(Integer value) {
        setIteration(value);
        return this;
    }

    public AssignmentHolderType iterationToken(String value) {
        setIterationToken(value);
        return this;
    }

    public AssignmentHolderType archetypeRef(ObjectReferenceType value) {
        return this;
    }

    public AssignmentHolderType archetypeRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        return archetypeRef(ort);
    }

    public AssignmentHolderType archetypeRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        return archetypeRef(ort);
    }

    public ObjectReferenceType beginArchetypeRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        archetypeRef(value);
        return value;
    }

    public AssignmentHolderType roleMembershipRef(ObjectReferenceType value) {
        return this;
    }

    public AssignmentHolderType roleMembershipRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        return roleMembershipRef(ort);
    }

    public AssignmentHolderType roleMembershipRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        return roleMembershipRef(ort);
    }

    public ObjectReferenceType beginRoleMembershipRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        roleMembershipRef(value);
        return value;
    }

    public AssignmentHolderType delegatedRef(ObjectReferenceType value) {
        return this;
    }

    public AssignmentHolderType delegatedRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        return delegatedRef(ort);
    }

    public AssignmentHolderType delegatedRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        return delegatedRef(ort);
    }

    public ObjectReferenceType beginDelegatedRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        delegatedRef(value);
        return value;
    }

    public AssignmentHolderType roleInfluenceRef(ObjectReferenceType value) {
        return this;
    }

    public AssignmentHolderType roleInfluenceRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        return roleInfluenceRef(ort);
    }

    public AssignmentHolderType roleInfluenceRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        return roleInfluenceRef(ort);
    }

    public ObjectReferenceType beginRoleInfluenceRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        roleInfluenceRef(value);
        return value;
    }

    public AssignmentHolderType name(PolyStringType value) {
        setName(value);
        return this;
    }

    public AssignmentHolderType name(String value) {
        return name(PolyStringType.fromOrig(value));
    }

    public AssignmentHolderType description(String value) {
        setDescription(value);
        return this;
    }

    public AssignmentHolderType parentOrgRef(ObjectReferenceType value) {
        return this;
    }

    public AssignmentHolderType parentOrgRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        return parentOrgRef(ort);
    }

    public AssignmentHolderType parentOrgRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        return parentOrgRef(ort);
    }

    public ObjectReferenceType beginParentOrgRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        parentOrgRef(value);
        return value;
    }

    public AssignmentHolderType tenantRef(ObjectReferenceType value) {
        return this;
    }

    public AssignmentHolderType tenantRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        return tenantRef(ort);
    }

    public AssignmentHolderType tenantRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        return tenantRef(ort);
    }

    public ObjectReferenceType beginTenantRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        tenantRef(value);
        return value;
    }

    public AssignmentHolderType lifecycleState(String value) {
        return this;
    }

    public AssignmentHolderType policySituation(String value) {
        return this;
    }



    public AssignmentHolderType indestructible(Boolean value) {
        return this;
    }

    public AssignmentHolderType effectiveMarkRef(ObjectReferenceType value) {
        return this;
    }

    public AssignmentHolderType effectiveMarkRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        return effectiveMarkRef(ort);
    }

    public AssignmentHolderType effectiveMarkRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        return effectiveMarkRef(ort);
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
    public AssignmentHolderType clone() {
        return ((AssignmentHolderType) super.clone());
    }

}
