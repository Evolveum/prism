/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util.exception;

import com.evolveum.midpoint.util.LocalizableMessage;

/**
 * Exception indicating violation of security policies.
 * It is SecurityViolationException to avoid confusion with java.lang.SecurityException
 *
 * @author Radovan Semancik
 *
 */
public class SecurityViolationException extends CommonException {
    private static final long serialVersionUID = 1L;

    public SecurityViolationException() {
    }

    public SecurityViolationException(String message) {
        super(message);
    }

    public SecurityViolationException(LocalizableMessage userFriendlyMessage) {
        super(userFriendlyMessage);
    }

    public SecurityViolationException(Throwable cause) {
        super(cause);
    }

    public SecurityViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityViolationException(LocalizableMessage userFriendlyMessage, Throwable cause) {
        super(userFriendlyMessage, cause);
    }

    @Override
    public String getErrorTypeMessage() {
        return "Security violation";
    }

}
