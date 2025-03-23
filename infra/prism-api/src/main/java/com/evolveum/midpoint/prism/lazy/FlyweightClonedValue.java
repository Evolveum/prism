/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.lazy;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.deleg.*;

import com.google.common.collect.Collections2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.evolveum.midpoint.prism.CloneStrategy.LITERAL_MUTABLE;

public abstract class FlyweightClonedValue implements PrismValueDelegator {

    private Itemable parent;

    public static PrismValue from(@Nullable PrismValue value) {
        if (value == null) {
            return null;
        } else if (value instanceof FlyweightClonedValue flyweight) {
            return flyweight.copy();
        } else if (value instanceof PrismPropertyValue<?> property) {
            return new Property<>(property);
        } else if (value instanceof PrismContainerValue<?> container) {
            return new Container<>(container);
        } else if (value instanceof PrismReferenceValue reference) {
            return new Reference(reference);
        } else {
            throw new AssertionError("Unsupported prism value type: " + value.getClass());
        }
    }

    public static <T> PrismPropertyValue<T> from(@NotNull PrismPropertyValue<T> value) {
        if (value instanceof FlyweightClonedValue.Property<T> flyweight) {
            return flyweight.copy();
        } else {
            return new Property<>(value);
        }
    }

    public static <C extends Containerable> PrismContainerValue<C> from(@NotNull PrismContainerValue<C> value) {
        if (value instanceof FlyweightClonedValue.Container<C> flyweight) {
            return flyweight.copy();
        } else {
            return new Container<>(value);
        }
    }

    public static <O extends Objectable> PrismObjectValue<O> from(@NotNull PrismObjectValue<O> value) {
        if (value instanceof FlyweightClonedValue.ObjectValue<O> flyweight) {
            return flyweight.copy();
        } else {
            return new ObjectValue<>(value);
        }
    }

    public static PrismReferenceValue from(@NotNull PrismReferenceValue value) {
        if (value instanceof FlyweightClonedValue.Reference flyweight) {
            return flyweight.copy();
        } else {
            return new Reference(value);
        }
    }

    @Override
    public @Nullable Itemable getParent() {
        return parent;
    }

    @Override
    public void setParent(@Nullable Itemable parentValue) {
        parent = parentValue;
    }

    @Override
    public abstract PrismValue clone();

    @Override
    public void revive(PrismContext prismContext) {
        // NOOP
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public void freeze() {
        // NOOP
    }

    static class Property<T> extends FlyweightClonedValue implements PrismPropertyValueDelegator<T>  {

        private final PrismPropertyValue<T> delegate;

        public Property(PrismPropertyValue<T> delegate) {
            delegate.checkImmutable();
            this.delegate = delegate;
        }

        @Override
        public PrismPropertyValue<T> delegate() {
            return delegate;
        }

        @Override
        public PrismPropertyValue<T> clone() {
            return cloneComplex(LITERAL_MUTABLE);
        }

        @Override
        public PrismPropertyValue<T> cloneComplex(CloneStrategy strategy) {
            if (strategy.mutableCopy()) {
                return delegate.cloneComplex(strategy);
            } else {
                return new Property<>(delegate);
            }
        }
    }

    static class Container<T extends Containerable> extends FlyweightClonedValue implements PrismContainerValueDelegator<T> {

        private final PrismContainerValue<T> delegate;

        public Container(PrismContainerValue<T> delegate) {
            delegate.checkImmutable();
            this.delegate = delegate;
        }

        @Override
        public PrismContainerValue<T> delegate() {
            return delegate;
        }

        @Override
        public PrismContainerValue<T> clone() {
            return cloneComplex(LITERAL_MUTABLE);
        }

        @Override
        public @Nullable PrismContainerable<T> getParent() {
            return (PrismContainerable<T>) super.getParent();
        }

        @Override
        public PrismContainerValue<T> cloneComplex(CloneStrategy strategy) {
            if (strategy.mutableCopy()) {
                return delegate.cloneComplex(strategy);
            } else {
                return new Container<>(delegate);
            }
        }

        @Override
        public @NotNull Collection<Item<?, ?>> getItems() {
            return Collections2.transform(delegate().getItems(), this::wrapItem);
        }

        private Item<?,?> wrapItem(Item<?,?> item) {
            var ret = FlyweightClonedItem.from(item);
            ret.setParent(this);
            return ret;
        }
    }

    static class Reference extends FlyweightClonedValue implements PrismReferenceValueDelegator  {

        private final PrismReferenceValue delegate;

        Reference(PrismReferenceValue delegate) {
            delegate.checkImmutable();
            this.delegate = delegate;
        }

        @Override
        public PrismReferenceValue delegate() {
            return delegate;
        }

        @Override
        public PrismReferenceValue clone() {
            return cloneComplex(LITERAL_MUTABLE);
        }

        @Override
        public PrismReferenceValue cloneComplex(CloneStrategy strategy) {
            if (strategy.mutableCopy()) {
                return delegate.cloneComplex(strategy);
            } else {
                return new Reference(delegate);
            }
        }

        @Override
        public void shortDump(StringBuilder sb) {
            delegate.shortDump(sb);
        }
    }

    static class ObjectValue<O extends Objectable> extends Container<O> implements PrismObjectValueDelegator<O> {

        public ObjectValue(PrismObjectValue<O> delegate) {
            super(delegate);
        }

        @Override
        public PrismObjectValue<O> delegate() {
            return (PrismObjectValue<O>) super.delegate();
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public PrismObjectValue<O> clone() {
            return cloneComplex(LITERAL_MUTABLE);
        }

        @Override
        public PrismObjectValue<O> cloneComplex(CloneStrategy strategy) {
            if (strategy.mutableCopy()) {
                return delegate().cloneComplex(strategy);
            } else {
                return new ObjectValue<>(delegate());
            }
        }
    }
}
