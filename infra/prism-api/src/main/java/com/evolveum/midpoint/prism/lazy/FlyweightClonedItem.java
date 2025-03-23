/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.lazy;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.deleg.*;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.evolveum.midpoint.prism.CloneStrategy.LITERAL_MUTABLE;

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
public abstract class FlyweightClonedItem<V extends PrismValue, D extends ItemDefinition<?>>
        implements ItemDelegator<V,D> {

    private PrismContainerValue<?> parent;

    public static @NotNull Item<?,?> from(@NotNull Item<?,?> item) {
        if (item instanceof FlyweightClonedItem<?,?> flyweight) {
            return flyweight.copy();
        } else if (item instanceof PrismProperty<?> property) {
            return new Property<>(property);
        } else if (item instanceof PrismObject<?> object) {
            return new ObjectItem<>(object);
        } else if (item instanceof PrismContainer<?> container) {
            return new Container<>(container);
        } else if (item instanceof PrismReference reference) {
            return new Reference(reference);
        } else {
            throw new AssertionError("Unsupported item type: " + item.getClass());
        }
    }

    public static <T> @NotNull PrismProperty<T> from(@NotNull PrismProperty<T> property) {
        if (property instanceof FlyweightClonedItem.Property<T> flyweight) {
            return flyweight.copy();
        } else {
            return new Property<>(property);
        }
    }

    public static <C extends Containerable> @NotNull PrismContainer<C> from(@NotNull PrismContainer<C> container) {
        if (container instanceof FlyweightClonedItem.Container<C> flyweight) {
            return flyweight.copy();
        } else {
            return new Container<>(container);
        }
    }

    public static <O extends Objectable> @NotNull PrismObject<O> from(@NotNull PrismObject<O> object) {
        if (object instanceof FlyweightClonedItem.ObjectItem<O> flyweight) {
            return flyweight.copy();
        } else {
            return new ObjectItem<>(object);
        }
    }

    public static @NotNull PrismReference from(@NotNull PrismReference reference) {
        if (reference instanceof FlyweightClonedItem.Reference flyweight) {
            return flyweight.copy();
        } else {
            return new Reference(reference);
        }
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

    private V wrapValue(V value) {
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

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Item<V, D> clone() {
        return cloneComplex(LITERAL_MUTABLE);
    }

    static class Property<T>
            extends FlyweightClonedItem<PrismPropertyValue<T>, PrismPropertyDefinition<T>>
            implements PrismPropertyDelegator<T>  {

        private final PrismProperty<T> delegate;

        Property(PrismProperty<T> delegate) {
            delegate.checkImmutable();
            this.delegate = delegate;
        }

        @Override
        public PrismProperty<T> delegate() {
            return delegate;
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public PrismProperty<T> clone() {
            return cloneComplex(LITERAL_MUTABLE);
        }

        @Override
        public @NotNull PrismProperty<T> cloneComplex(@NotNull CloneStrategy strategy) {
            if (strategy.mutableCopy()) {
                return delegate.cloneComplex(strategy);
            } else {
                return new Property<>(delegate);
            }
        }

        @Override
        protected PrismPropertyValue<T> createWrapped(PrismPropertyValue<T> value) {
            return new FlyweightClonedValue.Property<>(value);
        }
    }

    static class Container<C extends Containerable>
            extends FlyweightClonedItem<PrismContainerValue<C>, PrismContainerDefinition<C>>
            implements PrismContainerDelegator<C> {

        private final PrismContainer<C> delegate;

        public Container(PrismContainer<C> delegate) {
            delegate.checkImmutable();
            this.delegate = delegate;
        }

        @Override
        public PrismContainer<C> delegate() {
            return delegate;
        }

        @Override
        protected PrismContainerValue<C> createWrapped(PrismContainerValue<C> value) {
            return new FlyweightClonedValue.Container<>(value);
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public PrismContainer<C> clone() {
            return cloneComplex(LITERAL_MUTABLE);
        }

        @Override
        public @NotNull PrismContainer<C> cloneComplex(@NotNull CloneStrategy strategy) {
            if (strategy.mutableCopy()) {
                return delegate.cloneComplex(strategy);
            } else {
                return new Container<>(delegate);
            }
        }
    }

    static class ObjectItem<C extends Objectable>
            extends Container<C>
            implements PrismObjectDelegator<C> {

        ObjectItem(PrismObject<C> delegate) {
            super(delegate);
        }

        @Override
        public PrismObject<C> delegate() {
            return (PrismObject<C>) super.delegate();
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public PrismObject<C> clone() {
            return cloneComplex(LITERAL_MUTABLE);
        }

        @Override
        public @NotNull PrismObject<C> cloneComplex(@NotNull CloneStrategy strategy) {
            if (strategy.mutableCopy()) {
                return delegate().cloneComplex(strategy);
            } else {
                return new ObjectItem<>(delegate());
            }
        }

        @Override
        protected PrismContainerValue<C> createWrapped(PrismContainerValue<C> value) {
            return super.createWrapped(value);
        }

        @Override
        public @NotNull PrismObjectValue<C> getValue() {
            return (PrismObjectValue<C>) super.getValue();
        }
    }

    static class Reference
            extends FlyweightClonedItem<PrismReferenceValue, PrismReferenceDefinition>
            implements PrismReferenceDelegator {

        private final PrismReference delegate;

        Reference(PrismReference delegate) {
            delegate.checkImmutable();
            this.delegate = delegate;
        }

        @Override
        public PrismReference delegate() {
            return delegate;
        }

        @Override
        protected PrismReferenceValue createWrapped(PrismReferenceValue value) {
            return new FlyweightClonedValue.Reference(value);
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public PrismReference clone() {
            return cloneComplex(LITERAL_MUTABLE);
        }

        @Override
        public @NotNull PrismReference cloneComplex(@NotNull CloneStrategy strategy) {
            if (strategy.mutableCopy()) {
                return delegate.cloneComplex(strategy);
            } else {
                return new Reference(delegate);
            }
        }
    }
}
