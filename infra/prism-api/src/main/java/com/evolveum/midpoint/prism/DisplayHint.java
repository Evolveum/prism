/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import java.util.Arrays;

/**
 * Enumeration for "display" annotation.
 *
 * "display" annotation enumeration also replaces emphasized=true.
 */
public enum DisplayHint {

    /**
     * Item shouldn't be visible.
     */
    HIDDEN("hidden"),

    /**
     * Item should be visible, standard visibility behaviour for item.
     */
    REGULAR("regular"),

    /**
     * Item should be visible and emphasized. E.g. it should be displayed in bold.
     */
    EMPHASIZED("emphasized"),

    /**
     * Item should be visible and collapsed. E.g. it should be displayed in collapsed form. Applicable only for containers.
     */
    COLLAPSED("collapsed"),

    /**
     * Item should be visible and expanded. E.g. it should be displayed in expanded form. Applicable only for containers.
     */
    EXPANDED("expanded");

    final String value;

    DisplayHint(final String value) {
        this.value = value;
    }

    public static DisplayHint findByValue(String value) {
        if (value == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(val -> val.value.equals(value)).
                findFirst()
                .orElse(null);
    }

    public String getValue() {
        return value;
    }
}
