/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.xml.ns._public.query_3;

import java.io.Serializable;
import jakarta.xml.bind.annotation.*;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.builder.ToStringBuilder;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.binding.StructuredEqualsStrategy;
import com.evolveum.midpoint.prism.binding.StructuredHashCodeStrategy;

/**
 * <p>Java class for UriFilterType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="UriFilterType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://prism.evolveum.com/xml/ns/public/query-2}FilterType"&gt;
 *       &lt;attribute name="uri" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UriFilterType")
public class UriFilterType
        extends FilterClauseType
        implements Serializable, Cloneable {

    private static final long serialVersionUID = 201105211233L;

    public static final QName COMPLEX_TYPE = new QName(PrismConstants.NS_QUERY, "UriFilterType");
    public static final QName F_URI = new QName(PrismConstants.NS_QUERY, "uri");

    @XmlAttribute(name = "uri")
    @XmlSchemaType(name = "anyURI")
    protected String uri;

    /**
     * Creates a new {@code UriFilterType} instance.
     */
    public UriFilterType() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
    }

    /**
     * Creates a new {@code UriFilterType} instance by deeply copying a given {@code UriFilterType} instance.
     *
     * @param o The instance to copy.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public UriFilterType(final UriFilterType o) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super(o);
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'UriFilterType' from 'null'.");
        }
        // CBuiltinLeafInfo: java.lang.String
        this.uri = ((o.uri == null) ? null : o.getUri());
    }

    /**
     * Gets the value of the uri property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     *
     * @param value allowed object is
     * {@link String }
     */
    public void setUri(String value) {
        this.uri = value;
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
        int currentHashCode = super.hashCode(strategy);
        currentHashCode = strategy.hashCode(currentHashCode, getUri());
        return currentHashCode;
    }


    @Override
    public boolean equals(Object object, StructuredEqualsStrategy strategy) {
        if (!(object instanceof UriFilterType)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (!super.equals(object, strategy)) {
            return false;
        }
        final UriFilterType that = ((UriFilterType) object);
        String lhsUri;
        lhsUri = this.getUri();
        String rhsUri;
        rhsUri = that.getUri();
        return strategy.equals(lhsUri, rhsUri);
    }

    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @Override
    public UriFilterType clone() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        final UriFilterType clone = ((UriFilterType) super.clone());
        // CBuiltinLeafInfo: java.lang.String
        clone.uri = ((this.uri == null) ? null : this.getUri());
        return clone;

    }
}
