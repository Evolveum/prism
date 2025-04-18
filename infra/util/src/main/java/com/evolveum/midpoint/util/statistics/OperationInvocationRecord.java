/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.util.statistics;

import java.io.Serial;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ch.qos.logback.classic.Level;

import com.evolveum.midpoint.util.CanBeNone;
import com.evolveum.midpoint.util.NoValueUtil;
import com.evolveum.midpoint.util.aspect.MidpointInterceptor;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.MDC;

import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.aspect.ProfilingDataManager;

import static com.evolveum.midpoint.util.NoValueUtil.NONE_LONG;

/**
 * Represents an invocation of a single *operation* in the system. (Operation is typically implemented by a method or a set
 * of methods. But it may not be necessarily aligned to method's boundary.)
 *
 * NOTE: History: This class provides basically the functionality of {@link MidpointInterceptor}. However, it was later
 * refactored to be callable also outside of the context of AOP - manually by injecting appropriate code, mimicking
 * {@link MidpointInterceptor#invoke(MethodInvocation)} method. Currently, its main use is to manage the performance
 * data of the operations executed in the system in cooperation with the `OperationResult` class.
 *
 * "Operation" may represent "operation invocation" in short (where no confusion is possible).
 */
public final class OperationInvocationRecord implements Serializable {

    @Serial private static final long serialVersionUID = 6805648677427302932L;

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    /** When the operation started (see {@link System#nanoTime()}). */
    private final long startTime = System.nanoTime();

    /** CPU time at the start of the operation. Usually not measured; the value of {@link NoValueUtil#NONE_LONG} is used then. */
    @CanBeNone private final long startCpuTime;

    /** Do we measure the CPU time? */
    private final boolean measureCpuTime;

    /** Elapsed time during the operation; in nanoseconds. */
    private long elapsedTime;

    /** Operation own time: without called (child) operations. In microseconds. {@link NoValueUtil#NONE_LONG} if not known. */
    @CanBeNone private long ownTimeMicros = NONE_LONG;

    /** CPU time of the operation (including child operations). {@link NoValueUtil#NONE_LONG} if not measured. */
    @CanBeNone private long cpuTime = NONE_LONG;

    /** Unique ID of the operation invocation. */
    private int invocationId;

    /** Return value of the operation. Present only if {@link #traceEnabled} is `true` to avoid wasting CPU cycles. */
    private String formattedReturnValue;

    /** True if the operation invocation ended with an exception. */
    private boolean gotException;

    /** Name of the exception occurring during operation invocation. */
    private String exceptionName;

    /**
     * A {@link ProfilingDataManager.Subsystem} to which the operation belongs. Derived from the operation name.
     * Slightly obsolete. We now have a different classification of subsystems in midPoint, see e.g. `BasicComponentStructure`
     * class.
     */
    private final ProfilingDataManager.Subsystem subsystem;

    /** Used to manage the subsystem mark in logging context ({@link MDC}). */
    private String previousSubsystem;

    /** Qualified class name corresponding to the operation. (In fact, it may not be a Java class name, but it usually is.) */
    private final String fullClassName;

    /** Abbreviated class name, for clarity purposes. */
    private final String shortenedClassName;

    /** The "method" part of the operation name. May or may not correspond to actual Java method. */
    private final String methodName;

    /** How deep are we in the call stack? Used for old-style logger-based profiling. */
    private int callDepth;

    /** Is the first (coarse) level of logger-based profiling enabled? */
    private final boolean debugEnabled;

    /** Is the second (fine) level of logger-based profiling enabled? */
    private final boolean traceEnabled;

    private OperationInvocationRecord(String fullClassName, String methodName, boolean measureCpuTime) {
        this.measureCpuTime = measureCpuTime;
        this.startCpuTime = measureCpuTime ? getCurrentCpuTime() : NONE_LONG;

        this.fullClassName = fullClassName;
        shortenedClassName = getClassName(fullClassName);
        subsystem = getSubsystem(fullClassName);
        this.methodName = methodName;
        Level localLevelOverride = OperationExecutionLogger.THREAD_LOCAL_LEVEL_OVERRIDE.get();
        if (OperationExecutionLogger.globalLevelOverride == null && localLevelOverride == null) {
            debugEnabled = OperationExecutionLogger.LOGGER_PROFILING.isDebugEnabled();
            traceEnabled = OperationExecutionLogger.LOGGER_PROFILING.isTraceEnabled();
        } else {
            debugEnabled = isDebug(OperationExecutionLogger.globalLevelOverride) || isDebug(localLevelOverride);
            traceEnabled = isTrace(OperationExecutionLogger.globalLevelOverride) || isTrace(localLevelOverride);
        }
    }

