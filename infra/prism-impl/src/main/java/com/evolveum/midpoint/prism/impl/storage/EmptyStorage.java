package com.evolveum.midpoint.prism.impl.storage;

import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

class EmptyStorage<V extends PrismValue> implements ItemStorage<V> {

    private static final Iterator EMPTY_ITERATOR = new Iterator() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new IllegalStateException("No next value");
        }
    };

    private final Function<V, ItemStorage<V>> nextStorage;

    public EmptyStorage(Function<V, ItemStorage<V>> nextStorage) {
        this.nextStorage = nextStorage;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<V> iterator() {
        return EMPTY_ITERATOR;
    }

    @Override
    public ItemStorage<V> add(@NotNull Itemable owner, @NotNull V newValue, EquivalenceStrategy strategy) throws IllegalStateException, ExactValueExistsException {
        return nextStorage.apply(newValue);
    }

    @Override
    public List<V> asList() {
        return Collections.emptyList();
    }

    @Override
    public V getOnlyValue() throws IllegalStateException {
        throw new IllegalStateException("Empty list. No value present.");
    }

    @Override
    public boolean containsSingleValue() {
        return false;
    }

    @Override
    public ItemStorage<V> remove(V value, EquivalenceStrategy strategy) {
        return this;
    }

    @Override
    public ItemStorage<V> addForced(V newValue) {
        return nextStorage.apply(newValue);
    }
}
