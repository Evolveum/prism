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
import org.jetbrains.annotations.Nullable;

public abstract class FlyweightClonedItem<V extends PrismValue, D extends ItemDefinition<?>> implements ItemDelegator<V,D> {
    private PrismContainerValue<?> parent;

    public static Item<?,?> copyOf(Item<?,?> item) {
        if (item instanceof FlyweightClonedItem<?,?> reparentedItem) {
            return reparentedItem.copy();
        }
        if (item instanceof PrismProperty<?> property) {
            return new Property<>(property);
        }
        return item.clone();
    }

    @Override
    public @Nullable PrismContainerValue<?> getParent() {
        return parent;
    }

    @Override
    public void setParent(@Nullable PrismContainerValue<?> parentValue) {
        parent = parentValue;
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
    }
}