    public static OperationInvocationRecord create(MethodInvocation invocation) {
        OperationInvocationRecord ctx = new OperationInvocationRecord(
                getFullClassName(invocation), invocation.getMethod().getName() + "#", true);
        ctx.beforeCall(invocation.getArguments());
        return ctx;
    }

    public static OperationInvocationRecord create(String operationName, Object[] arguments, boolean measureCpuTime) {
        int i = operationName.lastIndexOf('.');
        String className, methodName;
        if (i < 0) {
            className = operationName;
            methodName = "unknownMethod";
        } else {
            className = operationName.substring(0, i);
            methodName = operationName.substring(i + 1);
        }
        OperationInvocationRecord ctx = new OperationInvocationRecord(className, methodName, measureCpuTime);
        ctx.beforeCall(arguments);
        return ctx;
    }

    private static String formatVal(Object value) {
        if (value == null) {
            return ("null");
        }
        try {
            return PrettyPrinter.prettyPrint(value);
        } catch (Throwable t) {
            OperationExecutionLogger.LOGGER_PROFILING.error("Internal error formatting a value: {}", value, t);
            return "###INTERNAL#ERROR### " + t.getClass().getName() + ": " + t.getMessage() + " value=" + value;
        }
    }

    // This is made public to use in testing
    public static String swapSubsystemMark(String subsystemName) {
        String prev = MDC.get(OperationExecutionLogger.MDC_SUBSYSTEM_KEY);
        if (subsystemName == null) {
            MDC.remove(OperationExecutionLogger.MDC_SUBSYSTEM_KEY);
        } else {
            MDC.put(OperationExecutionLogger.MDC_SUBSYSTEM_KEY, subsystemName);
        }
        return prev;
    }

    public static void formatExecutionTime(StringBuilder sb, long elapsed) {
        sb.append(elapsed / 1000000);
        sb.append('.');
        long micros = (elapsed / 1000) % 1000;
        if (micros < 100) {
            sb.append('0');
        }
        if (micros < 10) {
            sb.append('0');
        }
        sb.append(micros);
    }

    private boolean isDebug(Level level) {
        return level != null && Level.DEBUG.isGreaterOrEqual(level);
    }

    private boolean isTrace(Level level) {
        return level != null && Level.TRACE.isGreaterOrEqual(level);
    }

    private void beforeCall(Object[] arguments) {
        previousSubsystem = swapSubsystemMark(subsystem != null ? subsystem.name() : null);

        invocationId = ID_COUNTER.incrementAndGet();

        if (debugEnabled) {
            StringBuilder infoLog = new StringBuilder("#### Entry: ");
            infoLog.append(invocationId);
            infoLog.append(" ");
            if (traceEnabled) {
                String depthStringValue = MDC.get(OperationExecutionLogger.MDC_DEPTH_KEY);
                if (depthStringValue == null || depthStringValue.isEmpty()) {
                    callDepth = 1;
                } else {
                    callDepth = Integer.parseInt(depthStringValue) + 1;
                }
                MDC.put(OperationExecutionLogger.MDC_DEPTH_KEY, Integer.toString(callDepth));
                infoLog.append(OperationExecutionLogger.INDENT_STRING.repeat(Math.max(0, callDepth)));
            }

            infoLog.append(shortenedClassName);
            OperationExecutionLogger.LOGGER_PROFILING.debug("{}->{}", infoLog, methodName);

            if (traceEnabled) {
                StringBuilder sb = new StringBuilder();
                sb.append("###### args: ");
                sb.append("(");
                if (arguments != null) {
                    for (int i = 0; i < arguments.length; i++) {
                        sb.append(formatVal(arguments[i]));
                        if (arguments.length != i + 1) {
                            sb.append(", ");
                        }
                    }
                }
                sb.append(")");
                OperationExecutionLogger.LOGGER_PROFILING.trace(sb.toString());
            }
        }
    }

    public Object processReturnValue(Object returnValue) {
        if (traceEnabled) {
            formattedReturnValue = formatVal(returnValue);
        }
        return returnValue;
    }

    public void processReturnValue(Map<String, Collection<String>> returns, Throwable cause) {
        if (traceEnabled) {
            formattedReturnValue = returns.toString();
            if (cause != null) {
                formattedReturnValue += "; " + cause.getClass().getName() + ": " + cause.getMessage();
            }
        }
    }

