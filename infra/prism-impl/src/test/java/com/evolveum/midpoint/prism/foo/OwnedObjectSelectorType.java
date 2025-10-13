/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.path.ItemName;

import com.evolveum.midpoint.util.Producer;

import jakarta.xml.bind.annotation.*;

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OwnedObjectSelectorType", propOrder = {
        "owner",
        "delegator",
        "requester",
        "assignee",
        "candidateAssignee",
        "relatedObject",
        "tenant"
})
@XmlSeeAlso({
})
public class OwnedObjectSelectorType extends SubjectedObjectSelectorType implements Serializable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "OwnedObjectSelectorType");
    public static final ItemName F_OWNER = new ItemName(ObjectType.NS_FOO, "owner");
    public static final ItemName F_DELEGATOR = new ItemName(ObjectType.NS_FOO, "delegator");
    public static final Producer<OwnedObjectSelectorType> FACTORY = new Producer<OwnedObjectSelectorType>() {
        private static final long serialVersionUID = 201105211233L;

        public OwnedObjectSelectorType run() {
            return new OwnedObjectSelectorType();
        }

    };

    public OwnedObjectSelectorType() {
        super();
    }

    @Deprecated
    public OwnedObjectSelectorType(PrismContext context) {
        super();
    }

    @XmlElement(name = "owner")
    public SubjectedObjectSelectorType getOwner() {
        return this.prismGetSingleContainerable(F_OWNER, SubjectedObjectSelectorType.class);
    }

    public void setOwner(SubjectedObjectSelectorType value) {
        this.prismSetSingleContainerable(F_OWNER, value);
    }

    @XmlElement(name = "delegator")
    public SubjectedObjectSelectorType getDelegator() {
        return this.prismGetSingleContainerable(F_DELEGATOR, SubjectedObjectSelectorType.class);
    }

    public void setDelegator(SubjectedObjectSelectorType value) {
        this.prismSetSingleContainerable(F_DELEGATOR, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public OwnedObjectSelectorType id(Long value) {
        setId(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public OwnedObjectSelectorType clone() {
        return ((OwnedObjectSelectorType) super.clone());
    }
}
