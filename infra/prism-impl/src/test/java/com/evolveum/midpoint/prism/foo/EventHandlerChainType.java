
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
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventHandlerChainType", propOrder = {
        "handler"
})
public class EventHandlerChainType
        extends EventHandlerType
        implements Serializable, Cloneable {

    public static final QName COMPLEX_TYPE = new QName("http://midpoint.evolveum.com/xml/ns/test/foo-1.xsd", "EventHandlerChainType");

    private static final long serialVersionUID = 201105211233L;

    @XmlElementRef(name = "handler", namespace = "http://midpoint.evolveum.com/xml/ns/test/foo-1.xsd", type = JAXBElement.class, required = false)
    protected List<JAXBElement<? extends EventHandlerType>> handler;

    /**
     * Creates a new {@code EventHandlerChainType} instance.
     */
    public EventHandlerChainType() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
    }

    /**
     * Creates a new {@code EventHandlerChainType} instance by deeply copying a given {@code EventHandlerChainType} instance.
     *
     * @param o The instance to copy.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public EventHandlerChainType(final EventHandlerChainType o) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super(o);
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'EventHandlerChainType' from 'null'.");
        }
        // 'Handler' collection.
        if (o.handler != null) {
            copyHandler(o.getHandler(), this.getHandler());
        }
    }

    /**
     * Gets the value of the handler property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the handler property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHandler().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link SimpleWorkflowNotifierType }{@code >}
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link SimpleResourceObjectNotifierType }{@code >}
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link EventHandlerChainType }{@code >}
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link DummyNotifierType }{@code >}
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link UserPasswordNotifierType }{@code >}
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link AccountPasswordNotifierType }{@code >}
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link EventCategoryFilterType }{@code >}
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link EventHandlerType }{@code >}
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link EventStatusFilterType }{@code >}
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link EventExpressionFilterType }{@code >}
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link EventHandlerForkType }{@code >}
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link EventOperationFilterType }{@code >}
     * {@link jakarta.xml.bind.JAXBElement }{@code <}{@link SimpleUserNotifierType }{@code >}
     */
    public List<JAXBElement<? extends EventHandlerType>> getHandler() {
        if (handler == null) {
            handler = new ArrayList<>();
        }
        return this.handler;
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
     * Copies all values of property {@code Handler} deeply.
     *
     * @param source The source to copy from.
     * @param target The target to copy {@code source} to.
     * @throws NullPointerException if {@code target} is {@code null}.
     */
    @SuppressWarnings("unchecked")
    private static void copyHandler(final List<JAXBElement<? extends EventHandlerType>> source, final List<JAXBElement<? extends EventHandlerType>> target) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        if ((source != null) && (!source.isEmpty())) {
            for (final Object next : source) {
                if (next instanceof JAXBElement) {
                    // Referenced elements without classes.
                    if (((JAXBElement) next).getValue() instanceof EventOperationFilterType) {
                        // CElementInfo: jakarta.xml.bind.JAXBElement<com.evolveum.midpoint.xml.ns._public.common.common_3.EventOperationFilterType>
                        target.add(copyOfEventOperationFilterTypeElement(((JAXBElement) next)));
                        continue;
                    }
                    if (((JAXBElement) next).getValue() instanceof EventStatusFilterType) {
                        // CElementInfo: jakarta.xml.bind.JAXBElement<com.evolveum.midpoint.xml.ns._public.common.common_3.EventStatusFilterType>
                        target.add(copyOfEventStatusFilterTypeElement(((JAXBElement) next)));
                        continue;
                    }
                    if (((JAXBElement) next).getValue() instanceof EventCategoryFilterType) {
                        // CElementInfo: jakarta.xml.bind.JAXBElement<com.evolveum.midpoint.xml.ns._public.common.common_3.EventCategoryFilterType>
                        target.add(copyOfEventCategoryFilterTypeElement(((JAXBElement) next)));
                        continue;
                    }
                    if (((JAXBElement) next).getValue() instanceof EventHandlerChainType) {
                        // CElementInfo: jakarta.xml.bind.JAXBElement<com.evolveum.midpoint.xml.ns._public.common.common_3.EventHandlerChainType>
                        target.add(copyOfEventHandlerChainTypeElement(((JAXBElement) next)));
                        continue;
                    }
                    if (((JAXBElement) next).getValue() instanceof EventHandlerType) {
                        // CElementInfo: jakarta.xml.bind.JAXBElement<com.evolveum.midpoint.xml.ns._public.common.common_3.EventHandlerType>
                        target.add(copyOfEventHandlerTypeElement(((JAXBElement) next)));
                        continue;
                    }
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '" + next) + "' for property 'Handler' of class 'com.evolveum.midpoint.xml.ns._public.common.common_3.EventHandlerChainType'."));
            }
        }
    }

    /**
     * Creates and returns a deep copy of a given {@code jakarta.xml.bind.JAXBElement<com.evolveum.midpoint.xml.ns._public.common.common_3.EventOperationFilterType>} instance.
     *
     * @param e The instance to copy or {@code null}.
     * @return A deep copy of {@code e} or {@code null} if {@code e} is {@code null}.
     */
    private static JAXBElement<EventOperationFilterType> copyOfEventOperationFilterTypeElement(
            final JAXBElement<EventOperationFilterType> e) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        if (e != null) {
            final JAXBElement<EventOperationFilterType> copy = new JAXBElement<>(e.getName(), e.getDeclaredType(), e.getScope(), e.getValue());
            copy.setNil(e.isNil());
            // CClassInfo: com.evolveum.midpoint.xml.ns._public.common.common_3.EventOperationFilterType
            copy.setValue(((copy.getValue() == null) ? null : copy.getValue().clone()));
            return copy;
        }
        return null;
    }

    /**
     * Creates and returns a deep copy of a given {@code jakarta.xml.bind.JAXBElement<com.evolveum.midpoint.xml.ns._public.common.common_3.EventStatusFilterType>} instance.
     *
     * @param e The instance to copy or {@code null}.
     * @return A deep copy of {@code e} or {@code null} if {@code e} is {@code null}.
     */
    private static JAXBElement<EventStatusFilterType> copyOfEventStatusFilterTypeElement(final JAXBElement<EventStatusFilterType> e) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        if (e != null) {
            final JAXBElement<EventStatusFilterType> copy = new JAXBElement<>(e.getName(), e.getDeclaredType(), e.getScope(), e.getValue());
            copy.setNil(e.isNil());
            // CClassInfo: com.evolveum.midpoint.xml.ns._public.common.common_3.EventStatusFilterType
            copy.setValue(((copy.getValue() == null) ? null : copy.getValue().clone()));
            return copy;
        }
        return null;
    }

    /**
     * Creates and returns a deep copy of a given {@code jakarta.xml.bind.JAXBElement<com.evolveum.midpoint.xml.ns._public.common.common_3.EventCategoryFilterType>} instance.
     *
     * @param e The instance to copy or {@code null}.
     * @return A deep copy of {@code e} or {@code null} if {@code e} is {@code null}.
     */
    private static JAXBElement<EventCategoryFilterType> copyOfEventCategoryFilterTypeElement(final JAXBElement<EventCategoryFilterType> e) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        if (e != null) {
            final JAXBElement<EventCategoryFilterType> copy = new JAXBElement<>(e.getName(), e.getDeclaredType(), e.getScope(), e.getValue());
            copy.setNil(e.isNil());
            // CClassInfo: com.evolveum.midpoint.xml.ns._public.common.common_3.EventCategoryFilterType
            copy.setValue(((copy.getValue() == null) ? null : copy.getValue().clone()));
            return copy;
        }
        return null;
    }

    /**
     * Creates and returns a deep copy of a given {@code jakarta.xml.bind.JAXBElement<com.evolveum.midpoint.xml.ns._public.common.common_3.EventHandlerChainType>} instance.
     *
     * @param e The instance to copy or {@code null}.
     * @return A deep copy of {@code e} or {@code null} if {@code e} is {@code null}.
     */
    private static JAXBElement<EventHandlerChainType> copyOfEventHandlerChainTypeElement(final JAXBElement<EventHandlerChainType> e) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        if (e != null) {
            final JAXBElement<EventHandlerChainType> copy = new JAXBElement<>(e.getName(), e.getDeclaredType(), e.getScope(), e.getValue());
            copy.setNil(e.isNil());
            // CClassInfo: com.evolveum.midpoint.xml.ns._public.common.common_3.EventHandlerChainType
            copy.setValue(((copy.getValue() == null) ? null : copy.getValue().clone()));
            return copy;
        }
        return null;
    }

    /**
     * Creates and returns a deep copy of a given {@code jakarta.xml.bind.JAXBElement<com.evolveum.midpoint.xml.ns._public.common.common_3.EventHandlerType>} instance.
     *
     * @param e The instance to copy or {@code null}.
     * @return A deep copy of {@code e} or {@code null} if {@code e} is {@code null}.
     */
    private static JAXBElement<EventHandlerType> copyOfEventHandlerTypeElement(final JAXBElement<EventHandlerType> e) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        if (e != null) {
            final JAXBElement<EventHandlerType> copy = new JAXBElement<>(e.getName(), e.getDeclaredType(), e.getScope(), e.getValue());
            copy.setNil(e.isNil());
            // CClassInfo: com.evolveum.midpoint.xml.ns._public.common.common_3.EventHandlerType
            copy.setValue(((copy.getValue() == null) ? null : copy.getValue().clone()));
            return copy;
        }
        return null;
    }

    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @Override
    public EventHandlerChainType clone() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        final EventHandlerChainType clone = ((EventHandlerChainType) super.clone());
        // 'Handler' collection.
        if (this.handler != null) {
            clone.handler = null;
            copyHandler(this.getHandler(), clone.getHandler());
        }
        return clone;
    }

    @Override
    public String toString() {
        return "EventHandlerChainType{" +
                "handler=" + handler +
                '}';
    }
}
