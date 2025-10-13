/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.foo;

import javax.xml.namespace.QName;

import jakarta.xml.bind.annotation.*;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableContainerable;
import com.evolveum.midpoint.prism.path.ItemName;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "UserInterfaceFeatureType", propOrder = {
        "identifier",
        "description",
        "documentation",
})
@XmlSeeAlso({
        GuiObjectListPanelConfigurationType.class,
})
public class UserInterfaceFeatureType extends AbstractMutableContainerable {

    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "UserInterfaceFeatureType");
    public static final ItemName F_IDENTIFIER = new ItemName(ObjectType.NS_FOO, "identifier");
    public static final ItemName F_DESCRIPTION = new ItemName(ObjectType.NS_FOO, "description");
    public static final ItemName F_DOCUMENTATION = new ItemName(ObjectType.NS_FOO, "documentation");
    public static final ItemName F_DISPLAY_ORDER = new ItemName(ObjectType.NS_FOO, "displayOrder");

    public UserInterfaceFeatureType() {
        super();
    }

    @Deprecated
    public UserInterfaceFeatureType(PrismContext context) {
        super();
    }

    @XmlElement(name = "identifier")
    public String getIdentifier() {
        return this.prismGetPropertyValue(F_IDENTIFIER, String.class);
    }

    public void setIdentifier(String value) {
        this.prismSetPropertyValue(F_IDENTIFIER, value);
    }

    @XmlElement(name = "description")
    public String getDescription() {
        return this.prismGetPropertyValue(F_DESCRIPTION, String.class);
    }

    public void setDescription(String value) {
        this.prismSetPropertyValue(F_DESCRIPTION, value);
    }

    @XmlElement(name = "documentation")
    public String getDocumentation() {
        return this.prismGetPropertyValue(F_DOCUMENTATION, String.class);
    }

    public void setDocumentation(String value) {
        this.prismSetPropertyValue(F_DOCUMENTATION, value);
    }

    @XmlElement(name = "displayOrder")
    public Integer getDisplayOrder() {
        return this.prismGetPropertyValue(F_DISPLAY_ORDER, Integer.class);
    }

    public void setDisplayOrder(Integer value) {
        this.prismSetPropertyValue(F_DISPLAY_ORDER, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public UserInterfaceFeatureType id(Long value) {
        setId(value);
        return this;
    }

    public UserInterfaceFeatureType identifier(String value) {
        setIdentifier(value);
        return this;
    }

    public UserInterfaceFeatureType description(String value) {
        setDescription(value);
        return this;
    }

    public UserInterfaceFeatureType documentation(String value) {
        setDocumentation(value);
        return this;
    }

    public UserInterfaceFeatureType displayOrder(Integer value) {
        setDisplayOrder(value);
        return this;
    }

    public <X> X end() {
        return ((X) ((PrismContainerValue) ((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public UserInterfaceFeatureType clone() {
        return ((UserInterfaceFeatureType) super.clone());
    }

}
