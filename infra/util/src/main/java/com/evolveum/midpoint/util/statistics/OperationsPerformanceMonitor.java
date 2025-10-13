/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
