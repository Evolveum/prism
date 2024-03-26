package com.evolveum.midpoint.prism.impl.storage;

import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;

import com.evolveum.midpoint.util.exception.SchemaException;

import com.sun.xml.xsom.impl.scd.Iterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


class SingleValueStorage<V extends PrismValue> extends AbstractMutableStorage<V> {

    V value;

    SingleValueStorage(V value) {
        this.value = value;
    }


    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Iterator<V> iterator() {
        return Iterators.singleton(value);
    }

    @Override
    public ItemStorage<V> add(@NotNull Itemable owner, @NotNull V newValue, EquivalenceStrategy strategy) throws IllegalStateException, ExactValueExistsException, SchemaException {
        if (strategy != null && strategy.equals(this.value, newValue)) {
            if (isDefaultEquals(strategy) || exactEquals(this.value, newValue)) {
                throw ExactValueExistsException.INSTANCE;
            }
            valueRemoved(this.value);
            this.value = newValue;
            return this;
        }
        throw new SchemaException("Attempt to put more than one value to single-valued item " + owner + "; newly added value: " + newValue);
    }

    @Override
    public List<V> asList() {
        return Collections.singletonList(value);
    }

    @Override
    public boolean containsSingleValue() {
        return true;
    }

    @Override
    public V getOnlyValue() throws IllegalStateException {
        return value;
    }

    @Override
    public ItemStorage<V> remove(V value, EquivalenceStrategy strategy) throws ValueDoesNotExistsException {
        if (strategy.equals(value, this.value)) {
            return ItemStorage.emptySingleValue();
        }
        throw ValueDoesNotExistsException.INSTANCE;
    }

    @Override
    public ItemStorage<V> addForced(V newValue) {
        throw new UnsupportedOperationException("Add forced on single value item");
    }

    static class Empty<V extends PrismValue> extends EmptyStorage<V> {

        @Override
        public ItemStorage<V> add(@NotNull Itemable owner, @NotNull V newValue, EquivalenceStrategy strategy) throws IllegalStateException, ExactValueExistsException, SchemaException {
            return new SingleValueStorage<>(newValue);
        }

        @Override
        public ItemStorage<V> addForced(V newValue) {
            return new SingleValueStorage<>(newValue);
        }
    }


    static abstract class Keyed<K,V extends PrismValue> extends SingleValueStorage<V>  implements KeyedStorage<K, V> {

        protected Keyed(V value) {
            super(value);
        }

        @Override
        public @Nullable V get(K key) {
            if (key.equals(extractKey(value))) {
                return value;
            }
            return null;
        }

        @Override
        public KeyedStorage<K, V> add(@NotNull Itemable owner, @NotNull V newValue, EquivalenceStrategy strategy) throws IllegalStateException, ExactValueExistsException, SchemaException {
            super.add(owner, newValue, strategy);
            return this;
        }

        @Override
        public KeyedStorage<K, V> addForced(V newValue) {
            super.addForced(newValue);
            return this;
        }

        @Override
        public KeyedStorage<K, V> remove(V value, EquivalenceStrategy strategy) throws ValueDoesNotExistsException {
            if (strategy.equals(value, this.value)) {
                return this.createEmpty();
            }
            throw ValueDoesNotExistsException.INSTANCE;
        }

        protected abstract KeyedStorage<K,V> createEmpty();
    }
}
