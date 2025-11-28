/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.delta.builder;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.annotation.Experimental;

import javax.xml.namespace.QName;
import java.util.List;

public interface S_ItemEntry {

    /**
     * Should we skip idempotent item deltas? (Default is `false`.)
     *
     * **Beware**: this method may return a new instance, so be sure to store it; otherwise, you may lose the deltas you
     * add.
     */
    @Experimental S_ItemEntry optimizing();

    /**
     * The state of object before the delta is applied. Used to compute estimatedOldValues.
     *
     * **Beware**: this method may return a new instance, so be sure to store it; otherwise, you may lose the deltas you
     * add.
     */
    @Experimental S_ItemEntry oldObject(Containerable object);

    /**
     * Add new item delta.
     *
     * **Beware**: this method may return a new instance, so be sure to store it; otherwise, you may lose the deltas you
     * add.
     *
     * @param names The names forming path to the item.
     * @return The (potentially new) instance of an item delta value builder.
     */
    S_ValuesEntry item(QName... names);
    /**
     * Add new item delta.
     *
     * **Beware**: this method may return a new instance, so be sure to store it; otherwise, you may lose the deltas you
     * add.
     *
     * @param namesOrIds The names or Ids forming path to the item.
     * @return The (potentially new) instance of an item delta value builder.
     */
    S_ValuesEntry item(Object... namesOrIds);
    /**
     * Add new item delta.
     *
     * **Beware**: this method may return a new instance, so be sure to store it; otherwise, you may lose the deltas you
     * add.
     *
     * @param path The path to the item.
     * @return The (potentially new) instance of an item delta value builder.
     */
    S_ValuesEntry item(ItemPath path);
    /**
     * Add new item delta.
     *
     * **Beware**: this method may return a new instance, so be sure to store it; otherwise, you may lose the deltas you
     * add.
     *
     * @param path The path to the item.
     * @param itemDefinition The definition of the item.
     * @return The (potentially new) instance of an item delta value builder.
     */
    S_ValuesEntry item(ItemPath path, ItemDefinition itemDefinition);

    /**
     * Add new property delta.
     *
     * **Beware**: this method may return a new instance, so be sure to store it; otherwise, you may lose the deltas you
     * add.
     *
     * Can be used with dynamic paths.
     *
     * @param names The names forming path to the property.
     * @return The (potentially new) instance of a property delta value builder.
     */
    S_ValuesEntry property(QName... names);
    /**
     * Add new property delta.
     *
     * **Beware**: this method may return a new instance, so be sure to store it; otherwise, you may lose the deltas you
     * add.
     *
     * Can be used with dynamic paths.
     *
     * @param namesOrIds The names or Ids forming path to the property.
     * @return The (potentially new) instance of a property delta value builder.
     */
    S_ValuesEntry property(Object... namesOrIds);
    /**
     * Add new property delta.
     *
     * **Beware**: this method may return a new instance, so be sure to store it; otherwise, you may lose the deltas you
     * add.
     *
     * Can be used with dynamic paths.
     *
     * @param path The path to the property.
     * @return The (potentially new) instance of a property delta value builder.
     */
    S_ValuesEntry property(ItemPath path);
    /**
     * Add new property delta.
     *
     * **Beware**: this method may return a new instance, so be sure to store it; otherwise, you may lose the deltas you
     * add.
     *
     * Can be used with dynamic paths.
     *
     * @param path The path to the property.
     * @param itemDefinition The definition of the property.
     * @return The (potentially new) instance of a property delta value builder.
     */
    <T> S_ValuesEntry property(ItemPath path, PrismPropertyDefinition<T> itemDefinition);

    List<ObjectDelta<?>> asObjectDeltas(String oid);
    <O extends Objectable> ObjectDelta<O> asObjectDelta(String oid);

    @Deprecated // Now this is the same as `asObjectDelta`
    default <X extends Objectable> ObjectDelta<X> asObjectDeltaCast(String oid) {
        return asObjectDelta(oid);
    }

    ItemDelta<?,?> asItemDelta();
    List<ItemDelta<?,?>> asItemDeltas();

}
