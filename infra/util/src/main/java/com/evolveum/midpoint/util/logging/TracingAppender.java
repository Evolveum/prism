/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import org.apache.commons.lang3.StringUtils;

/**
 * Collects log entries e.g. for tracing purposes in midPoint.
 *
 * It sends log lines to an instance of {@link LoggingEventSink} that has been set up for the current thread.
 */
public class TracingAppender<E> extends AppenderBase<E> {

    private Layout<E> layout;

    private static final ThreadLocal<LoggingEventSink> EVENT_SINK_THREAD_LOCAL = new ThreadLocal<>();

    @Override
    protected void append(E eventObject) {
        LoggingEventSink loggingEventSink = EVENT_SINK_THREAD_LOCAL.get();
        if (loggingEventSink != null) {
            String text = layout.doLayout(eventObject);
            String normalized = StringUtils.removeEnd(text, "\n");
            loggingEventSink.consume(normalized);
        }
    }

    public Layout<E> getLayout() {
        return layout;
    }

    public void setLayout(Layout<E> layout) {
        this.layout = layout;
    }

    public static void removeSink() {
        EVENT_SINK_THREAD_LOCAL.remove();
    }

    public static void setSink(LoggingEventSink sink) {
        EVENT_SINK_THREAD_LOCAL.set(sink);
    }
}
