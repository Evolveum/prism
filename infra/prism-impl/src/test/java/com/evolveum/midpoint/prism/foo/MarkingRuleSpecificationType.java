/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

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
@XmlType(name = "MarkingRuleSpecificationType", propOrder = {
        "ruleId",
        "transitional"
})
public class MarkingRuleSpecificationType extends AbstractMutableContainerable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "MarkingRuleSpecificationType");
    public static final ItemName F_RULE_ID = ItemName.interned(ObjectType.NS_FOO, "ruleId");
    public static final ItemName F_TRANSITIONAL = ItemName.interned(ObjectType.NS_FOO, "transitional");
    public static final Producer<MarkingRuleSpecificationType> FACTORY = new Producer<MarkingRuleSpecificationType>() {

        private static final long serialVersionUID = 201105211233L;

        public MarkingRuleSpecificationType run() {
            return new MarkingRuleSpecificationType();
        }

    }
            ;

    public MarkingRuleSpecificationType() {
        super();
    }

    @Deprecated
    public MarkingRuleSpecificationType(PrismContext context) {
        super();
    }

    @XmlElement(name = "ruleId")
    public Long getRuleId() {
        return this.prismGetPropertyValue(F_RULE_ID, Long.class);
    }

    public void setRuleId(Long value) {
        this.prismSetPropertyValue(F_RULE_ID, value);
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

    public MarkingRuleSpecificationType ruleId(Long value) {
        setRuleId(value);
        return this;
    }

    public MarkingRuleSpecificationType transitional(Boolean value) {
        setTransitional(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public MarkingRuleSpecificationType clone() {
        return ((MarkingRuleSpecificationType) super.clone());
    }

}
