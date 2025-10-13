/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.logging;

/**
 * Gathers log lines as they are produced by {@link TracingAppender}.
 */
public interface LoggingEventSink {

    /**
     * Consumes a log line.
     */
    void consume(String line);
}
