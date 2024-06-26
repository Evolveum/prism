/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import static com.evolveum.midpoint.prism.impl.GenericItemMerger.Kind.CONTAINER;
import static com.evolveum.midpoint.util.MiscUtil.argCheck;
import static com.evolveum.midpoint.util.MiscUtil.stateCheck;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemMerger;
import com.evolveum.midpoint.prism.key.NaturalKeyDefinition;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.PathKeyedMap;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * The generic item merger that follows these rules:
 *
 * 1. Matching property and reference values are overridden.
 * 2. Matching container values are merged recursively (using configured mergers for children).
 *
 * What are _matching_ values?
 *
 * 1. For single-valued items the values at source and target sides are automatically considered matching.
 * 2. For multi-valued items with a natural key defined, the values having the same key are considered matching.
 * 3. For multi-valued items without a natural key, no values are matching.
 */
public class GenericItemMerger extends BaseItemMerger<Item<?, ?>> {

    private static final Trace LOGGER = TraceManager.getTrace(GenericItemMerger.class);

    /** Natural key for the current item. If null, there's no natural key defined. */
    @Nullable private final NaturalKeyDefinition naturalKey;

    /** Mergers to be used for child items. */
    @NotNull private final PathKeyedMap<ItemMerger> childrenMergers;

    private GenericItemMerger(
            @Nullable OriginMarker originMarker,
            @Nullable NaturalKeyDefinition naturalKey,
            @NotNull PathKeyedMap<ItemMerger> childrenMergers,
            @NotNull MergeStrategy strategy) {
        super(originMarker);
        this.naturalKey = naturalKey;
        this.childrenMergers = childrenMergers;

        setStrategy(strategy);
    }

    public GenericItemMerger(
            @Nullable OriginMarker originMarker,
            @NotNull PathKeyedMap<ItemMerger> childrenMergers) {
        this(originMarker, childrenMergers, MergeStrategy.OVERLAY);
    }

    public GenericItemMerger(
            @Nullable OriginMarker originMarker,
            @NotNull PathKeyedMap<ItemMerger> childrenMergers,
            @NotNull MergeStrategy strategy) {
        this(originMarker, null, childrenMergers, strategy);
    }

    @SuppressWarnings("WeakerAccess")
    public GenericItemMerger(
            @Nullable OriginMarker originMarker,
            NaturalKeyDefinition naturalKey) {
        this(originMarker, naturalKey, new PathKeyedMap<>(), MergeStrategy.OVERLAY);
    }

    @Override
    public NaturalKeyDefinition getNaturalKey() {
        return naturalKey;
    }

    public void mergeContainerValues(@NotNull PrismContainerValue<?> targetPcv, @NotNull PrismContainerValue<?> sourcePcv)
            throws ConfigurationException, SchemaException {
        for (QName qName : determineItemNames(targetPcv, sourcePcv)) {
            LOGGER.trace("Merging {}", qName);
            ItemName itemName = ItemName.fromQName(qName);
            ItemMerger merger = determineChildMerger(itemName, sourcePcv);
            merger.merge(itemName, targetPcv, sourcePcv);
        }
    }

    @NotNull
    private ItemMerger determineChildMerger(ItemName itemName, PrismContainerValue<?> sourcePcv) {
        // First let's have a look at explicit path-based information
        ItemMerger byItemName = childrenMergers.get(itemName);
        if (byItemName != null) {
            LOGGER.trace("Child merger for {} obtained by item name: {}", itemName, byItemName);
            return byItemName;
        }

        // Then let's have a look at the type. Currently, we use definitions to determine it,
        // but maybe we could go with the real values (if this would not work).
        ComplexTypeDefinition ctd = sourcePcv.getComplexTypeDefinition();
        stateCheck(ctd != null, "No complex type definition of %s", sourcePcv);

        ItemDefinition<?> childDef = ctd.findItemDefinition(itemName);
        stateCheck(childDef != null, "No definition of %s in %s", itemName, ctd);

        ItemMerger merger = childDef.getMergerInstance(getStrategy(), originMarker);
        if (merger != null) {
            return merger;
        }

        Class<?> valueClass = childDef.getTypeClass();
        LOGGER.trace("Using default merger for {} (value class {})", itemName, valueClass);
        return createDefaultSubMerger(childDef, originMarker);
    }

