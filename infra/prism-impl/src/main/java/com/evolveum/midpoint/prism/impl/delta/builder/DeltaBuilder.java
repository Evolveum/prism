/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.delta.builder;

import java.util.*;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.util.MiscUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.*;
import com.evolveum.midpoint.prism.delta.builder.S_ItemEntry;
import com.evolveum.midpoint.prism.delta.builder.S_MaybeAdd;
import com.evolveum.midpoint.prism.delta.builder.S_MaybeDelete;
import com.evolveum.midpoint.prism.delta.builder.S_ValuesEntry;
import com.evolveum.midpoint.prism.impl.PrismPropertyValueImpl;
import com.evolveum.midpoint.prism.impl.delta.ContainerDeltaImpl;
import com.evolveum.midpoint.prism.impl.delta.PropertyDeltaImpl;
import com.evolveum.midpoint.prism.impl.delta.ReferenceDeltaImpl;
import com.evolveum.midpoint.prism.impl.query.builder.QueryBuilder;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.util.DefinitionUtil;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Grammar
 *
 * ----
 * ObjectDelta ::= (ItemDelta)* ( 'OBJECT-DELTA(oid)' | 'ITEM-DELTA' | 'ITEM-DELTAS' )
 *
 * ItemDelta ::= 'ITEM(...)' ( ( 'ADD-VALUES(...)' 'DELETE-VALUES(...)'? ) |
 * ( 'DELETE-VALUES(...)' 'ADD-VALUES(...)'? ) | 'REPLACE-VALUES(...)' )
 * ----
 *
 * When combining DELETE and ADD prefer using DELETE first to match the actual behavior.
 */
