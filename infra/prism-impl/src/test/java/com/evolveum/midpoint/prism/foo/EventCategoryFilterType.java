
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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventCategoryFilterType", propOrder = {
        "category"
})
public class EventCategoryFilterType
        extends EventHandlerType
        implements Serializable, Cloneable {

    private static final long serialVersionUID = 201105211233L;

    protected List<String> category;

    /**
     * Creates a new {@code EventCategoryFilterType} instance.
     */
    public EventCategoryFilterType() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
    }

    /**
     * Creates a new {@code EventCategoryFilterType} instance by deeply copying a given {@code EventCategoryFilterType} instance.
     *
     * @param o The instance to copy.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public EventCategoryFilterType(final EventCategoryFilterType o) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super(o);
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'EventCategoryFilterType' from 'null'.");
        }
        // 'Category' collection.
        if (o.category != null) {
            copyCategory(o.getCategory(), this.getCategory());
        }
    }

    /**
     * Gets the value of the category property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the category property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCategory().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getCategory() {
        if (category == null) {
            category = new ArrayList<>();
        }
        return this.category;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not implemented - migration to jakarta.xml.bind");
    }

    @Override
    public boolean equals(Object object) {
        throw new UnsupportedOperationException("Not implemented - migration to jakarta.xml.bind");
    }

    /**
     * Copies all values of property {@code Category} deeply.
     *
     * @param source The source to copy from.
     * @param target The target to copy {@code source} to.
     * @throws NullPointerException if {@code target} is {@code null}.
     */
    private static void copyCategory(final List<String> source, final List<String> target) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        if ((source != null) && (!source.isEmpty())) {
            for (final Object next : source) {
                if (next instanceof String) {
                    // CEnumLeafInfo: com.evolveum.midpoint.xml.ns._public.common.common_3.String
                    target.add(((String) next));
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '" + next) + "' for property 'Category' of class 'com.evolveum.midpoint.xml.ns._public.common.common_3.EventCategoryFilterType'."));
            }
        }
    }

    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @Override
    public EventCategoryFilterType clone() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        final EventCategoryFilterType clone = ((EventCategoryFilterType) super.clone());
        // 'Category' collection.
        if (this.category != null) {
            clone.category = null;
            copyCategory(this.getCategory(), clone.getCategory());
        }
        return clone;
    }

    @Override
    public String toString() {
        return "EventCategoryFilterType{" +
                "category=" + category +
                '}';
    }
}