    private ItemMerger createDefaultSubMerger(ItemDefinition<?> def, OriginMarker originMarker) {
        GenericItemMerger merger = new GenericItemMerger(
                originMarker,
                createSubChildMergersMap(def.getItemName()));
        merger.setStrategy(getStrategy());

        return merger;
    }

    private PathKeyedMap<ItemMerger> createSubChildMergersMap(@NotNull ItemName itemName) {
        PathKeyedMap<ItemMerger> childMap = new PathKeyedMap<>();
        for (Map.Entry<ItemPath, ItemMerger> entry : childrenMergers.entrySet()) {
            ItemPath path = entry.getKey();
            if (path.startsWith(itemName)) {
                childMap.put(path.rest(), entry.getValue());
            }
        }
        return childMap;
    }

    private Set<QName> determineItemNames(PrismContainerValue<?> target, PrismContainerValue<?> source) {
        Set<QName> itemNames = new HashSet<>();
        itemNames.addAll(target.getItemNames());
        itemNames.addAll(source.getItemNames());
        childrenMergers.keySet().stream()
                .filter(ItemPath::isSingleName)
                .forEach(path -> itemNames.add(path.asSingleNameOrFail()));
        return itemNames;
    }

    @Override
    protected void mergeInternal(@NotNull Item<?, ?> targetItem, @NotNull Item<?, ?> sourceItem)
            throws ConfigurationException, SchemaException {
        boolean isTargetItemSingleValued = isSingleValued(targetItem);
        boolean isSourceItemSingleValued = isSingleValued(sourceItem);
        stateCheck(isSourceItemSingleValued == isTargetItemSingleValued,
                "Mismatch between the cardinality of source and target items: single=%s (source) vs single=%s (target)",
                isSourceItemSingleValued, isTargetItemSingleValued);
        if (isSourceItemSingleValued) {
            LOGGER.trace(" -> Merging as single-valued item");
            mergeSingleValuedItem(targetItem, sourceItem);
        } else {
            LOGGER.trace(" -> Merging as multi-valued item");
            mergeMultiValuedItem(targetItem, sourceItem);
        }
    }

    private void mergeSingleValuedItem(Item<?, ?> targetItem, Item<?, ?> sourceItem)
            throws SchemaException, ConfigurationException {
        Kind kind = Kind.of(targetItem, sourceItem);
        if (kind == CONTAINER) {
            LOGGER.trace("Merging matching container (single) values");
            mergeContainerValues(
                    (PrismContainerValue<?>) getSingleValue(targetItem),
                    (PrismContainerValue<?>) getSingleValue(sourceItem));
        } else {
            if (isFullMerge()) {
                Item<PrismValue, ?> target = (Item<PrismValue, ?>) targetItem;

                PrismValue value = createMarkedClone(sourceItem.getValue());
                target.replace(value);
            } else {
                LOGGER.trace("Overriding non-container (single) value - i.e. keeping target item as is");
            }
        }
    }

    private @NotNull PrismValue getSingleValue(Item<?, ?> item) {
        stateCheck(item.size() == 1, "Single-valued non-empty item with %s values: %s", item.size(), item);
        return item.getValues().get(0);
    }

