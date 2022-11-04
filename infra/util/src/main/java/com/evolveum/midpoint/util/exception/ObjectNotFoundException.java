/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.util.exception;

import com.evolveum.midpoint.util.LocalizableMessage;

import org.jetbrains.annotations.NotNull;

/**
 * Object with specified {@link #type} and identifier ({@link #oid}) has not been found in the repository or in other
 * relevant context.
 *
 * @author Radovan Semancik
 */
public class ObjectNotFoundException extends CommonException {
    private static final long serialVersionUID = -9003686713018111855L;

    private final Class<?> type;

    /** Usually a repository OID. But for types other than Prism objects this may be arbitrary identifier. Can be null. */
    private final String oid;

    /** Was this exception somewhat expected? This drives the estimated severity. */
    private final boolean expected;

    public ObjectNotFoundException() {
        this((String) null, null, null, null, false);
    }

    // If possible, use the creator that has type/oid parameters.
    public ObjectNotFoundException(String message) {
        this(message, null, null, null, false);
    }

    /** Consider using {@link #wrap(String)} if the cause is {@link ObjectNotFoundException} itself. */
    public ObjectNotFoundException(String message, Throwable cause) {
        this(message, cause, null, null, false);
    }

    public ObjectNotFoundException(String message, Class<?> type, String oid, boolean expected) {
        this(message, null, type, oid, expected);
    }

    public ObjectNotFoundException(String message, Class<?> type, String oid) {
        this(message, null, type, oid, false);
    }

    /** Consider using {@link #wrap(String)} if the cause is {@link ObjectNotFoundException} itself. */
    public ObjectNotFoundException(String message, Throwable cause, Class<?> type, String oid) {
        this(message, cause, type, oid, false);
    }

    /** Consider using {@link #wrap(String)} if the cause is {@link ObjectNotFoundException} itself. */
    public ObjectNotFoundException(String message, Throwable cause, Class<?> type, String oid, boolean expected) {
        super(message, cause);
        this.type = type;
        this.oid = oid;
        this.expected = expected;
    }

    public ObjectNotFoundException(Class<?> type, String oid) {
        this(type, oid, false);
    }

    public ObjectNotFoundException(Class<?> type, String oid, boolean expected) {
        this("Object of type '" + type.getSimpleName() + "' with OID '" + oid + "' was not found.", null, type, oid, expected);
    }

    // Constructors potentially used outside midPoint

    public ObjectNotFoundException(LocalizableMessage message, Throwable cause, Class<?> type, String oid, boolean expected) {
        super(message, cause);
        this.type = type;
        this.oid = oid;
        this.expected = expected;
    }

    @SuppressWarnings("unused")
    public ObjectNotFoundException(LocalizableMessage message) {
        this(message, null, null, null, false);
    }

    @SuppressWarnings("unused")
    public ObjectNotFoundException(LocalizableMessage message, Throwable cause) {
        this(message, cause, null, null, false);
    }

    @Deprecated // the client should specify also the type
    public ObjectNotFoundException(String message, Throwable cause, String oid) {
        this(message, cause, null, oid, false);
    }

    @Deprecated // the client should specify also the type
    public ObjectNotFoundException(String message, String oid) {
        this(message, null, null, oid, false);
    }

    @Deprecated // the client should specify more details
    public ObjectNotFoundException(Throwable cause) {
        super(cause);
        this.type = null;
        this.oid = null;
        this.expected = false;
    }

    public String getOid() {
        return oid;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public String getErrorTypeMessage() {
        return "Object not found";
    }

    @Override
    public @NotNull CommonException.Severity getSeverity() {
        return expected ? Severity.HANDLED_ERROR : Severity.FATAL_ERROR;
    }

    /** Provides additional context information to the exception by creating a wrapping one. */
    public ObjectNotFoundException wrap(String context) {
        return new ObjectNotFoundException(context + ": " + getMessage(), this, type, oid, expected);
    }
}
