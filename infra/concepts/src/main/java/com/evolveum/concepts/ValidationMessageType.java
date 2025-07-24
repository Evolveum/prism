package com.evolveum.concepts;

public enum ValidationMessageType {
    WARNING("Warning"),
    ERROR("Error");

    private final String label;

    ValidationMessageType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
