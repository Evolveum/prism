/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import java.util.Arrays;

/**
 * Enumeration for "display" annotation.
 *
 * "display" annotation enumeration also replaces emphasized=true.
 */
public enum Display {

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

    Display(final String value) {
        this.value = value;
    }

    public static Display findByValue(String value) {
        if (value == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(val -> val.value.equals(value)).
                findFirst()
                .orElse(null);
    }
}