@Experimental
public class DeltaBuilder<C extends Containerable>
        implements S_ItemEntry, S_MaybeDelete, S_MaybeAdd, S_ValuesEntry {

    private final Class<C> objectClass;
    private final ComplexTypeDefinition containerCTD;
    private final PrismContext prismContext;

    /** Should we skip idempotent item deltas? */
    @Experimental private final boolean optimizing;

    /** Useful if one wants to create definition-ful deltas (e.g. for resource objects). */
    @Nullable private final ItemDefinitionResolver itemDefinitionResolver;

    // BEWARE - although these are final, their content may (and does) vary. Not much clean.
    private final List<ItemDelta<?, ?>> deltas;
    private final ItemDelta currentDelta;

    public DeltaBuilder(Class<C> objectClass, PrismContext prismContext, ItemDefinitionResolver itemDefinitionResolver)
            throws SchemaException {
        this.objectClass = objectClass;
        this.prismContext = prismContext;
        this.itemDefinitionResolver = itemDefinitionResolver;
        containerCTD = prismContext.getSchemaRegistry().findComplexTypeDefinitionByCompileTimeClass(this.objectClass);
        if (containerCTD == null) {
            throw new SchemaException("Couldn't find definition for complex type " + this.objectClass);
        }
        deltas = new ArrayList<>();
        currentDelta = null;
        optimizing = false;
    }

    private DeltaBuilder(
            Class<C> objectClass,
            ComplexTypeDefinition containerCTD,
            PrismContext prismContext,
            boolean optimizing,
            @Nullable ItemDefinitionResolver itemDefinitionResolver,
            List<ItemDelta<?, ?>> deltas,
            ItemDelta currentDelta) {
        this.objectClass = objectClass;
        this.containerCTD = containerCTD;
        this.prismContext = prismContext;
        this.optimizing = optimizing;
        this.itemDefinitionResolver = itemDefinitionResolver;
        this.deltas = deltas;
        this.currentDelta = currentDelta;
    }

    public Class<C> getObjectClass() {
        return objectClass;
    }

    public PrismContext getPrismContext() {
        return prismContext;
    }

    @Override
    public S_ItemEntry optimizing() {
        return new DeltaBuilder<>(
                objectClass, containerCTD, prismContext, true, itemDefinitionResolver, deltas, currentDelta);
    }

    @Override
    public S_ValuesEntry item(QName... names) {
        return item(ItemPath.create(names));
    }

    @Override
    public S_ValuesEntry item(Object... namesOrIds) {
        return item(ItemPath.create(namesOrIds));
    }

    @Override
    public S_ValuesEntry item(ItemPath path) {
        ItemDefinition<?> definition = findItemDefinition(path, ItemDefinition.class);
        if (definition == null) {
            throw new IllegalArgumentException("Undefined or dynamic path: " + path + " in: " + containerCTD);
        }
        return item(path, definition);
    }

    /**
     * See {@link QueryBuilder#findItemDefinition(Class, ItemPath, Class)}.
     *
     * For historic reasons, we allow missing definitions here. ({@link #property(ItemPath)} supports them.)
     */
    private <ID extends ItemDefinition<?>> @Nullable ID findItemDefinition(
            @NotNull ItemPath itemPath,
            @NotNull Class<ID> type) {
        if (itemDefinitionResolver != null) {
            ID definition = DefinitionUtil.findItemDefinition(itemDefinitionResolver, objectClass, itemPath, type);
            if (definition != null) {
                return definition;
            }
        }
        return containerCTD.findItemDefinition(itemPath, type);
    }

    @Override
    public S_ValuesEntry item(ItemPath path, ItemDefinition definition) {
        ItemDelta newDelta;
        if (definition instanceof PrismPropertyDefinition) {
            newDelta = new PropertyDeltaImpl<>(path, (PrismPropertyDefinition<?>) definition, prismContext);
        } else if (definition instanceof PrismContainerDefinition) {
            newDelta = new ContainerDeltaImpl<>(path, (PrismContainerDefinition<?>) definition, prismContext);
        } else if (definition instanceof PrismReferenceDefinition) {
            newDelta = new ReferenceDeltaImpl(path, (PrismReferenceDefinition) definition, prismContext);
        } else {
            throw new IllegalStateException("Unsupported definition type: " + definition);
        }
        List<ItemDelta<?, ?>> newDeltas = deltas;
        if (shouldApplyCurrent()) {
            newDeltas.add(currentDelta);
        }
        return new DeltaBuilder<>(
                objectClass, containerCTD, prismContext, optimizing, itemDefinitionResolver, newDeltas, newDelta);
    }

    private boolean shouldApplyCurrent() {
        if (currentDelta == null) {
            return false;
        }
        if (!optimizing) {
            return true;
        }
        if (currentDelta.isEmpty()) {
            return false;
        }
        Collection<?> estimatedOldValues = currentDelta.getEstimatedOldValues();
        Collection<?> valuesToAdd = currentDelta.getValuesToAdd();
        Collection<?> valuesToDelete = currentDelta.getValuesToDelete();
        Collection<?> valuesToReplace = currentDelta.getValuesToReplace();
        if (estimatedOldValues == null) {
            return true; // We cannot tell anything about idempotency of this delta, as old values are unknown
        }
        if (valuesToReplace != null) {
            return !MiscUtil.unorderedCollectionEquals(estimatedOldValues, valuesToReplace);
        } else {
            return (valuesToAdd != null && !CollectionUtils.containsAll(estimatedOldValues, valuesToAdd))
                    || (valuesToDelete != null && CollectionUtils.containsAny(estimatedOldValues, valuesToDelete));
        }
    }

    @Override
    public S_ValuesEntry property(QName... names) {
        return property(ItemPath.create(names));
    }

    @Override
    public S_ValuesEntry property(Object... namesOrIds) {
        return property(ItemPath.create(namesOrIds));
    }

    @Override
    public S_ValuesEntry property(ItemPath path) {
        PrismPropertyDefinition<?> definition = findItemDefinition(path, PrismPropertyDefinition.class);
        return property(path, definition);
    }

    @Override
    public <T> S_ValuesEntry property(ItemPath path, PrismPropertyDefinition<T> definition) {
        PropertyDelta<T> newDelta = new PropertyDeltaImpl<>(path, definition, prismContext);
        List<ItemDelta<?, ?>> newDeltas = deltas;
        if (currentDelta != null) {
            newDeltas.add(currentDelta);
        }
        return new DeltaBuilder<>(
                objectClass, containerCTD, prismContext, optimizing, itemDefinitionResolver, newDeltas, newDelta);
    }

    // TODO fix this after ObjectDelta is changed to accept Containerable
    @Override
    public ObjectDelta asObjectDelta(String oid) {
        return prismContext.deltaFactory().object().createModifyDelta(oid, getAllDeltas(), (Class) objectClass);
    }

    @Override
    public List<ObjectDelta<?>> asObjectDeltas(String oid) {
        return Collections.singletonList(
                prismContext.deltaFactory().object().createModifyDelta(oid, getAllDeltas(), (Class) objectClass));
    }

    @Override
    public ItemDelta asItemDelta() {
        List<ItemDelta<?, ?>> allDeltas = getAllDeltas();
        if (allDeltas.size() > 1) {
            throw new IllegalStateException("Too many deltas to fit into item delta: " + allDeltas.size());
        } else if (allDeltas.size() == 1) {
            return allDeltas.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<ItemDelta<?, ?>> asItemDeltas() {
        return getAllDeltas();
    }

    private List<ItemDelta<?, ?>> getAllDeltas() {
        if (shouldApplyCurrent()) {
            deltas.add(currentDelta);
        }
        return deltas;
    }

    @Override
    public S_MaybeDelete add(Object... realValues) {
        return addRealValues(Arrays.asList(realValues));
    }

    @Override
    public S_MaybeDelete addRealValues(Collection<?> realValues) {
        for (Object v : realValues) {
            if (v != null) {
                currentDelta.addValueToAdd(toPrismValue(currentDelta, v));
            }
        }
        return this;
    }

    @Override
    public S_MaybeDelete add(PrismValue... values) {
        for (PrismValue v : values) {
            if (v != null) {
                currentDelta.addValueToAdd(v);
            }
        }
        return this;
    }

    @Override
    public S_MaybeDelete add(Collection<? extends PrismValue> values) {
        for (PrismValue v : values) {
            if (v != null) {
                currentDelta.addValueToAdd(v);
            }
        }
        return this;
    }

    @Override
    public S_ValuesEntry old(Object... realValues) {
        return oldRealValues(Arrays.asList(realValues));
    }

    @Override
    public S_ValuesEntry oldRealValues(Collection<?> realValues) {
        setOldValuesIfNeeded();
        for (Object v : realValues) {
            if (v != null) {
                currentDelta.addEstimatedOldValue(toPrismValue(currentDelta, v));
            }
        }
        return this;
    }

    private void setOldValuesIfNeeded() {
        if (currentDelta.getEstimatedOldValues() == null) {
            //noinspection unchecked
            currentDelta.setEstimatedOldValues(new ArrayList<>()); // ugly but necessary
        }
    }

    @Override
    public <T> S_ValuesEntry oldRealValue(T realValue) {
        setOldValuesIfNeeded();
        if (realValue != null) {
            currentDelta.addEstimatedOldValue(toPrismValue(currentDelta, realValue));
        }
        return this;
    }

    @Override
    public S_ValuesEntry old(PrismValue... values) {
        setOldValuesIfNeeded();
        for (PrismValue v : values) {
            if (v != null) {
                currentDelta.addEstimatedOldValue(v);
            }
        }
        return this;
    }

    @Override
    public S_ValuesEntry old(Collection<? extends PrismValue> values) {
        setOldValuesIfNeeded();
        for (PrismValue v : values) {
            if (v != null) {
                currentDelta.addEstimatedOldValue(v);
            }
        }
        return this;
    }

    @Override
    public S_MaybeAdd delete(Object... realValues) {
        return deleteRealValues(Arrays.asList(realValues));
    }

    @Override
    public S_MaybeAdd deleteRealValues(Collection<?> realValues) {
        for (Object v : realValues) {
            if (v != null) {
                currentDelta.addValueToDelete(toPrismValue(currentDelta, v));
            }
        }
        return this;
    }

//    protected void checkNullMisuse(Object[] realValues) {
//        if (realValues.length == 1 && realValues[0] == null) {
//            throw new IllegalArgumentException("NULL value should be represented as no value, not as 'null'");
//        }
//    }

    @Override
    public S_MaybeAdd delete(PrismValue... values) {
        for (PrismValue v : values) {
            if (v != null) {
                currentDelta.addValueToDelete(v);
            }
        }
        return this;
    }

    @Override
    public S_MaybeAdd delete(Collection<? extends PrismValue> values) {
        for (PrismValue v : values) {
            if (v != null) {
                currentDelta.addValueToDelete(v);
            }
        }
        return this;
    }

    @Override
    public S_ItemEntry replace(Object... realValues) {
        return replaceRealValues(Arrays.asList(realValues));
    }

    @Override
    public S_ItemEntry replaceRealValues(Collection<?> realValues) {
        List<PrismValue> prismValues = new ArrayList<>();
        for (Object v : realValues) {
            if (v != null) {
                prismValues.add(toPrismValue(currentDelta, v));
            }
        }
        currentDelta.setValuesToReplace(prismValues);
        return this;
    }

    @Override
    public S_ItemEntry replace(Collection<? extends PrismValue> values) {
        List<PrismValue> prismValues = new ArrayList<>();
        for (PrismValue v : values) {
            if (v != null) {
                prismValues.add(v);
            }
        }
        currentDelta.setValuesToReplace(prismValues);
        return this;
    }

    @Override
    public S_ItemEntry replace(PrismValue... values) {
        List<PrismValue> prismValues = new ArrayList<>();
        for (PrismValue v : values) {
            if (v != null) {
                prismValues.add(v);
            }
        }
        currentDelta.setValuesToReplace(prismValues);
        return this;
    }

    @Override
    public S_ItemEntry mod(PlusMinusZero plusMinusZero, Object... realValues) {
        return modRealValues(plusMinusZero, Arrays.asList(realValues));
    }

    @Override
    public S_ItemEntry modRealValues(PlusMinusZero plusMinusZero, Collection<?> realValues) {
        List<PrismValue> prismValues = new ArrayList<>();
        for (Object v : realValues) {
            if (v != null) {
                prismValues.add(toPrismValue(currentDelta, v));
            }
        }
        switch (plusMinusZero) {
            case PLUS:
                currentDelta.addValuesToAdd(prismValues);
                break;
            case MINUS:
                currentDelta.addValuesToDelete(prismValues);
                break;
            case ZERO:
                currentDelta.setValuesToReplace(prismValues);
                break;
        }
        return this;
    }

    @Override
    public S_ItemEntry mod(PlusMinusZero plusMinusZero, Collection<? extends PrismValue> values) {
        List<PrismValue> prismValues = new ArrayList<>();
        for (PrismValue v : values) {
            if (v != null) {
                prismValues.add(v);
            }
        }
        switch (plusMinusZero) {
            case PLUS:
                currentDelta.addValuesToAdd(prismValues);
                break;
            case MINUS:
                currentDelta.addValuesToDelete(prismValues);
                break;
            case ZERO:
                currentDelta.setValuesToReplace(prismValues);
                break;
        }
        return this;
    }

    @Override
    public S_ItemEntry mod(PlusMinusZero plusMinusZero, PrismValue... values) {
        List<PrismValue> prismValues = new ArrayList<>();
        for (PrismValue v : values) {
            if (v != null) {
                prismValues.add(v);
            }
        }
        switch (plusMinusZero) {
            case PLUS:
                currentDelta.addValuesToAdd(prismValues);
                break;
            case MINUS:
                currentDelta.addValuesToDelete(prismValues);
                break;
            case ZERO:
                currentDelta.setValuesToReplace(prismValues);
                break;
        }
        return this;
    }

    private PrismValue toPrismValue(ItemDelta<?, ?> currentDelta, @NotNull Object v) {
        if (currentDelta instanceof PropertyDelta<?>) {
            return new PrismPropertyValueImpl<>(v);
        } else if (currentDelta instanceof ContainerDelta<?>) {
            return ((Containerable) v).asPrismContainerValue();
        } else if (currentDelta instanceof ReferenceDelta) {
            if (v instanceof Referencable) {
                return ((Referencable) v).asReferenceValue();
            } else {
                throw new IllegalStateException("Expected Referencable, got: " + v);
            }
        } else {
            throw new IllegalStateException("Unsupported delta type: " + currentDelta);
        }
    }

}
