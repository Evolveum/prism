/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.util.statistics;

import org.jetbrains.annotations.NotNull;

public interface OperationsPerformanceMonitor {

    OperationsPerformanceMonitor INSTANCE = OperationsPerformanceMonitorImpl.INSTANCE;

    void clearGlobalPerformanceInformation();

    @NotNull OperationsPerformanceInformation getGlobalPerformanceInformation();

    /**
     * Starts gathering thread-local performance information, clearing existing (if any).
     */
    void startThreadLocalPerformanceInformationCollection();

    /**
     * Stops gathering thread-local performance information, clearing existing (if any).
     */
    void stopThreadLocalPerformanceInformationCollection();

    OperationsPerformanceInformation getThreadLocalPerformanceInformation();
}
