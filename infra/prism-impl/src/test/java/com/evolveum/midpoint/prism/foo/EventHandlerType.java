
/*
 * Copyright (c) 2010-2014 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.foo;

import java.io.Serializable;
import jakarta.xml.bind.annotation.*;

/**
 * An event handler - typically either a filter, a notifier, a fork (fan-out), or a chain of handlers.
 *
 *
 * <p>Java class for EventHandlerType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EventHandlerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventHandlerType", propOrder = {

})
@XmlSeeAlso({
        EventHandlerChainType.class,
        EventCategoryFilterType.class,
        EventStatusFilterType.class,
        EventOperationFilterType.class
})
public class EventHandlerType implements Serializable, Cloneable {

    private static final long serialVersionUID = 201105211233L;
    @XmlAttribute(name = "name")
    protected String name;

    /**
     * Creates a new {@code EventHandlerType} instance.
     */
    public EventHandlerType() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
    }

    /**
     * Creates a new {@code EventHandlerType} instance by deeply copying a given {@code EventHandlerType} instance.
     *
     * @param o The instance to copy.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public EventHandlerType(final EventHandlerType o) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'EventHandlerType' from 'null'.");
        }
        // CBuiltinLeafInfo: java.lang.String
        this.name = ((o.name == null) ? null : o.getName());
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     * {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Generates a String representation of the contents of this type.
     * This is an extension method, produced by the 'ts' xjc plugin
     */

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not implemented javax - jakarta");
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object object) {
        throw new UnsupportedOperationException("Not implemented javax - jakarta");
    }

    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @Override
    public EventHandlerType clone() {
        try {
            // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
            final EventHandlerType clone = ((EventHandlerType) super.clone());
            // CBuiltinLeafInfo: java.lang.String
            clone.name = ((this.name == null) ? null : this.getName());
            return clone;
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }

    @Override
    public String toString() {
        return "EventHandlerType{" +
                "name='" + name + '\'' +
                '}';
    }
}
