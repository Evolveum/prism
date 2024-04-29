package com.evolveum.midpoint.prism.foo;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Created by Dominik.
 */
@XmlType(name = "ArchetypeTypeType")
@XmlEnum
public enum ArchetypeTypeType {
    @XmlEnumValue("structural")
    STRUCTURAL("structural"),

    @XmlEnumValue("auxiliary")
    AUXILIARY("auxiliary");
    private final String value;

    ArchetypeTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ArchetypeTypeType fromValue(String v) {
        for (ArchetypeTypeType c: ArchetypeTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
