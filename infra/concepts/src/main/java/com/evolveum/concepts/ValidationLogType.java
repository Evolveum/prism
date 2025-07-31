package com.evolveum.concepts;

public enum ValidationLogType {
    WARNING("Warning"),
    ERROR("Error");

    private final String label;

    ValidationLogType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
