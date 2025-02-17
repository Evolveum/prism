/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.util.statistics;

import com.evolveum.midpoint.util.DebugDumpable;

import java.util.Map;

public interface OperationsPerformanceInformation extends DebugDumpable {

    void clear();

    Map<String, SingleOperationPerformanceInformation> getAllData();

    default SingleOperationPerformanceInformation get(String name) {
        return getAllData().get(name);
    }

    default int getInvocationCount(String name) {
        var op = get(name);
        return op != null ? op.getInvocationCount() : 0;
    }

    default long getTotalTime(String name) {
        var op = get(name);
        return op != null ? op.getTotalTime() : 0;
    }

    default Long getOwnTime(String name) {
        var op = get(name);
        return op != null ? op.getOwnTime() : null;
    }
}