    private void mergeMultiValuedItem(Item<?, ?> targetItem, Item<?, ?> sourceItem)
            throws SchemaException, ConfigurationException {

        Set<PrismValue> targetValuesMatched = new HashSet<>();

        Kind kind = Kind.of(targetItem, sourceItem);
        for (PrismValue sourceValue : sourceItem.getValues()) {
            LOGGER.trace("Going to merge source value: {}", sourceValue);
            if (kind == CONTAINER) {
                PrismContainerValue<?> sourcePcv = (PrismContainerValue<?>) sourceValue;
                PrismContainerValue<?> matchingTargetValue =
                        findMatchingTargetValue((PrismContainer<?>) targetItem, sourcePcv);
                if (matchingTargetValue != null) {
                    LOGGER.trace(" -> Matching target value found, merging into it: {}", matchingTargetValue);
                    mergeKey(matchingTargetValue, sourcePcv);
                    mergeContainerValues(matchingTargetValue, sourcePcv);

                    targetValuesMatched.add(matchingTargetValue);
                } else {
                    LOGGER.trace(" -> Has no matching target value, so adding it to the target item (without ID) - if needed");
                    PrismValue added = addIfNotThere(targetItem, sourceValue);
                    if (added != null) {
                        targetValuesMatched.add(added);
                    }
                }
            } else {
                LOGGER.trace(" -> Not a container, adding the value right to the target item - if needed");
                PrismValue added = addIfNotThere(targetItem, sourceValue);
                if (added != null) {
                    targetValuesMatched.add(added);
                }
            }
        }

        if (isFullMerge()) {
            List<?> toBeRemoved = targetItem.getValues().stream()
                    .filter(v -> !targetValuesMatched.contains(v))
                    .toList();

            toBeRemoved.forEach(v -> targetItem.getValues().remove(v));
        }
    }

    private PrismValue addIfNotThere(Item<?, ?> targetItem, PrismValue sourceValue) throws SchemaException {
        //noinspection unchecked
        Item<PrismValue, ?> target = (Item<PrismValue, ?>) targetItem;
        PrismValue existing = target.findValue(sourceValue, VALUE_COMPARISON_STRATEGY);
        if (existing != null) {
            LOGGER.trace("     (but target contains the corresponding value - not adding)");
            return existing;
        }

        PrismValue cloned = createMarkedClone(sourceValue);
        target.add(cloned);

        return cloned;
    }

    private PrismContainerValue<?> findMatchingTargetValue(
            PrismContainer<?> targetItem, PrismContainerValue<?> sourceValue) throws ConfigurationException {
        if (naturalKey == null) {
            return null;
        }
        for (PrismContainerValue<?> targetValue : targetItem.getValues()) {
            if (naturalKey.valuesMatch(targetValue, sourceValue)) {
                return targetValue;
            }
        }
        return null;
    }

    private void mergeKey(PrismContainerValue<?> matchingTargetValue, PrismContainerValue<?> sourcePcv)
            throws ConfigurationException {
        if (naturalKey != null) {
            naturalKey.mergeMatchingKeys(matchingTargetValue, sourcePcv);
        }
    }

    private boolean isSingleValued(@NotNull Item<?, ?> item) throws SchemaException {
        ItemDefinition<?> definition =
                MiscUtil.requireNonNull(
                        item.getDefinition(),
                        () -> "Cannot merge item without definition: " + item);
        return definition.isSingleValue();
    }

    enum Kind {
        PROPERTY, REFERENCE, CONTAINER;

        static @NotNull Kind of(@NotNull Item<?, ?> item) {
            if (item instanceof PrismProperty<?>) {
                return PROPERTY;
            } else if (item instanceof PrismReference) {
                return REFERENCE;
            } else if (item instanceof PrismContainer<?>) {
                return CONTAINER;
            } else {
                throw new IllegalArgumentException("Unknown item type: " + MiscUtil.getValueWithClass(item));
            }
        }

        static @NotNull Kind of(@NotNull Item<?, ?> targetItem, @NotNull Item<?, ?> sourceItem) {
            Kind target = of(targetItem);
            Kind source = of(sourceItem);
            argCheck(source == target,
                    "Mismatching kinds for target (%s) and source (%s) items: %s, %s",
                    target, source, targetItem, sourceItem);
            return target;
        }
    }
}
