/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.exception;

/**
 * Asserts (mainly to the compiler) that the flow of control really should not go here.
 *
 * To be used e.g. after "fail" methods, to avoid the necessity of returning fake values.
 */
public class NotHereAssertionError extends AssertionError {
    public NotHereAssertionError() {
        super("We should not be here");
    }
}
