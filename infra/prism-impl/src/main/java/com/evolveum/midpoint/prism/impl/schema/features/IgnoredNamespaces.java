/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.schema.features;

import java.util.List;

import org.jetbrains.annotations.Nullable;

/** Just the value holder to ensure type safety; for the "ignored namespaces" definition feature. */
public class IgnoredNamespaces
        extends AbstractValueWrapper.ForList<String> {

    private IgnoredNamespaces(List<String> values) {
        super(values);
    }

    static @Nullable List<String> unwrap(@Nullable IgnoredNamespaces wrapper) {
        return wrapper != null ? wrapper.getValue() : null;
    }

    static @Nullable IgnoredNamespaces wrap(@Nullable List<String> values) {
        return values != null && !values.isEmpty() ? new IgnoredNamespaces(values) : null;
    }
}
