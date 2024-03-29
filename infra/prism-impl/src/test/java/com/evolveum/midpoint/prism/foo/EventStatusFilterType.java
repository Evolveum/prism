
/*
 * Copyright (c) 2010-2014 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.foo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>Java class for EventStatusFilterType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EventStatusFilterType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://midpoint.evolveum.com/xml/ns/public/common/common-3}EventHandlerType">
 *       &lt;sequence>
 *         &lt;element name="status" type="{http://midpoint.evolveum.com/xml/ns/public/common/common-3}String" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventStatusFilterType", propOrder = {
        "status"
})
public class EventStatusFilterType
        extends EventHandlerType
        implements Serializable, Cloneable {

    private static final long serialVersionUID = 201105211233L;
    protected List<String> status;

    /**
     * Creates a new {@code EventStatusFilterType} instance.
     */
    public EventStatusFilterType() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
    }

    /**
     * Creates a new {@code EventStatusFilterType} instance by deeply copying a given {@code EventStatusFilterType} instance.
     *
     * @param o The instance to copy.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public EventStatusFilterType(final EventStatusFilterType o) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super(o);
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'EventStatusFilterType' from 'null'.");
        }
        // 'Status' collection.
        if (o.status != null) {
            copyStatus(o.getStatus(), this.getStatus());
        }
    }

    /**
     * Gets the value of the status property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the status property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStatus().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getStatus() {
        if (status == null) {
            status = new ArrayList<>();
        }
        return this.status;
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
    public boolean equals(Object object) {
        throw new UnsupportedOperationException("Not implemented javax - jakarta");
    }

    /**
     * Copies all values of property {@code Status} deeply.
     *
     * @param source The source to copy from.
     * @param target The target to copy {@code source} to.
     * @throws NullPointerException if {@code target} is {@code null}.
     */
    private static void copyStatus(final List<String> source, final List<String> target) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        if ((source != null) && (!source.isEmpty())) {
            for (final Object next : source) {
                if (next instanceof String) {
                    // CEnumLeafInfo: com.evolveum.midpoint.xml.ns._public.common.common_3.String
                    target.add(((String) next));
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '" + next) + "' for property 'Status' of class 'com.evolveum.midpoint.xml.ns._public.common.common_3.EventStatusFilterType'."));
            }
        }
    }

    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @Override
    public EventStatusFilterType clone() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        final EventStatusFilterType clone = ((EventStatusFilterType) super.clone());
        // 'Status' collection.
        if (this.status != null) {
            clone.status = null;
            copyStatus(this.getStatus(), clone.getStatus());
        }
        return clone;
    }

    @Override
    public String toString() {
        return "EventStatusFilterType{" +
                "status=" + status +
                '}';
    }
}
