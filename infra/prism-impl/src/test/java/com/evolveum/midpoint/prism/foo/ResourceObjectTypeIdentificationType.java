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
@XmlType(name = "ResourceObjectTypeIdentificationType", propOrder = {
        "kind",
        "intent"
})
public class ResourceObjectTypeIdentificationType  extends AbstractMutableContainerable  {
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "ResourceObjectTypeIdentificationType");
    public static final ItemName F_KIND = ItemName.interned(ObjectType.NS_FOO, "kind");
    public static final ItemName F_INTENT = ItemName.interned(ObjectType.NS_FOO, "intent");
    public static final Producer<ResourceObjectTypeIdentificationType> FACTORY = new Producer<ResourceObjectTypeIdentificationType>() {

        private static final long serialVersionUID = 201105211233L;

        public ResourceObjectTypeIdentificationType run() {
            return new ResourceObjectTypeIdentificationType();
        }

    }
            ;

    public ResourceObjectTypeIdentificationType() {
        super();
    }

    @Deprecated
    public ResourceObjectTypeIdentificationType(PrismContext context) {
        super();
    }

    @XmlElement(name = "kind")
    public ShadowKindType getKind() {
        return this.prismGetPropertyValue(F_KIND, ShadowKindType.class);
    }

    public void setKind(ShadowKindType value) {
        this.prismSetPropertyValue(F_KIND, value);
    }

    @XmlElement(name = "intent")
    public String getIntent() {
        return this.prismGetPropertyValue(F_INTENT, String.class);
    }

    public void setIntent(String value) {
        this.prismSetPropertyValue(F_INTENT, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public ResourceObjectTypeIdentificationType id(Long value) {
        setId(value);
        return this;
    }

    public ResourceObjectTypeIdentificationType kind(ShadowKindType value) {
        setKind(value);
        return this;
    }

    public ResourceObjectTypeIdentificationType intent(String value) {
        setIntent(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public ResourceObjectTypeIdentificationType clone() {
        return ((ResourceObjectTypeIdentificationType) super.clone());
    }
}