    public <T extends Throwable> T processException(T e) {
        exceptionName = e.getClass().getName();
        gotException = true;
        return e;
    }

    public void afterCall(@CanBeNone long notOwnTimeMicros) {
        afterCall(null, notOwnTimeMicros);
    }

    public void afterCall(MethodInvocation invocation, @CanBeNone long notOwnTimeMicros) {
        elapsedTime = System.nanoTime() - startTime;
        if (notOwnTimeMicros != NONE_LONG) {
            ownTimeMicros = getElapsedTimeMicros() - notOwnTimeMicros;
        }
        if (measureCpuTime && startCpuTime != NONE_LONG) {
            long currentCpuTime = getCurrentCpuTime();
            if (currentCpuTime != NONE_LONG) {
                cpuTime = currentCpuTime - startCpuTime;
            }
        }

        OperationsPerformanceMonitorImpl.INSTANCE.registerInvocationCompletion(this);

        if (traceEnabled) {
            MDC.put(OperationExecutionLogger.MDC_DEPTH_KEY, Integer.toString(--callDepth));
        }

        // Restore previously marked subsystem executed before return
        if (debugEnabled) {
            StringBuilder sb = new StringBuilder();
            sb.append("##### Exit: ");
            sb.append(invocationId);
            sb.append(" ");
            if (traceEnabled) {
                sb.append(OperationExecutionLogger.INDENT_STRING.repeat(Math.max(0, callDepth + 1)));
            }
            sb.append(shortenedClassName);
            sb.append("->");
            sb.append(methodName);

            sb.append(" etime: ");
            formatExecutionTime(sb, elapsedTime);
            sb.append(" ms");

            if (cpuTime != NONE_LONG) {
                sb.append(", cputime: ");
                formatExecutionTime(sb, cpuTime);
                sb.append(" ms");
            }

            OperationExecutionLogger.LOGGER_PROFILING.debug(sb.toString());
            if (traceEnabled) {
                if (gotException) {
                    OperationExecutionLogger.LOGGER_PROFILING.trace("###### return exception: {}", exceptionName);
                } else {
                    OperationExecutionLogger.LOGGER_PROFILING.trace("###### retval: {}", formattedReturnValue);
                }
            }
        }

        if (invocation != null && OperationExecutionLogger.isProfilingActive) {
            long processingStartTime = System.nanoTime();
            ProfilingDataManager.getInstance().applyGranularityFilterOnEnd(
                    shortenedClassName, invocation.getMethod().getName(), invocation.getArguments(),
                    subsystem, startTime, processingStartTime);
        }

        swapSubsystemMark(previousSubsystem);
    }

    private ProfilingDataManager.Subsystem getSubsystem(String className) {
        if (className == null) {
            return null;
        }
        if (className.startsWith("com.evolveum.midpoint.repo")) {
            return ProfilingDataManager.Subsystem.REPOSITORY;
        } else if (className.startsWith("com.evolveum.midpoint.model.impl.sync")) {
            return ProfilingDataManager.Subsystem.SYNCHRONIZATION_SERVICE;
        } else if (className.startsWith("com.evolveum.midpoint.model")) {
            return ProfilingDataManager.Subsystem.MODEL;
        } else if (className.startsWith("com.evolveum.midpoint.provisioning")) {
            return ProfilingDataManager.Subsystem.PROVISIONING;
        } else if (className.startsWith("com.evolveum.midpoint.task")) {
            return ProfilingDataManager.Subsystem.TASK_MANAGER;
        } else if (className.startsWith("com.evolveum.midpoint.wf")) {
            return ProfilingDataManager.Subsystem.WORKFLOW;
        } else {
            return null;
        }
    }

    private String getClassName(String fullClassName) {
        return fullClassName != null ? fullClassName.replace("com.evolveum.midpoint", "..") : null;
    }

    private static String getFullClassName(MethodInvocation invocation) {
        if (invocation.getThis() != null) {
            return invocation.getThis().getClass().getName();
        } else {
            return null;
        }
    }

    String getFullClassName() {
        return fullClassName;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getElapsedTimeMicros() {
        return elapsedTime / 1000;
    }

    public @CanBeNone long getOwnTimeMicros() {
        return ownTimeMicros;
    }

    public @CanBeNone long getCpuTimeMicros() {
        return cpuTime != NONE_LONG ? cpuTime / 1000 : NONE_LONG;
    }

    public long getInvocationId() {
        return invocationId;
    }

    private @CanBeNone long getCurrentCpuTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported() ? bean.getCurrentThreadCpuTime() : NONE_LONG;
    }
}
