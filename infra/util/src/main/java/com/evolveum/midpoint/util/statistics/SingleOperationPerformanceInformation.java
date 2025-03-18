/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.util.statistics;

import com.evolveum.midpoint.util.ShortDumpable;

import java.util.Locale;

/**
 *  Experimental.
 */
public class SingleOperationPerformanceInformation implements ShortDumpable {

    private static final long NONE = -1;

    private int invocationCount;
    private long totalTime;
    private long minTime = NONE;
    private long maxTime = NONE;
    private long ownTime = NONE;
    private long minOwnTime = NONE;
    private long maxOwnTime = NONE;

    public int getInvocationCount() {
        return invocationCount;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public Long getMinTime() {
        return minTime != NONE ? minTime : null;
    }

    public Long getMaxTime() {
        return maxTime != NONE ? maxTime : null;
    }

    public Long getOwnTime() {
        return ownTime != NONE ? ownTime : null;
    }

    public Long getMinOwnTime() {
        return minOwnTime != NONE ? minOwnTime : null;
    }

    public Long getMaxOwnTime() {
        return maxOwnTime != NONE ? maxOwnTime : null;
    }

    public synchronized void register(OperationInvocationRecord operation) {
        invocationCount++;
        addTotalTime(operation.getElapsedTimeMicros());
        var ownTime = operation.getOwnTimeMicros();
        if (ownTime != null) {
            addOwnTime(ownTime);
        }
    }

    private void addTotalTime(long time) {
        totalTime += time;
        if (minTime == NONE || time < minTime) {
            minTime = time;
        }
        if (maxTime == NONE || time > maxTime) {
            maxTime = time;
        }
    }

    private void addOwnTime(long time) {
        ownTime = (ownTime == NONE ? 0 : ownTime) + time;
        if (minOwnTime == NONE || time < minOwnTime) {
            minOwnTime = time;
        }
        if (maxOwnTime == NONE || time > maxOwnTime) {
            maxOwnTime = time;
        }
    }

    @Override
    public synchronized void shortDump(StringBuilder sb) {
        sb.append(invocationCount);
        sb.append(", total time: ");
        sb.append(totalTime/1000).append(" ms");
        if (invocationCount > 0) {
            sb.append(String.format(Locale.US, " (min/max/avg: %.2f/%.2f/%.2f)", minTime/1000.0, maxTime/1000.0,
                    (float) totalTime / invocationCount / 1000.0));
        }
        if (ownTime != NONE) {
            sb.append(", own time: ");
            sb.append(ownTime/1000).append(" ms");
            if (invocationCount > 0) {
                sb.append(String.format(Locale.US, " (min/max/avg: %.2f/%.2f/%.2f)",
                        minOwnTime/1000.0, maxOwnTime/1000.0, (float) ownTime / invocationCount / 1000.0));
            }
        }
    }
}
