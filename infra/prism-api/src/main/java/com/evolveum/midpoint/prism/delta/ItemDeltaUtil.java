/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.delta;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.util.CloneUtil;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;

/**
 * Utilities related to {@link ItemDelta} and {@link ItemDeltaType} objects.
 */
public class ItemDeltaUtil {

    public static boolean isEmpty(ItemDeltaType itemDeltaType) {
        if (itemDeltaType == null) {
            return true;
        }
        if (itemDeltaType.getModificationType() == ModificationTypeType.REPLACE) {
            return false;
        }
        return !itemDeltaType.getValue().isEmpty();
    }

    /** Converts the old state of an item and the delta into "plus/minus/zero" information. */
    public static <IV extends PrismValue,ID extends ItemDefinition<?>> PrismValueDeltaSetTriple<IV> toDeltaSetTriple(
            Item<IV, ID> itemOld, ItemDelta<IV, ID> delta) throws SchemaException {
        if (itemOld == null && delta == null) {
            return null;
        } else if (delta == null) {
            PrismValueDeltaSetTriple<IV> triple = PrismContext.get().deltaFactory().createPrismValueDeltaSetTriple();
            triple.addAllToZeroSet(PrismValueCollectionsUtil.cloneCollection(itemOld.getValues()));
            return triple;
        } else {
            return delta.toDeltaSetTriple(itemOld);
        }
    }

    /**
     * The {@link #toDeltaSetTriple(Item, ItemDelta)} for whole objects. It is necessary mainly because {@link ObjectDelta}
     * is not an {@link ItemDelta} (although {@link PrismObject} is an {@link Item}).
     */
    @Experimental
    public static <O extends Objectable> PrismValueDeltaSetTriple<PrismObjectValue<O>> toDeltaSetTriple(
            PrismObject<O> objectOld, ObjectDelta<O> delta) throws SchemaException {
        if (objectOld == null && delta == null) {
            return null;
        } else if (delta == null) {
            PrismValueDeltaSetTriple<PrismObjectValue<O>> triple =
                    PrismContext.get().deltaFactory().createPrismValueDeltaSetTriple();
            triple.addToZeroSet(objectOld.getValue().clone());
            return triple;
        } else {
            return delta.toDeltaSetTriple(objectOld);
        }
    }

    // TODO move to Item
    public static <V extends PrismValue, D extends ItemDefinition<?>> ItemDelta<V, D> createAddDeltaFor(Item<V, D> item) {
        ItemDelta<V, D> rv = item.createDelta(item.getPath());
        rv.addValuesToAdd(item.getClonedValues());
        return rv;
    }

    // TODO move to Item
    @SuppressWarnings("unchecked")
    public static <V extends PrismValue, D extends ItemDefinition<?>> ItemDelta<V, D> createAddDeltaFor(Item<V, D> item,
            PrismValue value) {
        ItemDelta<V, D> rv = item.createDelta(item.getPath());
        rv.addValueToAdd((V) CloneUtil.clone(value));
        return rv;
    }

}
