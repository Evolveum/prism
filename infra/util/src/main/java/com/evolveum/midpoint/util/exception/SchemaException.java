/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.util.exception;

import javax.xml.namespace.QName;

import com.evolveum.concepts.SourceLocation;
import com.evolveum.midpoint.util.LocalizableMessage;
import com.evolveum.midpoint.util.annotation.Experimental;

/**
 * Error regarding schema.
 *
 * E.g. Object class violation, missing object class, inconsistent schema, etc.
 *
 * @author Radovan Semancik
 *
 */
public class SchemaException extends CommonException {
    private static final long serialVersionUID = -6016220825724355014L;

    private QName propertyName;
    private SourceLocation sourceLocation;

    public SchemaException() {
        super();
    }

    public SchemaException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaException(LocalizableMessage userFriendlyMessage, Throwable cause) {
        super(userFriendlyMessage, cause);
    }

    public SchemaException(String message, Throwable cause, QName propertyName) {
        super(message, cause);
        this.propertyName = propertyName;
    }

    public SchemaException(String message) {
        super(message);
    }

    public SchemaException(String message, ExceptionContext context) {
        super(message, context);
    }

    public SchemaException(LocalizableMessage userFriendlyMessage) {
        super(userFriendlyMessage);
    }

    public SchemaException(String message, QName propertyName) {
        super(message);
        this.propertyName = propertyName;
    }

    public SchemaException(String message, SourceLocation sourceLocation) {
        super(message);
        this.sourceLocation = sourceLocation;
    }

    @Experimental // is this a good idea?
    public static SchemaException of(String messageTemplate, Object... arguments) {
        return new SchemaException(messageTemplate.formatted(arguments));
    }

    @Override
    public String getErrorTypeMessage() {
        return "Schema problem";
    }

    public QName getPropertyName() {
        return propertyName;
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    /** Provides additional context information to the exception by creating a wrapping one. */
    @Experimental
    public SchemaException wrap(String context) {
        // Later we may take userFriendlyMessage into account.
        return new SchemaException(context + ": " + getMessage(), this);
    }
}
