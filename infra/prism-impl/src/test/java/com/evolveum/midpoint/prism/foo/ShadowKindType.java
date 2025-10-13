/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.binding.TypeSafeEnum;

import jakarta.xml.bind.annotation.XmlEnumValue;

/**
 * Created by Dominik.
 */
public enum ShadowKindType  implements TypeSafeEnum  {
    /**
     *
     *                         Represents account on a target system.
     *
     *
     */
    @XmlEnumValue("account")
    ACCOUNT("account"),

    /**
     *
     */
    @XmlEnumValue("entitlement")
    ENTITLEMENT("entitlement"),

    /**
     *
     */
    @XmlEnumValue("generic")
    GENERIC("generic"),

    /**
     *
     */
    @XmlEnumValue("association")
    ASSOCIATION("association"),

    /**
     *
     */
    @XmlEnumValue("unknown")
    UNKNOWN("unknown");
    private final String value;

    ShadowKindType(String v) {
        value = v;
    }

    @Override
    public String value() {
        return value;
    }

    public static ShadowKindType fromValue(String v) {
        for (ShadowKindType c: ShadowKindType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
