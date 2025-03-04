/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.lazy;

import com.evolveum.midpoint.prism.*;

import com.evolveum.midpoint.prism.deleg.PrismPropertyValueDelegator;
import org.jetbrains.annotations.NotNull;

public class ReparentedPropertyValue<T> extends AbstractFreezable implements PrismPropertyValueDelegator<T> {


    private Itemable parent;
    private final PrismPropertyValue<T> delegate;

    protected ReparentedPropertyValue(PrismPropertyValue<T> delegate) {
        this.delegate = delegate;
    }

    public static <T> ReparentedPropertyValue<T> from(PrismPropertyValue<T> value) {
        if (value instanceof ReparentedPropertyValue) {
            return (ReparentedPropertyValue<T>) value.clone(); // This is shallow clone
        }
        return new ReparentedPropertyValue<>(value);
    }

    @Override
    public PrismPropertyValue<T> delegate() {
        return delegate;
    }

    @Override
    public void setParent(Itemable parent) {
        checkMutable();
        this.parent = parent;
    }

    @Override
    public Itemable getParent() {
        return parent;
    }

    @Override
    public void revive(PrismContext prismContext) {

    }

    @Override
    public PrismPropertyValue<T> clone() {
        return from(delegate);
    }

    @Override
    public PrismPropertyValue<T> cloneComplex(@NotNull CloneStrategy strategy) {
        return from(delegate);
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}

