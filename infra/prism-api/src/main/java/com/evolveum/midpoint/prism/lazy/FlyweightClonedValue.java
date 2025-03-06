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

public abstract class FlyweightClonedValue implements PrismValueDelegator {

    private Itemable parent;

    public static PrismValue from(@Nullable PrismValue value) {
        if (value == null) {
            return null;
        }
        if (value instanceof FlyweightClonedValue flyweight) {
            return flyweight.clone();
        }
        if (value instanceof PrismPropertyValue<?> property) {
            return new Property<>(property);
        }
        if (value instanceof PrismContainerValue<?> container) {
            return new Container<>(container);
        }
        if (value instanceof PrismReferenceValue reference) {
            return new Reference(reference);
        }
        return value.clone();
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
            this.delegate = delegate;
        }

        @Override
        public PrismPropertyValue<T> delegate() {
            return delegate;
        }

        @Override
        public PrismPropertyValue<T> clone() {
            return new Property<>(delegate);
        }

        @Override
        public PrismPropertyValue<T> cloneComplex(CloneStrategy strategy) {
            return clone();
        }
    }

    static class Container<T extends Containerable> extends FlyweightClonedValue implements PrismContainerValueDelegator<T> {

        private final PrismContainerValue<T> delegate;

        public Container(PrismContainerValue<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public PrismContainerValue<T> delegate() {
            return delegate;
        }

        @Override
        public PrismContainerValue<T> clone() {
            return new Container<>(delegate);
        }

        @Override
        public @Nullable PrismContainerable<T> getParent() {
            return (PrismContainerable<T>) super.getParent();
        }

        @Override
        public PrismContainerValue<T> cloneComplex(CloneStrategy strategy) {
            return clone();
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

        public Reference(PrismReferenceValue delegate) {
            this.delegate = delegate;
        }

        @Override
        public PrismReferenceValue delegate() {
            return delegate;
        }

        @Override
        public PrismReferenceValue clone() {
            return new Reference(delegate);
        }

        @Override
        public PrismReferenceValue cloneComplex(CloneStrategy strategy) {
            return clone();
        }

        @Override
        public void shortDump(StringBuilder sb) {
            delegate.shortDump(sb);
        }
    }
}
