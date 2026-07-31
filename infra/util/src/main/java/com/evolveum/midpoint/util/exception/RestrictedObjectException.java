/*
 * Copyright (c) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util.exception;

import java.io.Serial;

import com.evolveum.midpoint.util.LocalizableMessage;

/**
 * Subscription compliance exception.
 * <p>
 * Exception thrown when a subscription-restricted object or operation is
 * accessed in the production environment without an active subscription.
 * <p>
 * This exception is used to enforce subscription compliance for production
 * deployments.
 */
public class RestrictedObjectException extends CommonException {
    @Serial private static final long serialVersionUID = 1L;

    public RestrictedObjectException() {
    }

    public RestrictedObjectException(String message) {
        super(message);
    }

    public RestrictedObjectException(LocalizableMessage userFriendlyMessage) {
        super(userFriendlyMessage);
    }

    public RestrictedObjectException(Throwable cause) {
        super(cause);
    }

    public RestrictedObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestrictedObjectException(LocalizableMessage userFriendlyMessage, Throwable cause) {
        super(userFriendlyMessage, cause);
    }

    @Override
    public String getErrorTypeMessage() {
        return "Unsupported operation error";
    }

}
