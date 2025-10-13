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
@XmlType(name = "PolicyStatementSpecificationType", propOrder = {
        "statementId"
})
public class PolicyStatementSpecificationType extends AbstractMutableContainerable {
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "PolicyStatementSpecificationType");
    public static final ItemName F_STATEMENT_ID = ItemName.interned(ObjectType.NS_FOO, "statementId");
    public static final Producer<PolicyStatementSpecificationType> FACTORY = new Producer<PolicyStatementSpecificationType>() {

        private static final long serialVersionUID = 201105211233L;

        public PolicyStatementSpecificationType run() {
            return new PolicyStatementSpecificationType();
        }

    }
            ;

    public PolicyStatementSpecificationType() {
        super();
    }

    @Deprecated
    public PolicyStatementSpecificationType(PrismContext context) {
        super();
    }

    @XmlElement(name = "statementId")
    public Long getStatementId() {
        return this.prismGetPropertyValue(F_STATEMENT_ID, Long.class);
    }

    public void setStatementId(Long value) {
        this.prismSetPropertyValue(F_STATEMENT_ID, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public PolicyStatementSpecificationType statementId(Long value) {
        setStatementId(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public PolicyStatementSpecificationType clone() {
        return ((PolicyStatementSpecificationType) super.clone());
    }

}
