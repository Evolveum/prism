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

    /** Should we skip idempotent item deltas? (Default is `false`.) */
    @Experimental S_ItemEntry optimizing();

    /** The state of object before the delta is applied. Used to compute estimatedOldValues. */
    @Experimental S_ItemEntry oldObject(Containerable object);

    S_ValuesEntry item(QName... names);
    S_ValuesEntry item(Object... namesOrIds);
    S_ValuesEntry item(ItemPath path);
    S_ValuesEntry item(ItemPath path, ItemDefinition itemDefinition);

    /**
     * Can be used with dynamic paths.
     */
    S_ValuesEntry property(QName... names);
    S_ValuesEntry property(Object... namesOrIds);
    S_ValuesEntry property(ItemPath path);
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
