package com.evolveum.midpoint.prism.impl.storage;

import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;

import com.evolveum.midpoint.util.exception.SchemaException;

import com.sun.xml.xsom.impl.scd.Iterators;
import org.jetbrains.annotations.NotNull;

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
    public ItemStorage<V> remove(V value, EquivalenceStrategy strategy) {
        if (strategy.equals(value, this.value)) {
            return ItemStorage.emptySingleValue();
        }
        return this;
    }

    @Override
    public ItemStorage<V> addForced(V newValue) {
        throw new UnsupportedOperationException("Add forced on single value item");
    }
}
