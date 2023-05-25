/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.xml.ns._public.query_3;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.builder.ToStringBuilder;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.binding.PlainStructured;
import com.evolveum.midpoint.prism.binding.StructuredEqualsStrategy;
import com.evolveum.midpoint.prism.binding.StructuredHashCodeStrategy;

/**
 * <p>Java class for FilterType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="FilterType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="matching" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilterType", propOrder = {
        "matching"
})
@XmlSeeAlso({
        PropertyNoValueFilterType.class,
        PropertyComplexValueFilterType.class,
        PropertySimpleValueFilterType.class,
        UriFilterType.class,
        LogicalOperatorFilterType.class
})
public class FilterClauseType implements Serializable, Cloneable, PlainStructured {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(PrismConstants.NS_QUERY, "FilterType");
    public static final QName F_MATCHING = new QName(PrismConstants.NS_QUERY, "matching");

    protected String matching;

    /**
     * Creates a new {@code FilterType} instance.
     */
    public FilterClauseType() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
    }

    /**
     * Creates a new {@code FilterType} instance by deeply copying a given {@code FilterType} instance.
     *
     * @param o The instance to copy.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public FilterClauseType(final FilterClauseType o) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'FilterType' from 'null'.");
        }
        // CBuiltinLeafInfo: java.lang.String
        this.matching = ((o.matching == null) ? null : o.getMatching());
    }

    /**
     * Gets the value of the matching property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMatching() {
        return matching;
    }

    /**
     * Sets the value of the matching property.
     *
     * @param value allowed object is
     * {@link String }
     */
    public void setMatching(String value) {
        this.matching = value;
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
        currentHashCode = strategy.hashCode(currentHashCode, matching);
        return currentHashCode;
    }

    @Override
    public final int hashCode() {
        return this.hashCode(StructuredHashCodeStrategy.DEFAULT);
    }

    @Override
    public boolean equals(Object object, StructuredEqualsStrategy strategy) {
        if (!(object instanceof FilterClauseType)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final FilterClauseType that = ((FilterClauseType) object);
        String lhsMatching;
        lhsMatching = this.getMatching();
        String rhsMatching;
        rhsMatching = that.getMatching();

        return strategy.equals(lhsMatching, rhsMatching);
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public final boolean equals(Object object) {
        return equals(object, StructuredEqualsStrategy.DOM_AWARE);
    }

    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @Override
    public FilterClauseType clone() {
        try {
            // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
            final FilterClauseType clone = ((FilterClauseType) super.clone());
            // CBuiltinLeafInfo: java.lang.String
            clone.matching = ((this.matching == null) ? null : this.getMatching());
            return clone;
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }

}
