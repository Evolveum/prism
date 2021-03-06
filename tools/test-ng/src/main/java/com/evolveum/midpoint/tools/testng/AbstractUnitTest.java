/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.tools.testng;

import java.lang.reflect.Method;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Base test class providing basic {@link MidpointTestMixin} implementation.
 * Can be extended by any unit test class that otherwise doesn't extend anything.
 */
public abstract class AbstractUnitTest implements MidpointTestMixin {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // region perf-test support
    private TestMonitor testMonitor;

    /** Called only by tests that need it, implements performance mixin interface. */
    public TestMonitor createTestMonitor() {
        testMonitor = new TestMonitor();
        return testMonitor;
    }

    /** Called only by tests that need it, implements performance mixin interface. */
    public void destroyTestMonitor() {
        testMonitor = null;
    }

    /** Called only by tests that need it, implements performance mixin interface. */
    public TestMonitor testMonitor() {
        return testMonitor;
    }

    // see the comment in PerformanceTestMethodMixin for explanation
    @BeforeMethod
    public void initTestMethodMonitor() {
        if (this instanceof PerformanceTestMethodMixin) {
            createTestMonitor();
        }
    }

    // see the comment in PerformanceTestMethodMixin for explanation
    @AfterMethod
    public void dumpMethodReport(Method method) {
        if (this instanceof PerformanceTestMethodMixin) {
            ((PerformanceTestMethodMixin) this).dumpReport(
                    getClass().getSimpleName() + "#" + method.getName());
        }
    }

    // see the comment in PerformanceTestClassMixin for explanation
    @BeforeClass
    public void initTestClassMonitor() {
        if (this instanceof PerformanceTestClassMixin) {
            createTestMonitor();
        }
    }

    // see the comment in PerformanceTestClassMixin for explanation
    @AfterClass
    public void dumpClassReport() {
        if (this instanceof PerformanceTestClassMixin) {
            ((PerformanceTestClassMixin) this).dumpReport(getClass().getSimpleName());
        }
    }
    // endregion

    @BeforeClass
    public void displayTestClassTitle() {
        displayTestTitle("Starting TEST CLASS: " + getClass().getName());
    }

    @AfterClass
    public void displayTestClassFooter() {
        displayTestFooter("Finishing with TEST CLASS: " + getClass().getName());
    }

    @BeforeMethod
    public void startTestContext(ITestResult testResult) {
        SimpleMidpointTestContext context = SimpleMidpointTestContext.create(testResult);
        displayTestTitle(context.getTestName());
    }

    @AfterMethod
    public void finishTestContext(ITestResult testResult) {
        SimpleMidpointTestContext context = SimpleMidpointTestContext.get();
        displayTestFooter(context.getTestName(), testResult);
        SimpleMidpointTestContext.destroy();
    }

    @Override
    @Nullable
    public MidpointTestContext getTestContext() {
        return SimpleMidpointTestContext.get();
    }

    @Override
    public Logger logger() {
        return logger;
    }
}
