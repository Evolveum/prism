/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.util.exception;

import java.io.Serializable;
import java.util.function.Supplier;

import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.logging.LoggingUtils;

/**
 * Additional information for an exception. It is NOT to be displayed to the user along with the exception, but
 * it should be recorded in the logs. It is intended to provide additional context for the exception when diagnosing it.
 *
 * The goal is to simplify the error handling code in cases where we need to log additional information. Instead of issuing
 * extra logging calls (including e.g. redundant construction of the error message), one simply adds the additional information
 * to the log message, and the methods {@link LoggingUtils} will do the rest.
 */
@Experimental
public interface ExceptionContext extends Supplier<String>, Serializable {

    static ExceptionContext of(String message) {
        return () -> message;
    }
}
