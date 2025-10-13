/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.key;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.PrismContainerValue;

import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.key.NaturalKeyDefinition;
import com.evolveum.midpoint.prism.path.ItemName;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;

/**
 * A {@link NaturalKeyDefinition} implementation that uses a simple list of constituent items to compare container values.
 *
 * No specific key merging is provided.
 */
public class DefaultNaturalKeyDefinitionImpl implements NaturalKeyDefinition {

    /** Items that constitute the natural key. */
    @NotNull private final Collection<QName> constituents;

    private DefaultNaturalKeyDefinitionImpl(@NotNull Collection<QName> constituents) {
        this.constituents = constituents;
    }

    public static DefaultNaturalKeyDefinitionImpl of(QName... constituents) {
        return new DefaultNaturalKeyDefinitionImpl(List.of(constituents));
    }

    @Override
    public boolean valuesMatch(PrismContainerValue<?> targetValue, PrismContainerValue<?> sourceValue) {
        for (QName keyConstituent : constituents) {
            Item<?, ?> targetKeyItem = targetValue.findItem(ItemName.fromQName(keyConstituent));
            Item<?, ?> sourceKeyItem = sourceValue.findItem(ItemName.fromQName(keyConstituent));
            if (areNotEquivalent(targetKeyItem, sourceKeyItem)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void mergeMatchingKeys(PrismContainerValue<?> targetValue, PrismContainerValue<?> sourceValue) {
        // No-op in this general case
    }

    private boolean areNotEquivalent(Item<?, ?> targetKeyItem, Item<?, ?> sourceKeyItem) {
        if (targetKeyItem != null && targetKeyItem.hasAnyValue()) {
            return !targetKeyItem.equals(sourceKeyItem, EquivalenceStrategy.DATA.exceptForValueMetadata());
        } else {
            return sourceKeyItem != null && sourceKeyItem.hasAnyValue();
        }
    }
}
