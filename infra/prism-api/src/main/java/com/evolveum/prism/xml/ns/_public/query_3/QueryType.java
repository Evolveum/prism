/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.xml.ns._public.query_3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.binding.PlainStructured;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;

/**
 * TODO
 *
 *
 * <p>Java class for QueryType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="QueryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element ref="{http://prism.evolveum.com/xml/ns/public/query-2}filter"/&gt;
 *         &lt;element name="paging" type="{http://prism.evolveum.com/xml/ns/public/query-2}PagingType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryType", propOrder = {
        "description",
        "filter",
        "paging"
})
public class QueryType implements PlainStructured.WithoutStrategy, DebugDumpable {

    private static final long serialVersionUID = 201105211233L;

    public static final QName COMPLEX_TYPE = new QName(PrismConstants.NS_QUERY, "QueryType");
    public static final QName F_DESCRIPTION = new QName(PrismConstants.NS_QUERY, "description");
    public static final QName F_FILTER = new QName(PrismConstants.NS_QUERY, "filter");
    public static final QName F_PAGING = new QName(PrismConstants.NS_QUERY, "paging");

    protected String description;
    @XmlElement(required = true)
    protected SearchFilterType filter;
    protected PagingType paging;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     * {@link String }
     */
    public void setDescription(String value) {
        this.description = value;
    }

    public QueryType description(String value) {
        setDescription(value);
        return this;
    }

    /**
     * Gets the value of the filter property.
     *
     * @return possible object is
     * {@link SearchFilterType }
     */
    public SearchFilterType getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     *
     * @param value allowed object is
     * {@link SearchFilterType }
     */
    public void setFilter(SearchFilterType value) {
        this.filter = value;
    }

    public QueryType filter(SearchFilterType value) {
        setFilter(value);
        return this;
    }

    /**
     * Gets the value of the paging property.
     *
     * @return possible object is
     * {@link PagingType }
     */
    public PagingType getPaging() {
        return paging;
    }

    /**
     * Sets the value of the paging property.
     *
     * @param value allowed object is
     * {@link PagingType }
     */
    public void setPaging(PagingType value) {
        this.paging = value;
    }

    public QueryType paging(PagingType value) {
        setPaging(value);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (filter == null ? 0 : filter.hashCode());
        result = prime * result + (paging == null ? 0 : paging.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        QueryType other = (QueryType) obj;
        if (description == null) {
            if (other.description != null) { return false; }
        } else if (!description.equals(other.description)) { return false; }
        if (filter == null) {
            if (other.filter != null) { return false; }
        } else if (!filter.equals(other.filter)) { return false; }
        if (paging == null) {
            if (other.paging != null) { return false; }
        } else if (!paging.equals(other.paging)) { return false; }
        return true;
    }

    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @Override
    public QueryType clone() {
        try {
            // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
            final QueryType clone = (QueryType) super.clone();
            // CBuiltinLeafInfo: java.lang.String
            clone.description = this.description == null ? null : this.getDescription();
            // CWildcardTypeInfo: org.w3c.dom.Element
            clone.filter = this.filter == null ? null : this.getFilter() == null ? null : this.getFilter().clone();
            // CClassInfo: com.evolveum.prism.xml.ns._public.query_3.PagingType
            clone.paging = this.paging == null ? null : this.getPaging() == null ? null : this.getPaging().clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append("QueryType");
        if (description != null) {
            sb.append("\n");
            DebugUtil.debugDumpWithLabel(sb, "description", description, indent + 1);
        }
        if (filter != null) {
            sb.append("\n");
            DebugUtil.debugDumpWithLabel(sb, "filter", filter, indent + 1);
        }
        if (paging != null) {
            sb.append("\n");
            DebugUtil.debugDumpWithLabel(sb, "paging", paging.toString(), indent + 1);
        }
        return sb.toString();
    }

}
