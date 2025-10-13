/*
 * Copyright (c) 2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

/**
 * @author semancik
 *
 */
public enum ItemProcessing {

    IGNORE("ignore"), MINIMAL("minimal"), AUTO("auto"), FULL("full");

    private final String stringValue;

    ItemProcessing(final String value) {
        this.stringValue = value;
    }

    public String getValue() {
        return stringValue;
    }

    public static ItemProcessing findByValue(String stringValue) {
        if (stringValue == null) {
            return null;
        }
        for (ItemProcessing val : ItemProcessing.values()) {
            if (val.getValue().equals(stringValue)) {
                return val;
            }
        }
        return null;
    }

}
