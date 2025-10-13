/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.exception;

import com.evolveum.midpoint.util.annotation.Experimental;

import org.jetbrains.annotations.NotNull;

public interface SeverityAwareException {

    /**
     * Not all exceptions are fatal. This method returns the (estimated) severity of this exception.
     * The final decision is up to the exception handling code, of course. It may or may not accept this value.
     */
    @Experimental
    default @NotNull SeverityAwareException.Severity getSeverity() {
        return Severity.FATAL_ERROR;
    }

    enum Severity {
        FATAL_ERROR, PARTIAL_ERROR, WARNING, HANDLED_ERROR, SUCCESS, NOT_APPLICABLE
    }
}
