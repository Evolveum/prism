/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.xml.ns._public.query_3;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.binding.PlainStructured;
import com.evolveum.midpoint.prism.binding.StructuredEqualsStrategy;
import com.evolveum.midpoint.prism.binding.StructuredHashCodeStrategy;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

/**
 * Parameters limiting the number of returned
 * entries, offset, etc.
 * Used in the web service operations
 * such as list or search
 *
 *
 * <p>Java class for PagingType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PagingType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="orderBy" type="{http://prism.evolveum.com/xml/ns/public/types-3}XPathType" minOccurs="0"/&gt;
 *         &lt;element name="orderDirection" type="{http://prism.evolveum.com/xml/ns/public/query-2}OrderDirectionType" minOccurs="0"/&gt;
 *         &lt;element name="offset" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="maxSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PagingType", propOrder = {
        "orderBy",
        "orderDirection",
        "offset",
        "maxSize"
})
public class PagingType implements PlainStructured, Serializable, Cloneable {

    private static final long serialVersionUID = 201105211233L;

    protected ItemPathType orderBy;

    @XmlElement(defaultValue = "ascending")
    protected OrderDirectionType orderDirection;

    @XmlElement(defaultValue = "0")
    protected Integer offset;

    @XmlElement(defaultValue = "2147483647")
    protected Integer maxSize;

    public static final QName COMPLEX_TYPE = new QName(PrismConstants.NS_QUERY, "PagingType");
    public static final QName F_ORDER_DIRECTION = new QName(PrismConstants.NS_QUERY, "orderDirection");
    public static final QName F_OFFSET = new QName(PrismConstants.NS_QUERY, "offset");
    public static final QName F_MAX_SIZE = new QName(PrismConstants.NS_QUERY, "maxSize");

    /**
     * Creates a new {@code PagingType} instance.
     */
    public PagingType() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
    }

    /**
     * Creates a new {@code PagingType} instance by deeply copying a given {@code PagingType} instance.
     *
     * @param o The instance to copy.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public PagingType(final PagingType o) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'PagingType' from 'null'.");
        }
        this.orderBy = (o.orderBy == null) ? null : o.orderBy.clone();
        // CEnumLeafInfo: com.evolveum.prism.xml.ns._public.query_3.OrderDirectionType
        this.orderDirection = ((o.orderDirection == null) ? null : o.getOrderDirection());
        // CBuiltinLeafInfo: java.lang.Integer
        this.offset = ((o.offset == null) ? null : o.getOffset());
        // CBuiltinLeafInfo: java.lang.Integer
        this.maxSize = ((o.maxSize == null) ? null : o.getMaxSize());
    }

    /**
     * Gets the value of the orderBy property.
     *
     * @return possible object is
     * {@link Element }
     */
    public ItemPathType getOrderBy() {
        return orderBy;
    }

    /**
     * Sets the value of the orderBy property.
     *
     * @param value allowed object is
     * {@link Element }
     */
    public void setOrderBy(ItemPathType value) {
        this.orderBy = value;
    }

    /**
     * Gets the value of the orderDirection property.
     *
     * @return possible object is
     * {@link OrderDirectionType }
     */
    public OrderDirectionType getOrderDirection() {
        return orderDirection;
    }

    /**
     * Sets the value of the orderDirection property.
     *
     * @param value allowed object is
     * {@link OrderDirectionType }
     */
    public void setOrderDirection(OrderDirectionType value) {
        this.orderDirection = value;
    }

    /**
     * Gets the value of the offset property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getOffset() {
        return offset;
    }

    /**
     * Sets the value of the offset property.
     *
     * @param value allowed object is
     * {@link Integer }
     */
    public void setOffset(Integer value) {
        this.offset = value;
    }

    /**
     * Gets the value of the maxSize property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the value of the maxSize property.
     *
     * @param value allowed object is
     * {@link Integer }
     */
    public void setMaxSize(Integer value) {
        this.maxSize = value;
    }

    /**
     * Generates a String representation of the contents of this type.
     * This is an extension method, produced by the 'ts' xjc plugin
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode(StructuredHashCodeStrategy strategy) {
        int currentHashCode = 1;
        currentHashCode = strategy.hashCode(currentHashCode, this.getOrderBy());
        currentHashCode = strategy.hashCode(currentHashCode, this.getOrderDirection());
        currentHashCode = strategy.hashCode(currentHashCode, this.getOffset());
        currentHashCode = strategy.hashCode(currentHashCode, this.getMaxSize());
        return currentHashCode;
    }

    @Override
    public int hashCode() {
        return this.hashCode(StructuredHashCodeStrategy.DEFAULT);
    }

    @Override
    public boolean equals(Object object, StructuredEqualsStrategy strategy) {
        if (!(object instanceof PagingType)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final PagingType that = ((PagingType) object);
        ItemPathType lhsOrderBy;
        lhsOrderBy = this.getOrderBy();
        ItemPathType rhsOrderBy;
        rhsOrderBy = that.getOrderBy();
        if (!strategy.equals(lhsOrderBy, rhsOrderBy)) {
            return false;
        }
        OrderDirectionType lhsOrderDirection;
        lhsOrderDirection = this.getOrderDirection();
        OrderDirectionType rhsOrderDirection;
        rhsOrderDirection = that.getOrderDirection();
        if (!strategy.equals(lhsOrderDirection, rhsOrderDirection)) {
            return false;
        }
        Integer lhsOffset;
        lhsOffset = this.getOffset();
        Integer rhsOffset;
        rhsOffset = that.getOffset();
        if (!strategy.equals(lhsOffset, rhsOffset)) {
            return false;
        }
        Integer lhsMaxSize;
        lhsMaxSize = this.getMaxSize();
        Integer rhsMaxSize;
        rhsMaxSize = that.getMaxSize();
        return strategy.equals(lhsMaxSize, rhsMaxSize);
    }

    @Override
    public boolean equals(Object object) {
        return equals(object, StructuredEqualsStrategy.DOM_AWARE);
    }

    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @Override
    public PagingType clone() {
        try {
            // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
            final PagingType clone = ((PagingType) super.clone());
            // CWildcardTypeInfo: org.w3c.dom.Element
            clone.orderBy = ((this.orderBy == null) ? null : ((this.getOrderBy() == null) ? null : (this.getOrderBy().clone())));
            // CEnumLeafInfo: com.evolveum.prism.xml.ns._public.query_3.OrderDirectionType
            clone.orderDirection = ((this.orderDirection == null) ? null : this.getOrderDirection());
            // CBuiltinLeafInfo: java.lang.Integer
            clone.offset = ((this.offset == null) ? null : this.getOffset());
            // CBuiltinLeafInfo: java.lang.Integer
            clone.maxSize = ((this.maxSize == null) ? null : this.getMaxSize());
            return clone;
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }
}
