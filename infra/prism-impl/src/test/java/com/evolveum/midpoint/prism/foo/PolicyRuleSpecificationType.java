package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
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
@XmlType(name = "PolicyRuleSpecificationType", propOrder = {
        "ruleName",
        "assignmentPath",
        "transitional"
})
public class PolicyRuleSpecificationType extends AbstractMutableContainerable {
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "PolicyRuleSpecificationType");
    public static final ItemName F_RULE_NAME = ItemName.interned(ObjectType.NS_FOO, "ruleName");
    public static final ItemName F_ASSIGNMENT_PATH = ItemName.interned(ObjectType.NS_FOO, "assignmentPath");
    public static final ItemName F_TRANSITIONAL = ItemName.interned(ObjectType.NS_FOO, "transitional");
    public static final Producer<PolicyRuleSpecificationType> FACTORY = new Producer<PolicyRuleSpecificationType>() {

        private static final long serialVersionUID = 201105211233L;

        public PolicyRuleSpecificationType run() {
            return new PolicyRuleSpecificationType();
        }

    }
            ;

    public PolicyRuleSpecificationType() {
        super();
    }

    @Deprecated
    public PolicyRuleSpecificationType(PrismContext context) {
        super();
    }

    @XmlElement(name = "ruleName")
    public String getRuleName() {
        return this.prismGetPropertyValue(F_RULE_NAME, String.class);
    }

    public void setRuleName(String value) {
        this.prismSetPropertyValue(F_RULE_NAME, value);
    }

    @XmlElement(name = "assignmentPath")
    public AssignmentPathMetadataType getAssignmentPath() {
        return this.prismGetSingleContainerable(F_ASSIGNMENT_PATH, AssignmentPathMetadataType.class);
    }

    public void setAssignmentPath(AssignmentPathMetadataType value) {
        this.prismSetSingleContainerable(F_ASSIGNMENT_PATH, value);
    }

    @XmlElement(name = "transitional")
    public Boolean isTransitional() {
        return this.prismGetPropertyValue(F_TRANSITIONAL, Boolean.class);
    }

    public Boolean getTransitional() {
        return this.prismGetPropertyValue(F_TRANSITIONAL, Boolean.class);
    }

    public void setTransitional(Boolean value) {
        this.prismSetPropertyValue(F_TRANSITIONAL, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public PolicyRuleSpecificationType ruleName(String value) {
        setRuleName(value);
        return this;
    }

    public PolicyRuleSpecificationType assignmentPath(AssignmentPathMetadataType value) {
        setAssignmentPath(value);
        return this;
    }

    public AssignmentPathMetadataType beginAssignmentPath() {
        AssignmentPathMetadataType value = new AssignmentPathMetadataType();
        assignmentPath(value);
        return value;
    }

    public PolicyRuleSpecificationType transitional(Boolean value) {
        setTransitional(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public PolicyRuleSpecificationType clone() {
        return ((PolicyRuleSpecificationType) super.clone());
    }

}
