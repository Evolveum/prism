/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
