/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.util.statistics;

import com.evolveum.midpoint.util.DebugDumpable;

import java.util.Map;

/**
 *
 */
public interface OperationsPerformanceInformation extends DebugDumpable {

    void clear();

    Map<String, SingleOperationPerformanceInformation> getAllData();
}
