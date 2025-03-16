/*
 * Copyright (C) 2010-2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.lazy;

import java.io.Serializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContainerable;
import com.evolveum.midpoint.prism.deleg.PrismContainerValueDelegator;
import com.evolveum.midpoint.util.annotation.Experimental;

/**
 * Lazily evaluated {@link PrismContainerValue}.
 *
 * The materialization, as well as some key information necessary in the lazy state (e.g., the PCV ID) are provided by
 * {@link ValueSource} instance.
 *
 * EXPERIMENTAL. Tailored to the needs of midPoint magic assignment.
 *
 * @param <C>
 */
@Experimental
public class GenericLazyPrismContainerValue<C extends Containerable> implements PrismContainerValueDelegator<C> {

    private PrismContainerable<C> parent;

    private Object value;

    private GenericLazyPrismContainerValue(Object value) {
        this.value = value;
    }

    public static <C extends Containerable> GenericLazyPrismContainerValue<C> from(@NotNull ValueSource<C> source) {
        return new GenericLazyPrismContainerValue<>(source);
    }

    private PrismContainerValue<C> materialized() {
        if (value instanceof ValueSource<?> source) {
            value = source.get();
            ((PrismContainerValue<?>) value).setParent(parent);
        }
        //noinspection unchecked
        return (PrismContainerValue<C>) value;
    }

    /** This will materialize the value on almost any API call (which is OK for now). */
    public PrismContainerValue<C> delegate() {
        return materialized();
    }

    @Override
    public @Nullable PrismContainerable<C> getParent() {
        return parent;
    }

    @Override
    public void setParent(@Nullable Itemable parentValue) {
        //noinspection unchecked
        parent = (PrismContainerable<C>) parentValue;
        if (value instanceof PrismContainerValue<?> pcv) {
            pcv.setParent(parentValue);
        }
    }

    @Override
    public Long getId() {
        if (value instanceof ValueSource<?> source) {
            return source.getId();
        } else {
            return delegate().getId();
        }
    }

    @Override
    public <T> @Nullable T getRealValue() {
        if (value instanceof ValueSource<?> source) {
            //noinspection unchecked
            return (T) source.getRealValue();
        } else {
            return delegate().getRealValue();
        }
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public void freeze() {
        // NOOP
    }

    @Override
    public PrismContainerValue<C> clone() {
        return PrismContainerValueDelegator.super.clone();
    }

    public interface ValueSource<C extends Containerable> extends Serializable {

        PrismContainerValue<C> get();

        Long getId();

        C getRealValue();
    }
}
