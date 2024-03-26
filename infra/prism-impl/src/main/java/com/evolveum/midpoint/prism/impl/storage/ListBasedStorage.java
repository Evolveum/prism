package com.evolveum.midpoint.prism.impl.storage;

import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;

import com.evolveum.midpoint.prism.impl.PrismContainerValueImpl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ListBasedStorage<V extends PrismValue> extends MultiValueStorage<V> {

    private final List<V> storage = new ArrayList<>();

    ListBasedStorage() {

    }

    ListBasedStorage(V value) {
        storage.add(value);
    }

    ListBasedStorage(ItemStorage<V> values) {
        for (var v : values) {
            storage.add(v);
        }
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
            iterateAndRemoveEquivalentValues(owner, newValue, strategy);
        }
        checkKeyUnique(owner, newValue);
        storage.add(newValue);
        return this;
    }

    @Override
    Iterator<V> mutableIterator() {
        return storage.iterator();
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

    static abstract  class AbstractKeyed<K,V extends PrismValue> extends ListBasedStorage<V> implements KeyedStorage<K,V> {

        protected AbstractKeyed() {
            super();
        }

        protected AbstractKeyed(ItemStorage<V> values) {
            super(values);
        }

        @Override
        protected void checkKeyUnique(@NotNull Itemable owner, V newValue) {
            var newKey = extractKey(newValue);
            if (newKey != null) {
                for (V existingValue : this) {
                    var existingKey = extractKey(existingValue);
                    if (newKey.equals(existingKey)) {
                        throw new IllegalStateException("Attempt to add a value with an key that already exists: " + newKey);
                    }
                }
            }
        }

        @Override
        public @Nullable V get(K key) {
            Preconditions.checkArgument(key != null, "Null Key provided for get");
            for (V value : this) {
                var valueKey = extractKey(value);
                if (key.equals(valueKey)) {
                    return value;
                }
            }
            return null;
        }

        @Override
        public KeyedStorage<K,V> add(@NotNull Itemable owner, @NotNull V newValue, @Nullable EquivalenceStrategy strategy) throws IllegalStateException, ExactValueExistsException {
            super.add(owner, newValue, strategy);
            return changeImplementationIfNeeded(newValue);
        }

        @Override
        public KeyedStorage<K,V> addForced(V newValue) {
            super.addForced(newValue);
            return changeImplementationIfNeeded(newValue);
        }

        @Override
        public KeyedStorage<K, V> remove(V value, EquivalenceStrategy strategy) throws ValueDoesNotExistsException {
            return (KeyedStorage<K, V>) super.remove(value, strategy);
        }

        protected KeyedStorage<K,V> changeImplementationIfNeeded(V lastAddedValue) {
            if (extractKey(lastAddedValue) == null) {
                return downgraded();
            }
            if (size() >= DEFAULT_MAP_THRESHOLD) {
                return upgraded();
            }
            return this;
        }

        protected KeyedStorage<K,V> downgraded() {
            return new KeyedDowngraded<>(this);
        }

        abstract protected KeyedStorage<K,V> upgraded();
    }

    /**
     * Implementation of keyed storage which was downgraded (contains value without key).
     *
     * This is fallback implementation when we started with well behaved storage - all items had key, but then ended up with
     * add which added value without key - so we do not know how to reason about keys in future.
     *
     * @param <K>
     * @param <V>
     */
    static class KeyedDowngraded<K,V extends PrismValue> extends AbstractKeyed<K,V> {

        private final Function<V,K> keyExtractor;

        public KeyedDowngraded(KeyedStorage<K, V> original) {
            super(original);
            this.keyExtractor = original.keyExtractor();
        }

        @Override
        public Function<V, K> keyExtractor() {
            return keyExtractor;
        }

        @Override
        protected KeyedStorage<K, V> changeImplementationIfNeeded(V lastAddedValue) {
            return this;
        }

        @Override
        protected KeyedStorage<K, V> upgraded() {
            return this;
        }
    }

    static abstract class KeyedUpgradable<K,V extends PrismValue> extends AbstractKeyed<K,V> {

    }


}
