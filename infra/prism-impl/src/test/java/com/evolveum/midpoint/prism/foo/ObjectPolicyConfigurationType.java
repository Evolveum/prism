/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.foo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObjectPolicyConfigurationType", propOrder = {
        "type",
        "subtype"
})
public class ObjectPolicyConfigurationType extends ArchetypePolicyType implements Serializable {

    protected QName type;
    protected String subtype;

    /**
     * Gets the value of the type property.
     *
     * @return
     *     possible object is
     *     {@link QName }
     *
     */
    public QName getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value
     *     allowed object is
     *     {@link QName }
     *
     */
    public void setType(QName value) {
        this.type = value;
    }

    /**
     * Gets the value of the subtype property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * Sets the value of the subtype property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSubtype(String value) {
        this.subtype = value;
    }
}
