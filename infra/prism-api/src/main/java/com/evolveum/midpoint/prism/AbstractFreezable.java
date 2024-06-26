/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

import java.util.List;

import com.google.common.collect.ImmutableList;

public abstract class AbstractFreezable implements Freezable {

    private boolean frozen = false;

    @Override
    public final void freeze() {
        if (!frozen) {
            performFreeze();
            this.frozen = true;
        }
    }

    protected void freeze(Freezable child) {
        Freezable.freezeNullable(child);
    }

    protected void freezeAll(Iterable<? extends Freezable> children) {
        for (Freezable freezable : children) {
            freeze(freezable);
        }
    }

    protected void performFreeze() {
        // Intentional NOOP, for overriding
    }

    protected final boolean isMutable() {
        return !this.frozen;
    }

    @Override
    public final boolean isImmutable() {
        return this.frozen;
    }

    protected static <T> List<T> freezeNullableList(List<T> values) {
        if (values == null) {
            return null;
        }
        return ImmutableList.copyOf(values);
    }
}
