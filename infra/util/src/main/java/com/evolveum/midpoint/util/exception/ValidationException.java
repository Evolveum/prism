package com.evolveum.midpoint.util.exception;

import com.evolveum.concepts.ValidationLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception for haling validation logs
 */
public class ValidationException extends RuntimeException {

    List<ValidationLog> validationLogs = new ArrayList<>();

    public ValidationException(List<ValidationLog> validationLogs) {
        this.validationLogs = validationLogs;
    }

    public List<ValidationLog> getValidationLogs() {
        return validationLogs;
    }
}
