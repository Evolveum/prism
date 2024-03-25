package com.evolveum.midpoint.prism.impl.storage;

/**
 * Exact value is already present in storage.
 *
 *
 * This exception does not contain stacktrace and reuses same instance for fast execution.
 */
public class ExactValueExistsException extends Exception {

    public static final ExactValueExistsException INSTANCE = new ExactValueExistsException();


    private ExactValueExistsException() {
        // Intentional NOOP
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
