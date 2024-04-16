/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemMerger;
import com.evolveum.midpoint.prism.impl.key.NaturalKeyImpl;
import com.evolveum.midpoint.prism.key.NaturalKey;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

public class ItemMergerFactoryImpl implements ItemMergerFactory {

    private static final Trace LOGGER = TraceManager.getTrace(ItemMergerFactoryImpl.class);

    record TypedMergerSupplier(Class<?> type, Function<OriginMarker, ItemMerger> mergerFunction) {
    }

    private final Map<Class<?>, Function<OriginMarker, ItemMerger>> typeSpecificMergers = new HashMap<>();

    private final Map<String, TypedMergerSupplier> identifierSpecificMergers = new HashMap<>();

    public void registerMergerSupplier(
            @NotNull String identifier, @NotNull Class<?> type, @NotNull Function<OriginMarker, ItemMerger> supplier) {

        identifierSpecificMergers.put(identifier, new TypedMergerSupplier(type, supplier));
        typeSpecificMergers.put(type, supplier);
    }

    @Override
    public @Nullable ItemMerger createMerger(
            @NotNull ItemDefinition<?> definition, @NotNull MergeStrategy strategy, @Nullable OriginMarker originMarker) {

        ItemName itemName = definition.getItemName();
        Class<?> valueClass = definition.getTypeClass();

        // try to find merger based on definition annotations
        ItemMerger mergerByAnnotation = findMergerByAnnotation(definition, originMarker);
        if (mergerByAnnotation != null) {
            LOGGER.trace(
                    "Annotation-specific {} for {} (value class {}) with key {}",
                    mergerByAnnotation.getClass().getName(), itemName, valueClass, definition.getMergerIdentifier());
            return mergerByAnnotation;
        }

        // try to find merger based on class and supertypes (from custom mergers)
        if (valueClass != null) {
            ItemMerger mergerByType = findMergerByType(valueClass, originMarker);
            if (mergerByType != null) {
                LOGGER.trace(
                        "Type-specific merger for {} (type {}) was found: {}",
                        definition.getItemName(), valueClass, mergerByType);
                return mergerByType;
            }
        }

        // try to search for merger annotations in parent definitions
        return findMergerByAnnotationRecursively(definition, originMarker);
    }

    private ItemMerger findMergerByAnnotationRecursively(Definition def, OriginMarker originMarker) {
        ComplexTypeDefinition ctd = null;
        if (def instanceof ComplexTypeDefinition c) {
            ctd = c;
        } else if (def instanceof PrismContainerDefinition<?> pcd) {
            ctd = pcd.getComplexTypeDefinition();
        }

        if (ctd == null) {
            return null;
        }

        ItemMerger merger = findMergerByAnnotation(ctd, originMarker);
        if (merger != null) {
            return merger;
        }

        QName superType = ctd.getSuperType();
        if (superType == null) {
            return null;
        }

        SchemaRegistry registry = def.getSchemaRegistry();
        ctd = registry.findComplexTypeDefinitionByType(superType);

        return findMergerByAnnotationRecursively(ctd, originMarker);
    }

    private ItemMerger findMergerByAnnotation(Definition def, OriginMarker originMarker) {
        Class<?> valueClass = def.getTypeClass();

        ItemName itemName = def instanceof ItemDefinition<?> id ? id.getItemName() : null;

        // try to use a:naturalKey annotation
        List<QName> identifiers = def.getNaturalKeyConstituents();
        if (identifiers != null && !identifiers.isEmpty()) {
            NaturalKey key = NaturalKeyImpl.of(identifiers.toArray(new QName[0]));

            LOGGER.trace("Using generic item merger for {} (value class {}) with key {}", itemName, valueClass, key);
            return new GenericItemMerger(originMarker, key);
        }

        // try to use a:merger annotation (merger identifier for custom mergers)
        String customMerger = def.getMergerIdentifier();
        TypedMergerSupplier typedSupplier =
                customMerger != null ? identifierSpecificMergers.get(customMerger) : null;

        if (typedSupplier != null) {
            ItemMerger merger = typedSupplier.mergerFunction().apply(originMarker);
            LOGGER.trace("Using custom merger for {} (value class {}) with identifier {}", itemName, valueClass, merger.getClass());
            return merger;
        }

        if (customMerger != null) {
            throw new SystemException(String.format("Merger with identifier %s was not found", customMerger));
        }

        return null;
    }

    private ItemMerger findMergerByType(Class<?> valueClass, OriginMarker originMarker) {
        Map.Entry<Class<?>, Function<OriginMarker, ItemMerger>> entryFound = null;
        for (Map.Entry<Class<?>, Function<OriginMarker, ItemMerger>> entry : typeSpecificMergers.entrySet()) {
            if (entry.getKey().isAssignableFrom(valueClass)) {
                if (entryFound == null) {
                    entryFound = entry;
                } else {
                    // we're looking for the most concrete supplier
                    if (entryFound.getKey().isAssignableFrom(entry.getKey())) {
                        entryFound = entry;
                    }
                }
            }
        }

        return entryFound != null ? entryFound.getValue().apply(originMarker) : null;
    }
}
