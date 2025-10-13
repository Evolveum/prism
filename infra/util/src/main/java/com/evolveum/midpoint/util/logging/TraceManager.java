/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evolveum.midpoint.util.logging.impl.TraceImpl;

/**
 * Factory for trace instances.
 */
public class TraceManager {

    private static final String PERFORMANCE_ADVISOR = "PERFORMANCE_ADVISOR";

    public static Trace getTrace(Class<?> clazz) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(clazz);
        return new TraceImpl(logger);
    }

    public static Trace getTrace(String loggerName) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(loggerName);
        return new TraceImpl(logger);
    }

    public static Trace getPerformanceAdvisorTrace() {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PERFORMANCE_ADVISOR);
        return new TraceImpl(logger);
    }

    public static ILoggerFactory getILoggerFactory() {
        return LoggerFactory.getILoggerFactory();
    }
}
