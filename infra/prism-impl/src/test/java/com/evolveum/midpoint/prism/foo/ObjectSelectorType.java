/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableContainerable;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;

import jakarta.xml.bind.annotation.*;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "ObjectSelectorType", propOrder = {
        "name",
        "description",
        "documentation",
        "parent",
        "type",
        "subtype",
        "filter"
})
@XmlSeeAlso({
        SubjectedObjectSelectorType.class,
        LinkedObjectSelectorType.class
})
public class ObjectSelectorType extends AbstractMutableContainerable {
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "ObjectSelectorType");
    public static final ItemName F_NAME = new ItemName(ObjectType.NS_FOO, "name");
    public static final ItemName F_DESCRIPTION = new ItemName(ObjectType.NS_FOO, "description");
    public static final ItemName F_DOCUMENTATION = new ItemName(ObjectType.NS_FOO, "documentation");
    public static final ItemName F_PARENT = new ItemName(ObjectType.NS_FOO, "parent");
    public static final ItemName F_TYPE = new ItemName(ObjectType.NS_FOO, "type");
    public static final ItemName F_SUBTYPE = new ItemName(ObjectType.NS_FOO, "subtype");
    public static final ItemName F_FILTER = new ItemName(ObjectType.NS_FOO, "filter");

    public ObjectSelectorType() {
        super();
    }

    @Deprecated
    public ObjectSelectorType(PrismContext context) {
        super();
    }

    @XmlElement(name = "name")
    public String getName() {
        return this.prismGetPropertyValue(F_NAME, String.class);
    }

    public void setName(String value) {
        this.prismSetPropertyValue(F_NAME, value);
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

    @XmlElement(name = "parent")
    public ObjectParentSelectorType getParent() {
        return this.prismGetSingleContainerable(F_PARENT, ObjectParentSelectorType.class);
    }

    public void setParent(ObjectParentSelectorType value) {
        this.prismSetSingleContainerable(F_PARENT, value);
    }

    @XmlElement(name = "type")
    public QName getType() {
        return this.prismGetPropertyValue(F_TYPE, QName.class);
    }

    public void setType(QName value) {
        this.prismSetPropertyValue(F_TYPE, value);
    }

    @XmlElement(name = "subtype")
    public String getSubtype() {
        return this.prismGetPropertyValue(F_SUBTYPE, String.class);
    }

    public void setSubtype(String value) {
        this.prismSetPropertyValue(F_SUBTYPE, value);
    }

    @XmlElement(name = "filter")
    public SearchFilterType getFilter() {
        return this.prismGetPropertyValue(F_FILTER, SearchFilterType.class);
    }

    public void setFilter(SearchFilterType value) {
        this.prismSetPropertyValue(F_FILTER, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public ObjectSelectorType id(Long value) {
        setId(value);
        return this;
    }

    public ObjectSelectorType name(String value) {
        setName(value);
        return this;
    }

    public ObjectSelectorType description(String value) {
        setDescription(value);
        return this;
    }

    public ObjectSelectorType documentation(String value) {
        setDocumentation(value);
        return this;
    }

    public ObjectSelectorType parent(ObjectParentSelectorType value) {
        setParent(value);
        return this;
    }

    public ObjectParentSelectorType beginParent() {
        ObjectParentSelectorType value = new ObjectParentSelectorType();
        parent(value);
        return value;
    }

    public ObjectSelectorType type(QName value) {
        setType(value);
        return this;
    }

    public ObjectSelectorType subtype(String value) {
        setSubtype(value);
        return this;
    }

    public ObjectSelectorType filter(SearchFilterType value) {
        setFilter(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public ObjectSelectorType clone() {
        return ((ObjectSelectorType) super.clone());
    }
}
