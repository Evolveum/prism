/**
 * Copyright (C) 2014-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.tools.testng;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

public class AlphabeticalMethodInterceptor implements IMethodInterceptor {

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<Object> instanceList = Lists.newArrayList();
        Map<Object, List<IMethodInstance>> map = Maps.newHashMap();
        for (IMethodInstance mi : methods) {
            Object instance = mi.getInstance();
            if (!instanceList.contains(instance)) {
                instanceList.add(instance);
            }
            List<IMethodInstance> l = map.computeIfAbsent(instance, k -> Lists.newArrayList());
            l.add(mi);
        }

        Comparator<IMethodInstance> comparator = Comparator.comparing(o -> o.getMethod().getMethodName());
        List<IMethodInstance> result = Lists.newArrayList();
        for (Object instance : instanceList) {
            List<IMethodInstance> methodList = map.get(instance);
            IMethodInstance[] array = methodList.toArray(new IMethodInstance[0]);
            Arrays.sort(array, comparator);
            result.addAll(Arrays.asList(array));
        }

        return result;
    }
}
