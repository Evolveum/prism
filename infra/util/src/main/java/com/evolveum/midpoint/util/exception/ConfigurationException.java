/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util.exception;

import com.evolveum.midpoint.util.LocalizableMessage;

/**
 * Configuration exception indicates that something is mis-configured.
 *
 * The system or its part is misconfigured and therefore the intended operation
 * cannot be executed.
 *
 * @author Radovan Semancik
 *
 */
public class ConfigurationException extends CommonException {
    private static final long serialVersionUID = 1L;

    public ConfigurationException() {
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(LocalizableMessage userFriendlyMessage) {
        super(userFriendlyMessage);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(LocalizableMessage userFriendlyMessage, Throwable cause) {
        super(userFriendlyMessage, cause);
    }

    @Override
    public String getErrorTypeMessage() {
        return "Configuration error";
    }

}
