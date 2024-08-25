/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.axiom.concepts.Lazy;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Statically holds an instance of PrismContext (and maybe other beans later).
 */
public abstract class PrismService {

    private static final PrismService DEFAULT_INSTANCE = new Mutable();

    private static Supplier<PrismService> serviceSupplier = defaultSupplier();

    private PrismService() {
    }

    public static PrismService get() {
        return serviceSupplier.get();
    }

    public static Supplier<PrismService> defaultSupplier() {
        return () -> DEFAULT_INSTANCE;
    }

    public static void overrideSupplier(Supplier<PrismService> supplier) {
        serviceSupplier = Objects.requireNonNull(supplier);
    }

    public abstract PrismContext prismContext();

    public abstract void prismContext(PrismContext prismContext);
    public static class Mutable extends PrismService {
        private PrismContext context;

        @Override
        public void prismContext(PrismContext prismContext) {
            context = prismContext;
        }

        @Override
        public PrismContext prismContext() {
            return context;
        }
    }
}
