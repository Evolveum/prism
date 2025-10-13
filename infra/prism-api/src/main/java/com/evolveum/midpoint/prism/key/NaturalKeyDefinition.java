/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.key;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.path.ItemName;

/**
 * Describes how to handle a natural key of multivalued items.
 *
 * Responsibilities:
 *
 * 1. _Matching_ values of a multivalued item, so that the matching pairs can be appropriately merged.
 * 2. _Merging_ values of the key itself. For example, definitions of an attribute `ri:drink` with `ref`
 * of `drink` (unqualified) and `ri:drink` (qualified) have to be merged, and the result should use the
 * `ref` of `ri:drink`, i.e. the qualified version.
 *
 * (See also the description in {@link GenericItemMerger}, where this class is primarily used.)
 */
public interface NaturalKeyDefinition {

    /**
     * Returns `true` if the target and source container values match on their natural key.
     * (I.e. they have to be merged/diffed together.)
     */
    boolean valuesMatch(PrismContainerValue<?> targetValue, PrismContainerValue<?> sourceValue);

    /**
     * Merges natural key value in target and in source (assuming they match according to {@link #valuesMatch(PrismContainerValue,
     * PrismContainerValue)}), i.e. updates the key in targetValue if necessary.
     */
    void mergeMatchingKeys(PrismContainerValue<?> targetValue, PrismContainerValue<?> sourceValue);

    /**
     * Returns a collection of items that constitute the natural key for specific value.
     */
    default Collection<Item<?, ?>> getConstituents(PrismContainerValue<?> value) {
        PrismContainerDefinition<?> def = value.getDefinition();
        List<QName> constituents = def.getNaturalKeyConstituents();
        if (constituents == null || constituents.isEmpty()) {
            return null;
        }

        Collection<Item<?, ?>> items = new ArrayList<>();
        for (QName constituent : constituents) {
            Item<?, ?> item = value.findItem(ItemName.fromQName(constituent));
            if (item != null) {
                items.add(item);
            }
        }

        return items;
    }
}
