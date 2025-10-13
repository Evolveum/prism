/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.exception;

/**
 * Specific kind of SchemaException. Used e.g. to treat "no name" problems in previewChanges method nicely.
 * SchemaException.propertyName:=UserType.F_NAME could be used as well, but it's a bit ambiguous.
 *
 * A little bit experimental. (We certainly don't want to have millions of exception types.)
 */
public class NoFocusNameSchemaException extends SchemaException {
    private static final long serialVersionUID = 1L;

    public NoFocusNameSchemaException(String message) {
        super(message);
    }
}
