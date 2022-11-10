/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.util.exception;

import org.jetbrains.annotations.NotNull;

public class SystemException extends RuntimeException {

    private static final long serialVersionUID = -611042093339023362L;

    public SystemException() {
    }

    public SystemException(String message) {
        super(message);
    }

    public SystemException(Throwable throwable) {
        super(throwable);
    }

    public SystemException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * A shortcut used to signal that an exception was really unexpected in the given context.
     * It means either a bug, or a problem outside midPoint we cannot do anything with.
     *
     * So it is expected _not_ to be caught and treated in any reasonable way.
     */
    public static SystemException unexpected(@NotNull Throwable t) {
        return new SystemException("Unexpected " + t.getClass().getSimpleName() + ": " + t.getMessage(), t);
    }

    /**
     * A variant of {@link #unexpected(Throwable)} that provides a little bit of context.
     */
    public static SystemException unexpected(@NotNull Throwable t, String context) {
        return new SystemException("Unexpected " + t.getClass().getSimpleName() + " " + context + ": " + t.getMessage(), t);
    }
}
