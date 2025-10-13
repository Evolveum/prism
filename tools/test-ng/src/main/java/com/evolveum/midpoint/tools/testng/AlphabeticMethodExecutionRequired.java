/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.tools.testng;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the method in the test class require sequential execution.
 * Many midPoint tests, especially integrated, require that for many test methods all
 * (or at least some) of the previous methods must run as well.
 *
 * Note that marking these test classes with `@Listeners(AlphabeticalMethodInterceptor.class)`
 * is not what we want because the listener annotation is treated globally for the whole suite.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE })
public @interface AlphabeticMethodExecutionRequired {
}
