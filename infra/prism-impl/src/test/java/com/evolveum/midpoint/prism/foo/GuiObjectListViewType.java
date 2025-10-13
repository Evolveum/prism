/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.foo;

import javax.xml.namespace.QName;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.path.ItemName;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "GuiObjectListViewType", propOrder = {
        "type",
        "collection",
        "action",
        "additionalPanels"
})
@XmlSeeAlso({
        GuiShadowListViewType.class
})
public class GuiObjectListViewType
        extends GuiObjectListPanelConfigurationType
{
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "GuiObjectListViewType");
    public static final ItemName F_TYPE = new ItemName(ObjectType.NS_FOO, "type");

    public GuiObjectListViewType() {
        super();
    }

    @Deprecated
    public GuiObjectListViewType(PrismContext context) {
        super();
    }

    @XmlElement(name = "type")
    public QName getType() {
        return this.prismGetPropertyValue(F_TYPE, QName.class);
    }

    public void setType(QName value) {
        this.prismSetPropertyValue(F_TYPE, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public GuiObjectListViewType id(Long value) {
        setId(value);
        return this;
    }

    public GuiObjectListViewType type(QName value) {
        setType(value);
        return this;
    }

    public GuiObjectListViewType identifier(String value) {
        setIdentifier(value);
        return this;
    }

    public GuiObjectListViewType description(String value) {
        setDescription(value);
        return this;
    }

    public GuiObjectListViewType documentation(String value) {
        setDocumentation(value);
        return this;
    }


    public GuiObjectListViewType displayOrder(Integer value) {
        setDisplayOrder(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public GuiObjectListViewType clone() {
        return ((GuiObjectListViewType) super.clone());
    }

}
