/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util.exception;

import com.evolveum.midpoint.util.LocalizableMessage;

/**
 * @author semancik
 *
 */
public class PolicyViolationException extends CommonException {
    private static final long serialVersionUID = 1L;

    public PolicyViolationException() {
    }

    public PolicyViolationException(String message) {
        super(message);
    }

    public PolicyViolationException(LocalizableMessage userFriendlyMessage) {
        super(userFriendlyMessage);
    }

    public PolicyViolationException(Throwable cause) {
        super(cause);
    }

    public PolicyViolationException(LocalizableMessage userFriendlyMessage, Throwable cause) {
        super(userFriendlyMessage, cause);
    }

    public PolicyViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolicyViolationException(LocalizableMessage userFriendlyMessage, String technicalMessage) {
        this(userFriendlyMessage);
        setTechnicalMessage(technicalMessage);
    }

    @Override
    public String getErrorTypeMessage() {
        return "Policy violation";
    }

}
