/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.statistics;

import com.evolveum.midpoint.util.DebugUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class OperationsPerformanceInformationImpl implements OperationsPerformanceInformation {

    // operation -> performance information
    private final Map<String, SingleOperationPerformanceInformation> operationsMap = new ConcurrentHashMap<>();

    public void clear() {
        operationsMap.clear();
    }

    @Override
    public Map<String, SingleOperationPerformanceInformation> getAllData() {
        return operationsMap;
    }

    public void register(OperationInvocationRecord invocation) {
        // operationMap.compute is also atomic, but always replaces new value (even if the reference did not change)
        // so I think this is more efficient, even if it creates empty object each time
        String key = invocation.getFullClassName() + "." + invocation.getMethodName();
        operationsMap.putIfAbsent(key, new SingleOperationPerformanceInformation());
        operationsMap.get(key).register(invocation);
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.debugDumpLabelLn(sb, "Operations performance information", indent);
        ArrayList<String> operations = new ArrayList<>(operationsMap.keySet());
        operations.sort(String::compareTo);
        for (String operation : operations) {
            SingleOperationPerformanceInformation info = operationsMap.get(operation);
            if (info != null) {
                DebugUtil.debugDumpWithLabelLn(sb, operation, info.shortDump(), indent+1);
            }
        }
        return sb.toString();
    }
}
