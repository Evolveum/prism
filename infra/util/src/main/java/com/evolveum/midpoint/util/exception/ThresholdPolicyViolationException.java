/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util.exception;

import com.evolveum.midpoint.util.LocalizableMessage;

/**
 * @author katka
 *
 */
public class ThresholdPolicyViolationException extends PolicyViolationException {

    private static final long serialVersionUID = 1L;

    public ThresholdPolicyViolationException() {
    }

    public ThresholdPolicyViolationException(String message) {
        super(message);
    }

    public ThresholdPolicyViolationException(LocalizableMessage userFriendlyMessage) {
        super(userFriendlyMessage);
    }

    public ThresholdPolicyViolationException(LocalizableMessage userFriendlyMessage, String technicalMessage) {
        this(userFriendlyMessage);

        setTechnicalMessage(technicalMessage);
    }

    public ThresholdPolicyViolationException(Throwable cause) {
        super(cause);
    }

    public ThresholdPolicyViolationException(LocalizableMessage userFriendlyMessage, Throwable cause) {
        super(userFriendlyMessage, cause);
    }

    public ThresholdPolicyViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorTypeMessage() {
        return "Threshold policy violation";
    }
}
