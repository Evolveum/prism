/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util.aspect;

import com.evolveum.midpoint.util.statistics.OperationInvocationRecord;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import static com.evolveum.midpoint.util.NoValueUtil.NONE_LONG;

/**
 *  In this class, we define some Pointcuts in AOP meaning that will provide join points for most common
 *  methods used in main midPoint subsystems. We wrap these methods with profiling wrappers.
 *
 *  This class also serves another purpose - it is used for basic Method Entry/Exit or args profiling,
 *  results from which are dumped to idm.log (by default)
 *
 *  @author shood
 * */

@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class MidpointInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
        OperationInvocationRecord ctx = OperationInvocationRecord.create(invocation);
        try {
            return ctx.processReturnValue(invocation.proceed());
        } catch (Throwable e) {
            throw ctx.processException(e);
        } finally {
            ctx.afterCall(invocation, NONE_LONG);
        }
    }
}
