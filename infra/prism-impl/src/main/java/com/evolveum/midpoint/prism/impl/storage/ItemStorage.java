package com.evolveum.midpoint.prism.impl.storage;

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

public interface ItemStorage<V> extends Iterable<V> {

    boolean isEmpty();

    int size();

    @Override
    Iterator<V> iterator();
    /**
     * Adds value to item storage and returns optimized item storage for resulting situation.
     *
     * This may result in creating new item storage with value added, instead of original item storage.
     * If original item storage is still sufficient for storing item, it will be used instead.
     *
     * Usage pattern should be:
     * <code>
     *     itemStorage = itemStorage.add(value);
     * </code>
     *
     * @param newValue Adds new value to item storage
     * @return Item Storage with value added, note that this may be another instance with original item storage
     *         unchanged.
     * @throws IllegalStateException
     */
    ItemStorage<V> add(V newValue) throws IllegalStateException;

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
}
