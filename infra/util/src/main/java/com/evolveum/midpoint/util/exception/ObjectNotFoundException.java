/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util.exception;

import com.evolveum.midpoint.util.LocalizableMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    /** Estimated severity. */
    private final Severity severity;

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
        this.severity = severityForExpected(expected);
    }

    /** Consider using {@link #wrap(String)} if the cause is {@link ObjectNotFoundException} itself. */
    public ObjectNotFoundException(String message, Throwable cause, Class<?> type, String oid, Severity severity) {
        super(message, cause);
        this.type = type;
        this.oid = oid;
        this.severity = Objects.requireNonNullElse(severity, Severity.FATAL_ERROR);
    }

    private static Severity severityForExpected(boolean expected) {
        return expected ? Severity.HANDLED_ERROR : Severity.FATAL_ERROR;
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
        this.severity = severityForExpected(expected);
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
        this.severity = Severity.FATAL_ERROR;
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
    public @NotNull Severity getSeverity() {
        return severity;
    }

    /** Provides additional context information to the exception by creating a wrapping one. */
    public ObjectNotFoundException wrap(String context) {
        return new ObjectNotFoundException(context + ": " + getMessage(), this, type, oid, severity);
    }
}
