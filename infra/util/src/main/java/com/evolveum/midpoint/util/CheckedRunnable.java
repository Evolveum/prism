/*
 * Copyright (C) 2016-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
