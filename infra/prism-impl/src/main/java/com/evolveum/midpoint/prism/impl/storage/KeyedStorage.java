package com.evolveum.midpoint.prism.impl.storage;

import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismValue;

import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface KeyedStorage<K,V extends PrismValue> extends ItemStorage<V> {

    static int DEFAULT_MAP_THRESHOLD = 20;



    @Nullable V get(K key);

    Function<V, K> keyExtractor();

    @Override
    KeyedStorage<K,V> add(@NotNull Itemable owner, @NotNull V newValue, EquivalenceStrategy strategy) throws IllegalStateException, ExactValueExistsException, SchemaException;

    @Override
    KeyedStorage<K,V> addForced(V newValue);

    @Override
    KeyedStorage<K,V> remove(V value, EquivalenceStrategy strategy) throws ValueDoesNotExistsException;
    default  K extractKey(V value) {
        return keyExtractor().apply(value);
    }
}
