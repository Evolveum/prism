
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
 * <p>Java class for EventOperationFilterType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EventOperationFilterType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://midpoint.evolveum.com/xml/ns/public/common/common-3}EventHandlerType">
 *       &lt;sequence>
 *         &lt;element name="operation" type="{http://midpoint.evolveum.com/xml/ns/public/common/common-3}EventOperationType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventOperationFilterType", propOrder = {
        "operation"
})
public class EventOperationFilterType
        extends EventHandlerType
        implements Serializable, Cloneable {

    private static final long serialVersionUID = 201105211233L;
    protected List<String> operation;

    /**
     * Creates a new {@code EventOperationFilterType} instance.
     */
    public EventOperationFilterType() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
    }

    /**
     * Creates a new {@code EventOperationFilterType} instance by deeply copying a given {@code EventOperationFilterType} instance.
     *
     * @param o The instance to copy.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public EventOperationFilterType(final EventOperationFilterType o) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super(o);
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'EventOperationFilterType' from 'null'.");
        }
        // 'Operation' collection.
        if (o.operation != null) {
            copyOperation(o.getOperation(), this.getOperation());
        }
    }

    /**
     * Gets the value of the operation property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the operation property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOperation().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EventOperationType }
     */
    public List<String> getOperation() {
        if (operation == null) {
            operation = new ArrayList<>();
        }
        return this.operation;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not implemented javax - jakarta");
    }

    @Override
    public boolean equals(Object object) {
        throw new UnsupportedOperationException("Not implemented javax - jakarta");
    }

    /**
     * Copies all values of property {@code Operation} deeply.
     *
     * @param source The source to copy from.
     * @param target The target to copy {@code source} to.
     * @throws NullPointerException if {@code target} is {@code null}.
     */
    private static void copyOperation(final List<String> source, final List<String> target) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        if ((source != null) && (!source.isEmpty())) {
            for (final Object next : source) {
                if (next instanceof String) {
                    // CEnumLeafInfo: com.evolveum.midpoint.xml.ns._public.common.common_3.EventOperationType
                    target.add(((String) next));
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '" + next) + "' for property 'Operation' of class 'com.evolveum.midpoint.xml.ns._public.common.common_3.EventOperationFilterType'."));
            }
        }
    }

    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @Override
    public EventOperationFilterType clone() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        final EventOperationFilterType clone = ((EventOperationFilterType) super.clone());
        // 'Operation' collection.
        if (this.operation != null) {
            clone.operation = null;
            copyOperation(this.getOperation(), clone.getOperation());
        }
        return clone;
    }

    @Override
    public String toString() {
        return "EventOperationFilterType{" +
                "operation=" + operation +
                '}';
    }
}
