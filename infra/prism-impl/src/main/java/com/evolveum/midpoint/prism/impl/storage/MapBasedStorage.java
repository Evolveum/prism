package com.evolveum.midpoint.prism.impl.storage;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.evolveum.midpoint.util.exception.SchemaException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import org.apache.commons.collections4.keyvalue.AbstractMapEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;

abstract class MapBasedStorage<K,V extends PrismValue> extends MultiValueStorage<V> implements KeyedStorage<K,V> {

    protected final Map<K,V> storage = new LinkedHashMap<>();


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
        return Iterators.unmodifiableIterator(mutableIterator());
    }

    @Override
    Iterator<V> mutableIterator() {
        return Iterators.transform(storage.entrySet().iterator(), Map.Entry::getValue);
    }

    @Override
    public KeyedStorage<K, V> add(@NotNull Itemable owner, @NotNull V newValue, @Nullable EquivalenceStrategy strategy) throws IllegalStateException, ExactValueExistsException, SchemaException {
        if (shouldDowngrade(newValue)) {
            return createDowngraded().add(owner, newValue, strategy);
        }
        if (strategy != null) {
            // Only if strategy allows for different keys


            iterateAndRemoveEquivalentValues(owner, newValue, strategy);
        }
        checkKeyUnique(owner, newValue);
        return addForced(newValue);
    }

    @Override
    protected void checkKeyUnique(@NotNull Itemable owner, V value) {
        super.checkKeyUnique(owner, value);
    }

    @Override
    public KeyedStorage<K,V> addForced(V newValue) {
        if (shouldDowngrade(newValue)) {
            return createDowngraded().addForced(newValue);
        }
        var key = extractKey(newValue);
        var existing = storage.put(key,newValue);
        Preconditions.checkState(existing == null, "Key was already in use.");
        return this;
    }

    boolean shouldDowngrade(V newValue) {
        return extractKey(newValue) == null;
    }

    @Override
    public @Nullable V get(K key) {
        return storage.get(key);
    }

    @Override
    public KeyedStorage<K,V> remove(V value, EquivalenceStrategy strategy) throws ValueDoesNotExistsException {
        return (KeyedStorage<K, V>) super.remove(value, strategy);
    }

    @Override
    public List<V> asList() {
        // FIXME: Return real view
        return Collections.unmodifiableList(new ArrayList<>( storage.values()));
    }

    @Override
    public V getOnlyValue() throws IllegalStateException {
        return iterator().next();
    }

    protected KeyedStorage<K,V> createDowngraded() {
        return new ListBasedStorage.KeyedDowngraded(this);
    }

    static class ExtractorBased<K, V extends PrismValue> extends MapBasedStorage<K, V> {

        private final Function<V, K> extractor;

        public ExtractorBased(Function<V, K> extractor) {
            this.extractor = extractor;
        }

        public ExtractorBased(KeyedStorage<K,V> original) {
            this(original.keyExtractor());
            copyValues(original);

        }

        @Override
        public Function<V, K> keyExtractor() {
            return extractor;
        }
    }



    protected void copyValues(KeyedStorage<K, V> original) {
        for (var value : original) {
            var key = extractKey(value);
            Preconditions.checkArgument(key != null, "Value {} does not  have key.", value);
            storage.put(key, value);
        }
    }

}
