/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.xml.ns._public.query_3;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.commons.lang.builder.ToStringBuilder;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.binding.StructuredEqualsStrategy;

/**
 * <p>Java class for LogicalOperatorFilterType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="LogicalOperatorFilterType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://prism.evolveum.com/xml/ns/public/query-2}FilterType"&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LogicalOperatorFilterType")
@XmlSeeAlso({
        NAryLogicalOperatorFilterType.class,
        UnaryLogicalOperatorFilterType.class
})
public abstract class LogicalOperatorFilterType
        extends FilterClauseType
        implements Serializable, Cloneable {

    private static final long serialVersionUID = 201105211233L;

    public static final QName COMPLEX_TYPE = new QName(PrismConstants.NS_QUERY, "LogicalOperatorFilterType");

    /**
     * Creates a new {@code LogicalOperatorFilterType} instance.
     */
    public LogicalOperatorFilterType() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
    }

    /**
     * Creates a new {@code LogicalOperatorFilterType} instance by deeply copying a given {@code LogicalOperatorFilterType} instance.
     *
     * @param o The instance to copy.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public LogicalOperatorFilterType(final LogicalOperatorFilterType o) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super(o);
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'LogicalOperatorFilterType' from 'null'.");
        }
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
    public boolean equals(Object object, StructuredEqualsStrategy strategy) {
        if (!(object instanceof LogicalOperatorFilterType)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        return super.equals(object, strategy);
    }


    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @Override
    public LogicalOperatorFilterType clone() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        return ((LogicalOperatorFilterType) super.clone());
    }
}
