/*
 * Copyright (c) 2015 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

/**
 * Interface for object validation (mostly to be used in tests).
 *
 * @author Radovan Semancik
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Validate the provided object. Throws appropriate exception if
     * the object is not valid.
     *
     * @param object object to validate
     * @param name short string name of the object. Designed to be used in exception messages.
     * @throws Exception appropriate exception if the object is not valid.
     */
    void validate(T object, String name) throws Exception;

}
