/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.util.statistics;

import com.evolveum.midpoint.util.ShortDumpable;

import java.util.Locale;

import static com.evolveum.midpoint.util.MiscUtil.or0;

/**
 *  Experimental.
 */
public class SingleOperationPerformanceInformation implements ShortDumpable {

    private int invocationCount;
    private long totalTime;
    private Long minTime;
    private Long maxTime;
    private Long ownTime;
    private Long minOwnTime;
    private Long maxOwnTime;

    public int getInvocationCount() {
        return invocationCount;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public Long getMinTime() {
        return minTime;
    }

    public Long getMaxTime() {
        return maxTime;
    }

    public Long getOwnTime() {
        return ownTime;
    }

    public Long getMinOwnTime() {
        return minOwnTime;
    }

    public Long getMaxOwnTime() {
        return maxOwnTime;
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
        if (minTime == null || time < minTime) {
            minTime = time;
        }
        if (maxTime == null || time > maxTime) {
            maxTime = time;
        }
    }

    private void addOwnTime(long time) {
        ownTime = or0(ownTime) + time;
        if (minOwnTime == null || time < minOwnTime) {
            minOwnTime = time;
        }
        if (maxOwnTime == null || time > maxOwnTime) {
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
        if (ownTime != null) {
            sb.append(", own time: ");
            sb.append(ownTime/1000).append(" ms");
            if (invocationCount > 0) {
                sb.append(String.format(Locale.US, " (min/max/avg: %.2f/%.2f/%.2f)",
                        minOwnTime/1000.0, maxOwnTime/1000.0, (float) ownTime / invocationCount / 1000.0));
            }
        }
    }
}
