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
// Generated on: 2012.05.20 at 05:41:15 PM CEST
//


package com.evolveum.prism.xml.ns._public.types_3;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ModificationTypeType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ModificationTypeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="add"/&gt;
 *     &lt;enumeration value="replace"/&gt;
 *     &lt;enumeration value="delete"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlType(name = "ModificationTypeType")
@XmlEnum
public enum ModificationTypeType implements Serializable {

    @XmlEnumValue("add")
    ADD("add"),
    @XmlEnumValue("replace")
    REPLACE("replace"),
    @XmlEnumValue("delete")
    DELETE("delete");
    private final String value;

    ModificationTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ModificationTypeType fromValue(String v) {
        for (ModificationTypeType c: ModificationTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
