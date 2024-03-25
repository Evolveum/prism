package com.evolveum.midpoint.prism.impl.storage;

/**
 * Exact value is already present in storage.
 *
 *
 * This exception does not contain stacktrace and reuses same instance for fast execution.
 */
public class ValueDoesNotExistsException extends Exception {

    public static final ValueDoesNotExistsException INSTANCE = new ValueDoesNotExistsException();


    private ValueDoesNotExistsException() {
        // Intentional NOOP
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
