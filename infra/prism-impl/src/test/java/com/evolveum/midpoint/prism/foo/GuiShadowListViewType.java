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
import com.evolveum.prism.xml.ns._public.query_3.PagingType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "GuiShadowListViewType", propOrder = {
        "resourceRef",
        "kind",
        "intent"
})
public class GuiShadowListViewType extends GuiObjectListViewType {

    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "GuiShadowListViewType");
    public static final ItemName F_RESOURCE_REF = new ItemName(ObjectType.NS_FOO, "resourceRef");
    public static final ItemName F_KIND = new ItemName(ObjectType.NS_FOO, "kind");
    public static final ItemName F_INTENT = new ItemName(ObjectType.NS_FOO, "intent");

    public GuiShadowListViewType() {
        super();
    }

    @Deprecated
    public GuiShadowListViewType(PrismContext context) {
        super();
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

    public GuiShadowListViewType id(Long value) {
        setId(value);
        return this;
    }

    public GuiShadowListViewType intent(String value) {
        setIntent(value);
        return this;
    }

    public GuiShadowListViewType type(QName value) {
        setType(value);
        return this;
    }

    public GuiShadowListViewType disableSorting(Boolean value) {
        setDisableSorting(value);
        return this;
    }

    public GuiShadowListViewType disableCounting(Boolean value) {
        setDisableCounting(value);
        return this;
    }

    public GuiShadowListViewType refreshInterval(Integer value) {
        setRefreshInterval(value);
        return this;
    }

    public GuiShadowListViewType paging(PagingType value) {
        setPaging(value);
        return this;
    }

    public GuiShadowListViewType identifier(String value) {
        setIdentifier(value);
        return this;
    }

    public GuiShadowListViewType description(String value) {
        setDescription(value);
        return this;
    }

    public GuiShadowListViewType documentation(String value) {
        setDocumentation(value);
        return this;
    }
    public GuiShadowListViewType displayOrder(Integer value) {
        setDisplayOrder(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public GuiShadowListViewType clone() {
        return ((GuiShadowListViewType) super.clone());
    }

}
