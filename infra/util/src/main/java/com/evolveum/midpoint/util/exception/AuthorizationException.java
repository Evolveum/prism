/*
 * Copyright (c) 2015-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util.exception;

import com.evolveum.midpoint.util.LocalizableMessage;

/**
 * Exception indicating violation of authorization policies.
 *
 * @author Radovan Semancik
 *
 */
public class AuthorizationException extends SecurityViolationException {
    private static final long serialVersionUID = 1L;

    public AuthorizationException() {
    }

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(LocalizableMessage userFriendlyMessage) {
        super(userFriendlyMessage);
    }

    public AuthorizationException(Throwable cause) {
        super(cause);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorizationException(LocalizableMessage userFriendlyMessage, Throwable cause) {
        super(userFriendlyMessage, cause);
    }

    @Override
    public String getErrorTypeMessage() {
        return "Not authorized";
    }

}
