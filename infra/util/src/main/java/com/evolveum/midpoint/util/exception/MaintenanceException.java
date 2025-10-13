/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util.exception;

import com.evolveum.midpoint.util.LocalizableMessage;

/**
 *
 * May happen in case that resource is administratively set to maintenance mode.
 *
 *
 * @author Martin Lizner
 *
 */
public class MaintenanceException extends CommunicationException {
    private static final long serialVersionUID = 1L;

    public MaintenanceException() {
    }

    public MaintenanceException(String message) {
        super(message);
    }

    public MaintenanceException(LocalizableMessage userFriendlyMessage) {
        super(userFriendlyMessage);
    }

    public MaintenanceException(Throwable cause) {
        super(cause);
    }

    public MaintenanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MaintenanceException(LocalizableMessage userFriendlyMessage, Throwable cause) {
        super(userFriendlyMessage, cause);
    }

    @Override
    public String getErrorTypeMessage() {
        return "Resource is in the maintenance";
    }

}
