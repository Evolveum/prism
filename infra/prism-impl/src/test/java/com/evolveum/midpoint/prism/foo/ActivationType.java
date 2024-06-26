/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2012.02.24 at 02:46:19 PM CET
//

package com.evolveum.midpoint.prism.foo;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.path.ItemName;

/**
 * ActivationTyps is a container, therefore the following elements are properties.
 * This tests the ability to deal with boolean and dateTime properties.
 *
 *
 * <p>Java class for ActivationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ActivationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="validFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="validTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivationType", propOrder = {
        "enabled",
        "validFrom",
        "validTo"
})
public class ActivationType
        implements Serializable, Containerable {

    private static final long serialVersionUID = 201202081233L;

    public static final ItemName F_ENABLED = ItemName.from(ObjectType.NS_FOO, "enabled");
    public static final ItemName F_VALID_FROM = ItemName.from(ObjectType.NS_FOO, "validFrom");
    public static final ItemName F_VALID_TO = ItemName.from(ObjectType.NS_FOO, "validTo");

    protected boolean enabled;

    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar validFrom;

    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar validTo;

    /**
     * Gets the value of the enabled property.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     */
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the validFrom property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getValidFrom() {
        return validFrom;
    }

    /**
     * Sets the value of the validFrom property.
     *
     * @param value allowed object is
     * {@link XMLGregorianCalendar }
     */
    public void setValidFrom(XMLGregorianCalendar value) {
        this.validFrom = value;
    }

    /**
     * Gets the value of the validTo property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getValidTo() {
        return validTo;
    }

    /**
     * Sets the value of the validTo property.
     *
     * @param value allowed object is
     * {@link XMLGregorianCalendar }
     */
    public void setValidTo(XMLGregorianCalendar value) {
        this.validTo = value;
    }

    /* (non-Javadoc)
     * @see com.evolveum.midpoint.prism.Containerable#asPrismContainerValue()
     */
    @Override
    public PrismContainerValue asPrismContainerValue() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.evolveum.midpoint.prism.Containerable#setupContainerValue(com.evolveum.midpoint.prism.PrismContainerValue)
     */
    @Override
    public void setupContainerValue(PrismContainerValue container) {
        throw new UnsupportedOperationException();
    }

}
