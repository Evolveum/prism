package com.evolveum.midpoint.prism.impl.storage;

import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;

import com.evolveum.midpoint.util.exception.SchemaException;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

abstract class EmptyStorage<V extends PrismValue> implements ItemStorage<V> {

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
    public ItemStorage<V> remove(V value, EquivalenceStrategy strategy) throws ValueDoesNotExistsException {
        throw ValueDoesNotExistsException.INSTANCE;
    }

    static abstract class Keyed<K,V extends PrismValue> extends EmptyStorage<V> implements KeyedStorage<K,V> {

        @Override
        public abstract KeyedStorage<K, V> add(@NotNull Itemable owner, @NotNull V newValue, EquivalenceStrategy strategy) throws IllegalStateException, ExactValueExistsException, SchemaException;

        @Override
        public abstract KeyedStorage<K, V> addForced(V newValue);

        @Override
        public KeyedStorage<K, V>  remove(V value, EquivalenceStrategy strategy) throws ValueDoesNotExistsException {
            throw ValueDoesNotExistsException.INSTANCE;
        }


        @Override
        public @Nullable V get(K key) {
            return null;
        }
    }

}
