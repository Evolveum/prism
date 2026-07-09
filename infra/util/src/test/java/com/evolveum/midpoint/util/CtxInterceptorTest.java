/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import com.evolveum.midpoint.tools.testng.AbstractUnitTest;

/**
 * Verifies the behavior of {@code ctx-interceptor.xml}.
 *
 * The context file configures method profiling using {@link
 * org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator}. This test checks the observable proxying
 * behavior, regardless of how the bean-name list is represented internally. See MID-11630.
 */
public class CtxInterceptorTest extends AbstractUnitTest {

    private static final String[] INSTRUMENTABLE_BEAN_NAMES = {
            "repositoryService",
            "cacheRepositoryService",
            "taskManager",
            "caseManager",
            "provisioningService",
            "synchronizationService",
            "modelController",
            "modelInteractionService",
            "auditService"
    };

    /**
     * Checks that enabling profiling causes all configured service bean names to be wrapped by Spring AOP proxies.
     */
    @Test
    public void ctxInterceptorProxiesInstrumentableBeansWhenProfilingIsEnabled() {
        try (GenericApplicationContext context = createContext(true)) {
            for (String beanName : INSTRUMENTABLE_BEAN_NAMES) {
                assertTrue(beanName + " should be proxied when profiling is enabled",
                        AopUtils.isAopProxy(context.getBean(beanName)));
            }
        }
    }

    /**
     * Checks that disabling profiling leaves the configured service bean names as ordinary non-proxied beans.
     */
    @Test
    public void ctxInterceptorDoesNotProxyInstrumentableBeansWhenProfilingIsDisabled() {
        try (GenericApplicationContext context = createContext(false)) {
            for (String beanName : INSTRUMENTABLE_BEAN_NAMES) {
                assertFalse(beanName + " should not be proxied when profiling is disabled",
                        AopUtils.isAopProxy(context.getBean(beanName)));
            }
        }
    }

    private GenericApplicationContext createContext(boolean profilingEnabled) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("midpointConfiguration", TestMidpointConfiguration.class, () ->
                new TestMidpointConfiguration(profilingEnabled));

        for (String beanName : INSTRUMENTABLE_BEAN_NAMES) {
            context.registerBeanDefinition(beanName, new RootBeanDefinition(TestService.class));
        }

        new XmlBeanDefinitionReader(context)
                .loadBeanDefinitions(new ClassPathResource("ctx-interceptor.xml"));
        context.refresh();
        return context;
    }

    public static class TestMidpointConfiguration {

        private final boolean profilingEnabled;

        TestMidpointConfiguration(boolean profilingEnabled) {
            this.profilingEnabled = profilingEnabled;
        }

        @SuppressWarnings("unused")
        public boolean isProfilingEnabled() {
            return profilingEnabled;
        }
    }

    public static class TestService {

        @SuppressWarnings("unused")
        public void operation() {
        }
    }
}
