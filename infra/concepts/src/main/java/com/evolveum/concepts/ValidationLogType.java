/*
 * Copyright (c) 2026 Evolveum and contributors
 * 
 * This work is licensed under European Union Public License v1.2. See LICENSE file for details.
 * 
 */
package com.evolveum.concepts;

public enum ValidationLogType {
    WARNING("Warning"),
    ERROR("Error");

    public enum Specification {
        MISSING_DEFINITION,
        MISSING_NAMESPACE,
        UNKNOW
    }

    private final String label;

    ValidationLogType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
