package com.evolveum.midpoint.prism.impl.storage;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;

import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * Collection-like storage type designed specially for Prism purposes.
 *
 * It differs from normal mutable collections in edit patterns - modification operations
 * may perform modification on current instance or return new instance with modification
 * applied. This allows us to choose ideal storage based on current and previous storage type.
 *
 * In case of following operations:
 *  var multiValue = true;
 *  var keyed = true;
 *  storage  = ItemStorage.create(multiValue, keyed); -> returns ListBasedKeyedStorage (more performance for smaller item sets)
 *  storage = storage.add(new PrismContainerValueImpl(1)); -> has key (containerId), reuses same instance
 *  ...
 *  storage = storage.add(new PrismContainerValueImpl(50)); -> returns MapBasedKeyedStorage (more effective for  larger item sets)
 *  ...
 *  storage = storage.add(new PrismContainerValueImpl()); -> value without key, we need to downgrade to ListBasedStorage
 *
 * @param <V>
 */

public interface ItemStorage<V extends PrismValue> extends Iterable<V> {

    static ItemStorage<PrismValue> EMPTY_LIST_BASED = new EmptyStorage<>((v) -> new ListBasedStorage<>().addForced(v));
    static ItemStorage EMPTY_SINGLE_VALUE = new EmptyStorage<>(SingleValueStorage::new);


    static <V extends PrismValue> ItemStorage<V> createListBased() {
        return new ListBasedStorage();
    }
    static <V extends PrismValue> ItemStorage<V> emptySingleValue() {
        return EMPTY_SINGLE_VALUE;
    }

    static <V extends PrismContainerValue<?>> ItemStorage<V> containerList() {
        return new ListBasedStorage.Container<>();
    }

    boolean isEmpty();

    int size();

    @Override
    Iterator<V> iterator();
    /**
     * Adds value to item storage and returns optimized item storage for resulting situation.
     *
     *
     * If strategy is provided and it is different from
     * {@link com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy#DEFAULT_FOR_EQUALS}
     * this also removes other equivalent values.
     *
     * This may result in creating new item storage with value added, instead of original item storage.
     * If original item storage is still sufficient for storing item, it will be used instead.
     *
     * Usage pattern should be:
     * <code>
     *     itemStorage = itemStorage.add(owner, value, strategy);
     * </code>
     *
     * @param owner Owner / parent of this item storage and new value (used for error reporting mostly)
     * @param newValue Adds new value to item storage
     * @return Item Storage with value added, note that this may be another instance with original item storage
     *         unchanged.
     * @throws IllegalStateException If item storage if frozen / unmodifiable
     * @throws ExactValueExistsException If exactly equivalent value already present in storage.
     */
    ItemStorage<V> add(@NotNull Itemable owner, @NotNull V newValue, EquivalenceStrategy strategy)
            throws IllegalStateException, ExactValueExistsException, SchemaException;

    /**
     * Returns list view of item storage.
     *
     */
    List<V> asList();

    /**
     *
     * FIXME: should this be anyValue to be more generic?
     *
     * @return Only Value of item storage
     * @throws IllegalStateException If item storage is empty
     */
    V getOnlyValue() throws IllegalStateException;
    default boolean containsSingleValue() {
        return size() == 1;
    }

    ItemStorage<V> remove(V value, EquivalenceStrategy strategy) throws ValueDoesNotExistsException;

    @Experimental
    ItemStorage<V> addForced(V newValue);


    interface Factory<V extends PrismValue> {
        ItemStorage<V> create();
    }
}
