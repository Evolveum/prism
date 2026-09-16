/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.util.exception;

import com.evolveum.midpoint.util.LocalizableMessage;

/**
 * Exception thrown when an operation exceeds a system or program limitation.
 *
 * <p>This exception may be used to represent limitations reported by an
 * underlying system, such as a database, while exposing an application-level
 * error to the caller.</p>
 *
 * <p>The exception supports both plain and localized user-friendly messages,
 * as well as an optional underlying cause for diagnostic purposes.</p>
 */
public class ProgramLimitException extends RuntimeException {

    private final LocalizableMessage userFriendlyMessage;

    public ProgramLimitException() {
        super();
        userFriendlyMessage = null;
    }

    public ProgramLimitException(String message) {
        super(message);
        userFriendlyMessage = null;
    }

    public ProgramLimitException(Throwable cause) {
        super(cause);
        userFriendlyMessage = null;
    }

    public ProgramLimitException(String message, Throwable cause) {
        super(message, cause);
        userFriendlyMessage = null;
    }

    public ProgramLimitException(LocalizableMessage userFriendlyMessage) {
        super();
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public ProgramLimitException(LocalizableMessage userFriendlyMessage, Throwable cause) {
        super(cause);
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public LocalizableMessage getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    public String getErrorTypeMessage() {
        return "The operation exceeds the system limit.";
    }
}
