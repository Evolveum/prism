/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util.exception;

import com.evolveum.midpoint.util.LocalizableMessage;

public class IndestructibilityViolationException extends PolicyViolationException {

    public IndestructibilityViolationException(String message) {
        super(message);
    }

    public IndestructibilityViolationException(LocalizableMessage userFriendlyMessage) {
        super(userFriendlyMessage);
    }

    public IndestructibilityViolationException(Throwable cause) {
        super(cause);
    }

    public IndestructibilityViolationException(LocalizableMessage userFriendlyMessage, Throwable cause) {
        super(userFriendlyMessage, cause);
    }

    public IndestructibilityViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorTypeMessage() {
        return "Indestructibility violation";
    }

}
