package com.evolveum.midpoint.prism.impl.storage;

import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface ContainerValueStorage<V extends PrismContainerValue<?>> extends KeyedStorage<Long, V> {

    static Function<PrismContainerValue<?>, Long > CONTAINER_ID_EXTRACTOR = v-> v.getId();

    static ContainerValueStorage EMPTY_SINGLE = new EmptySingle();
    @Override
    default Function<V, Long> keyExtractor() {
        return (Function<V, Long>) CONTAINER_ID_EXTRACTOR;
    }

    @Override
    default Long extractKey(V value) {
        return value.getId();
    }

    static <V extends PrismContainerValue<?>> ContainerValueStorage<V> emptySingleValue() {
        return EMPTY_SINGLE;
    }

    static <V extends PrismContainerValue<?>> ContainerValueStorage<V> emptyMultiValue() {
        return new Indexed<>();
    }

    static class Downgraded<V extends PrismContainerValue<?>> extends ListBasedStorage.AbstractKeyed<Long, V>
            implements ContainerValueStorage<V> {
        public Downgraded(KeyedStorage<Long, V> original) {
            super(original);
        }

        @Override
        protected KeyedStorage<Long, V> changeImplementationIfNeeded(V lastAddedValue) {
            return this;
        }

        @Override
        protected KeyedStorage<Long, V> downgraded() {
            return this;
        }

        @Override
        protected KeyedStorage<Long, V> upgraded() {
            return this;
        }
    }

    class Upgradeable<V extends PrismContainerValue<?>> extends ListBasedStorage.KeyedUpgradable<Long, V>
            implements ContainerValueStorage<V> {

        @Override
        public Long extractKey(V value) {
            return super.extractKey(value);
        }

        @Override
        protected KeyedStorage<Long, V> upgraded() {
            return new Indexed<>(this);
        }

        @Override
        protected KeyedStorage<Long, V> downgraded() {
            return new Downgraded<>(this);
        }
    }

    static class Indexed<V extends PrismContainerValue<?>> extends MapBasedStorage<Long, V>
            implements ContainerValueStorage<V> {

        Indexed() {
            super();
        }

        Indexed(KeyedStorage<Long, V> original) {
            copyValues(original);
        }

        @Override
        public Long extractKey(V value) {
            return value.getId();
        }

        @Override
        protected KeyedStorage<Long, V> createDowngraded() {
            return new Downgraded(this);
        }
    }

    static class EmptySingle<V extends PrismContainerValue<?>> extends EmptyStorage.Keyed<Long, V> implements ContainerValueStorage<V> {


        @Override
        public KeyedStorage<Long, V> add(@NotNull Itemable owner, @NotNull V newValue, EquivalenceStrategy strategy) throws IllegalStateException, ExactValueExistsException, SchemaException {
            return new Single<>(newValue);
        }

        @Override
        public KeyedStorage<Long, V> addForced(V newValue) {
            return new Single<>(newValue);
        }

        @Override
        public KeyedStorage<Long, V> remove(V value, EquivalenceStrategy strategy) throws ValueDoesNotExistsException {
            throw ValueDoesNotExistsException.INSTANCE;
        }
    }

    static class Single<V extends PrismContainerValue<?>> extends SingleValueStorage.Keyed<Long, V> implements ContainerValueStorage<V> {

        public Single(V value) {
            super(value);
        }

        @Override
        protected KeyedStorage<Long, V> createEmpty() {
            return emptySingleValue();
        }
    }
}
