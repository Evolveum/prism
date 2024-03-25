package com.evolveum.midpoint.prism.impl.storage;

import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy.DEFAULT_FOR_EQUALS;

public class ListBasedStorage<V extends PrismValue> extends MultiValueStorage<V> {

    private final List<V> storage = new ArrayList<>();

    ListBasedStorage() {

    }

    ListBasedStorage(V value) {
        storage.add(value);
    }

    @Override
    public boolean isEmpty() {
        return storage.isEmpty();
    }

    @Override
    public int size() {
        return storage.size();
    }

    @Override
    public Iterator<V> iterator() {
        return Iterators.unmodifiableIterator(storage.iterator());
    }

    @Override
    public ItemStorage<V> add(@NotNull Itemable owner, @NotNull V newValue, @Nullable EquivalenceStrategy strategy) throws IllegalStateException, ExactValueExistsException {

        if (strategy != null) {
            boolean exactEquivalentFound = false;
            boolean somethingRemoved = false;
            Iterator<V> iterator = storage.iterator();
            while (iterator.hasNext()) {
                V currentValue = iterator.next();
                if (strategy.equals(currentValue, newValue)) {
                    if (!exactEquivalentFound &&
                            (isDefaultEquals(strategy)) || exactEquals(currentValue, newValue)) {
                        exactEquivalentFound = true;
                    } else {
                        iterator.remove();
                        somethingRemoved = true;
                        valueRemoved(currentValue);
                    }
                }
            }
            if (exactEquivalentFound && !somethingRemoved) {
                throw ExactValueExistsException.INSTANCE;
            }
        }
        checkKeyUnique(owner, newValue);
        storage.add(newValue);
        return this;
    }



    @Override
    public ItemStorage<V> remove(V value, EquivalenceStrategy strategy) throws ValueDoesNotExistsException {
        boolean changed = false;
        Iterator<V> iterator = storage.iterator();
        while (iterator.hasNext()) {
            V val = iterator.next();
            if (val.representsSameValue(value, false) || val.equals(value, strategy)) {
                iterator.remove();
                valueRemoved(val);
                changed = true;
            }
        }
        if (!changed) {
            throw ValueDoesNotExistsException.INSTANCE;
        }
        return this;
    }

    @Override
    public ItemStorage<V> addForced(V newValue) {
        storage.add(newValue);
        return this;
    }

    @Override
    public List<V> asList() {
        // FIXME: Return unmodifiable view?
        return storage;
    }

    @Override
    public V getOnlyValue() throws IllegalStateException {
        return storage.get(0);
    }

    public static class Container<V extends PrismContainerValue<?>> extends ListBasedStorage<V> {
        @Override
        protected void checkKeyUnique(@NotNull Itemable owner, V newValue) {
            if (newValue.getId() != null) {
                for (PrismContainerValue existingValue : this) {
                    if (existingValue.getId() != null && existingValue.getId().equals(newValue.getId())) {
                        throw new IllegalStateException("Attempt to add a container value with an id that already exists: " + newValue.getId());
                    }
                }
            }
        }
    }
}
