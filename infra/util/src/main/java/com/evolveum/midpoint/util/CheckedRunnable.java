/*
 * Copyright (C) 2016-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

/**
 * Runnable version with thrown exception.
 * This simplifies wrapping changing code (provided as lambda) with the same boilerplate code.
 */
@FunctionalInterface
public interface CheckedRunnable {

    void run() throws Exception;
}
