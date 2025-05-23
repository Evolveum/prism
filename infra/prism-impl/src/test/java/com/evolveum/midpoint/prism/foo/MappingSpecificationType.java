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
@XmlType(name = "MappingSpecificationType", propOrder = {
        "assignmentId",
        "definitionObjectRef",
        "objectType",
        "associationType",
        "tag",
        "mappingName"
})
public class MappingSpecificationType extends AbstractMutableContainerable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "MappingSpecificationType");
    public static final ItemName F_ASSIGNMENT_ID = ItemName.interned(ObjectType.NS_FOO, "assignmentId");
    public static final ItemName F_DEFINITION_OBJECT_REF = ItemName.interned(ObjectType.NS_FOO, "definitionObjectRef");
    public static final ItemName F_OBJECT_TYPE = ItemName.interned(ObjectType.NS_FOO, "objectType");
    public static final ItemName F_ASSOCIATION_TYPE = ItemName.interned(ObjectType.NS_FOO, "associationType");
    public static final ItemName F_TAG = ItemName.interned(ObjectType.NS_FOO, "tag");
    public static final ItemName F_MAPPING_NAME = ItemName.interned(ObjectType.NS_FOO, "mappingName");
    public static final Producer<MappingSpecificationType> FACTORY = new Producer<MappingSpecificationType>() {

        private static final long serialVersionUID = 201105211233L;

        public MappingSpecificationType run() {
            return new MappingSpecificationType();
        }

    }
            ;

    public MappingSpecificationType() {
        super();
    }

    @Deprecated
    public MappingSpecificationType(PrismContext context) {
        super();
    }

    @XmlElement(name = "assignmentId")
    public String getAssignmentId() {
        return this.prismGetPropertyValue(F_ASSIGNMENT_ID, String.class);
    }

    public void setAssignmentId(String value) {
        this.prismSetPropertyValue(F_ASSIGNMENT_ID, value);
    }

    @XmlElement(name = "definitionObjectRef")
    public ObjectReferenceType getDefinitionObjectRef() {
        return this.prismGetReferencable(F_DEFINITION_OBJECT_REF, ObjectReferenceType.class, ObjectReferenceType.FACTORY);
    }

    public void setDefinitionObjectRef(ObjectReferenceType value) {
        this.prismSetReferencable(F_DEFINITION_OBJECT_REF, value);
    }

    @XmlElement(name = "objectType")
    public ResourceObjectTypeIdentificationType getObjectType() {
        return this.prismGetSingleContainerable(F_OBJECT_TYPE, ResourceObjectTypeIdentificationType.class);
    }

    public void setObjectType(ResourceObjectTypeIdentificationType value) {
        this.prismSetSingleContainerable(F_OBJECT_TYPE, value);
    }

    @XmlElement(name = "associationType")
    public QName getAssociationType() {
        return this.prismGetPropertyValue(F_ASSOCIATION_TYPE, QName.class);
    }

    public void setAssociationType(QName value) {
        this.prismSetPropertyValue(F_ASSOCIATION_TYPE, value);
    }

    @XmlElement(name = "tag")
    public String getTag() {
        return this.prismGetPropertyValue(F_TAG, String.class);
    }

    public void setTag(String value) {
        this.prismSetPropertyValue(F_TAG, value);
    }

    @XmlElement(name = "mappingName")
    public String getMappingName() {
        return this.prismGetPropertyValue(F_MAPPING_NAME, String.class);
    }

    public void setMappingName(String value) {
        this.prismSetPropertyValue(F_MAPPING_NAME, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public MappingSpecificationType assignmentId(String value) {
        setAssignmentId(value);
        return this;
    }

    public MappingSpecificationType definitionObjectRef(ObjectReferenceType value) {
        setDefinitionObjectRef(value);
        return this;
    }

    public MappingSpecificationType definitionObjectRef(String oid, QName type) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return definitionObjectRef(ort);
    }

    public MappingSpecificationType definitionObjectRef(String oid, QName type, QName relation) {
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid, type);
        refVal.setRelation(relation);
        ObjectReferenceType ort = new ObjectReferenceType();
        ort.setupReferenceValue(refVal);
        return definitionObjectRef(ort);
    }

    public ObjectReferenceType beginDefinitionObjectRef() {
        ObjectReferenceType value = new ObjectReferenceType();
        definitionObjectRef(value);
        return value;
    }

    public MappingSpecificationType objectType(ResourceObjectTypeIdentificationType value) {
        setObjectType(value);
        return this;
    }

    public ResourceObjectTypeIdentificationType beginObjectType() {
        ResourceObjectTypeIdentificationType value = new ResourceObjectTypeIdentificationType();
        objectType(value);
        return value;
    }

    public MappingSpecificationType associationType(QName value) {
        setAssociationType(value);
        return this;
    }

    public MappingSpecificationType tag(String value) {
        setTag(value);
        return this;
    }

    public MappingSpecificationType mappingName(String value) {
        setMappingName(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public MappingSpecificationType clone() {
        return ((MappingSpecificationType) super.clone());
    }


}
