/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.key;

import static java.util.Map.entry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.key.NaturalKeyDefinition;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

public class NaturalKeyDefinitionImpl implements NaturalKeyDefinition {

    private static final Function<QName, NaturalKeyDefinition> DEFAULT_CONSTITUENT_HANDLER =
            (constituent) -> DefaultNaturalKeyDefinitionImpl.of(constituent);

    private static final @NotNull Map<Class<?>, Function<QName, NaturalKeyDefinition>> CONSTITUENT_HANDLERS =
            Map.ofEntries(
                    entry(ItemPathType.class, (constituent) -> ItemPathNaturalKeyDefinitionImpl.of(new ItemName(constituent)))
            );

    @NotNull private final Collection<QName> constituents;

    private NaturalKeyDefinitionImpl(@NotNull Collection<QName> constituents) {
        this.constituents = constituents;
    }

    public static NaturalKeyDefinitionImpl of(Collection<QName> constituents) {
        return new NaturalKeyDefinitionImpl(constituents);
    }

    public static NaturalKeyDefinitionImpl of(QName... constituents) {
        return new NaturalKeyDefinitionImpl(List.of(constituents));
    }

    @Override
    public boolean valuesMatch(PrismContainerValue<?> targetValue, PrismContainerValue<?> sourceValue) {
        PrismContainerDefinition<?> targetDef = targetValue.getDefinition();

        for (QName constituent : constituents) {
            ItemDefinition<?> itemDefinition = targetDef.findItemDefinition(ItemName.fromQName(constituent));
            Class<?> itemType = itemDefinition.getTypeClass();

            NaturalKeyDefinition handler = CONSTITUENT_HANDLERS
                    .getOrDefault(itemType, DEFAULT_CONSTITUENT_HANDLER)
                    .apply(constituent);

            if (!handler.valuesMatch(targetValue, sourceValue)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void mergeMatchingKeys(PrismContainerValue<?> targetValue, PrismContainerValue<?> sourceValue) {
        PrismContainerDefinition<?> targetDef = targetValue.getDefinition();

        for (QName constituent : constituents) {
            ItemDefinition<?> itemDefinition = targetDef.findItemDefinition(ItemName.fromQName(constituent));
            Class<?> itemType = itemDefinition.getTypeClass();

            NaturalKeyDefinition handler = CONSTITUENT_HANDLERS
                    .getOrDefault(itemType, DEFAULT_CONSTITUENT_HANDLER)
                    .apply(constituent);

            handler.mergeMatchingKeys(targetValue, sourceValue);
        }
    }
}
