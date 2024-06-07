package com.evolveum.midpoint.prism.foo;

import jakarta.xml.bind.annotation.*;

import java.io.Serializable;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArchetypePolicyType", propOrder = {
        "display",
        "objectTemplateRef",
        "itemConstraint",
        "propertyConstraint",
        "conflictResolution",
        "lifecycleStateModel",
        "applicablePolicies",
        "expressionProfile",
        "adminGuiConfiguration",
        "assignmentHolderRelationApproach",
        "links"
})
@XmlSeeAlso({
        ObjectPolicyConfigurationType.class
})
public class ArchetypePolicyType implements Serializable {

    protected ObjectReferenceType objectTemplateRef;
    protected String expressionProfile;

    @XmlAttribute(name = "id")
    protected Long id;

    /**
     * Gets the value of the objectTemplateRef property.
     *
     * @return
     *     possible object is
     *     {@link ObjectReferenceType }
     *
     */
    public ObjectReferenceType getObjectTemplateRef() {
        return objectTemplateRef;
    }

    /**
     * Sets the value of the objectTemplateRef property.
     *
     * @param value
     *     allowed object is
     *     {@link ObjectReferenceType }
     *
     */
    public void setObjectTemplateRef(ObjectReferenceType value) {
        this.objectTemplateRef = value;
    }

    /**
     * Gets the value of the expressionProfile property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getExpressionProfile() {
        return expressionProfile;
    }

    /**
     * Sets the value of the expressionProfile property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setExpressionProfile(String value) {
        this.expressionProfile = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setId(Long value) {
        this.id = value;
    }
}
