/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.prism.xml.ns._public.types_3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "ReferentialIntegrityType")
@XmlEnum
public enum ReferentialIntegrityType {

    @XmlEnumValue("strict")
    STRICT("strict"),

    @XmlEnumValue("relaxed")
    RELAXED("relaxed"),

    @XmlEnumValue("lax")
    LAX("lax"),

    @XmlEnumValue("default")
    DEFAULT("default");

    private final String value;

    ReferentialIntegrityType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReferentialIntegrityType fromValue(String v) {
        for (ReferentialIntegrityType c: ReferentialIntegrityType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
