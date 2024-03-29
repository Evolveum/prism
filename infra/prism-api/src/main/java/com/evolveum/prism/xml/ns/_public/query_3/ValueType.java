/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.xml.ns._public.query_3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.*;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.binding.PlainStructured;
import com.evolveum.midpoint.prism.binding.StructuredEqualsStrategy;
import com.evolveum.midpoint.prism.binding.StructuredHashCodeStrategy;

/**
 * <p>Java class for ValueType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ValueType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;any processContents='lax' maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValueType", propOrder = {
        "content"
})
public class ValueType implements PlainStructured, Serializable, Cloneable {

    private static final long serialVersionUID = 201105211233L;

    public static final QName COMPLEX_TYPE = new QName(PrismConstants.NS_QUERY, "ValueType");

    @XmlMixed
    @XmlAnyElement
    protected List<Object> content;

    /**
     * Creates a new {@code ValueType} instance.
     */
    public ValueType() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
    }

    /**
     * Creates a new {@code ValueType} instance by deeply copying a given {@code ValueType} instance.
     *
     * @param o The instance to copy.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public ValueType(final ValueType o) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'ValueType' from 'null'.");
        }
        // 'Content' collection.
        if (o.content != null) {
            copyContent(o.getContent(), this.getContent());
        }
    }

    /**
     * Gets the value of the content property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * {@link Element }
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<>();
        }
        return this.content;
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
        List<Object> theContent;
        theContent = this.content != null && !this.content.isEmpty() ? this.getContent() : null;
        currentHashCode = strategy.hashCode(currentHashCode, theContent);
        return currentHashCode;
    }

    @Override
    public int hashCode() {
        return this.hashCode(StructuredHashCodeStrategy.DEFAULT);
    }

    @Override
    public boolean equals(Object object, StructuredEqualsStrategy strategy) {
        if (!(object instanceof ValueType)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final ValueType that = (ValueType) object;
        List<Object> lhsContent;
        lhsContent = this.content != null && !this.content.isEmpty() ? this.getContent() : null;
        List<Object> rhsContent;
        rhsContent = that.content != null && !that.content.isEmpty() ? that.getContent() : null;
        return strategy.equals(lhsContent, rhsContent);
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object object) {
        return equals(object, StructuredEqualsStrategy.DOM_AWARE);
    }

    /**
     * Copies all values of property {@code Content} deeply.
     *
     * @param source The source to copy from.
     * @param target The target to copy {@code source} to.
     * @throws NullPointerException if {@code target} is {@code null}.
     */
    private static void copyContent(final List<Object> source, final List<Object> target) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        if (source != null && !source.isEmpty()) {
            for (final Object next : source) {
                if (next instanceof Element) {
                    // CWildcardTypeInfo: org.w3c.dom.Element
                    target.add(((Element) next).cloneNode(true));
                    continue;
                }
                if (next instanceof String) {
                    // CBuiltinLeafInfo: java.lang.String
                    target.add(next);
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError("Unexpected instance '" + next + "' for property 'Content' of class 'com.evolveum.prism.xml.ns._public.query_3.ValueType'.");
            }
        }
    }

    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @Override
    public ValueType clone() {
        try {
            // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
            final ValueType clone = (ValueType) super.clone();
            // 'Content' collection.
            if (this.content != null) {
                clone.content = null;
                copyContent(this.getContent(), clone.getContent());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }

}
