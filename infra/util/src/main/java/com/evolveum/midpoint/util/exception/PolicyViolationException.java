/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util.exception;

import java.io.Serial;

import com.evolveum.midpoint.util.LocalizableMessage;

/**
 * Policy violation.
 *
 * The operation violated a policy.
 * The policy is usually a high-level, business-oriented policy, configured or customized by system administrator.
 * This exception is not primarily intended to be thrown when violating some kind of hardcoded or inherent policy,
 * e.g. accessing inter-cluster operation by a node outside of cluster (although some such cases may be justified).
 * Also, this exception should not be thrown for situation when system is misconfigured.
 * This exception is meant to indicate violation of a high-level policy defined by the user or administrator.
 *
 * If an operation throws this exception, the invoking code should handle the exception at appropriate place(s).
 * The exception is likely to be handled at several places, e.g. the "backend" code handling the exception by marking
 * affected object with "policy violation" mark, re-throwing the exception, and the "frontend" (GUI) code handling the
 * exception by reporting it to the user.
 *
 * @author Radovan Semancik
 */
public class PolicyViolationException extends CommonException {
    @Serial private static final long serialVersionUID = 1L;

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
