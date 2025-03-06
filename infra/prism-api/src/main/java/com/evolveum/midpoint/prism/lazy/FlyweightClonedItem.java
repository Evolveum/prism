/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.lazy;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.deleg.ItemDelegator;
import com.evolveum.midpoint.prism.deleg.PrismPropertyDelegator;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Flyweight immutable item backed by delegate.
 *
 * Flyweight immutable item is special implementation of item, which is semantically clone
 * of another item, almost fully immutable, but allows this clone to be used in
 * different place (setParent is mutable - eg. deltas) without need to do deep clone of all children.
 *
 * When value is accessed a temporary flyweight clone with proper {@link PrismValue#getParent()} is created.
 * This lightweight clones are not cached and are created each time to simplify state tracking and
 * to prevent creation of full clone.
 *
 * @param <V>
 * @param <D>
 */
public abstract class FlyweightClonedItem<V extends PrismValue, D extends ItemDefinition<?>> implements ItemDelegator<V,D> {
    private PrismContainerValue<?> parent;


    public static Item<?,?> from(Item<?,?> item) {
        if (item instanceof FlyweightClonedItem<?,?> reparentedItem) {
            return reparentedItem.copy();
        }
        if (item instanceof PrismProperty<?> property) {
            return new Property<>(property);
        }
        return item.clone();
    }

    @Deprecated
    public static Item<?,?> copyOf(Item<?,?> item) {
        return from(item);
    }

    @Override
    public @Nullable PrismContainerValue<?> getParent() {
        return parent;
    }

    @Override
    public void setParent(@Nullable PrismContainerValue<?> parentValue) {
        parent = parentValue;
    }

    V wrapValue(V value) {
        var ret = createWrapped(value);
        ret.setParent(this);
        return ret;
    }

    protected abstract V createWrapped(V value);

    @Override
    public @NotNull List<V> getValues() {
        return Lists.transform(delegate().getValues(), this::wrapValue);
    }

    @Override
    public V getAnyValue(@NotNull ValueSelector<V> selector) {
        return wrapValue(delegate().getAnyValue(selector));
    }

    @Override
    public V getValue() {
        return wrapValue(delegate().getValue());
    }

    @Override
    public V getAnyValue() {
        return wrapValue(delegate().getAnyValue());
    }

    @Override
    public abstract Item<V, D> clone();


    private static class Property<T> extends FlyweightClonedItem<PrismPropertyValue<T>, PrismPropertyDefinition<T>> implements PrismPropertyDelegator<T>  {

        private final PrismProperty<T> delegate;

        public Property(PrismProperty<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public PrismProperty<T> delegate() {
            return delegate;
        }

        @Override
        public PrismProperty<T> clone() {
            return new Property<>(delegate);
        }

        @Override
        public PrismProperty<T> cloneComplex(CloneStrategy strategy) {
            return clone();
        }

        @Override
        protected PrismPropertyValue<T> createWrapped(PrismPropertyValue<T> value) {
            return new FlyweightClonedValue.Property<>(value);
        }
    }


}
