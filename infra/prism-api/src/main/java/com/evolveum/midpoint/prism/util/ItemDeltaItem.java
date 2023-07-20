/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.util;

import static com.evolveum.midpoint.prism.path.ItemPath.CompareResult;
import static com.evolveum.midpoint.prism.path.ItemPath.EMPTY_PATH;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.util.MiscUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * A class defining old item state (before change), delta (change) and new item state (after change). This is a useful
 * class used to describe how the item has changed or is going to be changed without the need to re-apply the delta
 * several times. The delta can be applied once, and then all the rest of the code will have all the data
 * available. This is mostly just a convenience class that groups those three things together.
 * There is only a very little logic on top of that.
 *
 * @param <V> type of value that the item holds
 * @param <D> type of definition of the item
 *
 * @author Radovan Semancik
 */
public class ItemDeltaItem<V extends PrismValue, D extends ItemDefinition<?>> implements AbstractItemDeltaItem<D> {

    /** The "before" state. Null if the item did not exist. */
    @Nullable private final Item<V,D> itemOld;

    /** The delta, if any. It is perfectly legal to have no delta here. */
    @Nullable private final ItemDelta<V,D> delta;

    /** The "after" state. Null if the item did not exist. Not final, as it is manipulated by {@link #recompute()}. */
    @Nullable private Item<V,D> itemNew;

    /**
     * We need explicit definition, because source may be completely null.
     * No item, no delta, nothing. In that case we won't be able to crete properly-typed
     * variable from the source.
     */
    @NotNull private final D definition;

    /** The current path (of embedding objects) at which we are, after application of all the {@link #findIdi(ItemPath)} calls. */
    @NotNull private final ItemPath resolvePath;

    /**
     * Residual path is a temporary solution to {@link Structured} attributes in 3.x and 4.x.
     * It points to a sub-property value inside a structured property present in {@link #itemOld},
     * {@link #itemNew}, or {@link #delta}. I.e. these values are {@link Structured} ones, so the residual path
     * should be still applied to them. See e.g. `TestExpressionUtil.testResolvePathPolyStringOdoNorm`.
     *
     * It should disappear in 5.x.
     */
    private final ItemPath residualPath;

    /**
     * The deltas in sub-items. E.g. if this object represents "ContainerDeltaContainer"
     * this property contains property deltas that may exist inside the container.
     */
    private final Collection<? extends ItemDelta<?,?>> subItemDeltas;

    /** For internal use (e.g., cloning); we do not do any checks here. */
    protected ItemDeltaItem(
            @Nullable Item<V, D> itemOld,
            @Nullable ItemDelta<V, D> delta,
            @Nullable Item<V, D> itemNew,
            @NotNull D definition,
            @NotNull ItemPath resolvePath,
            @Nullable ItemPath residualPath,
            @Nullable Collection<? extends ItemDelta<?, ?>> subItemDeltas) {
        this.itemOld = itemOld;
        this.delta = delta;
        this.itemNew = itemNew;
        this.definition = definition;
        this.resolvePath = resolvePath;
        this.residualPath = residualPath;
        this.subItemDeltas = subItemDeltas;
    }

    /**
     * For {@link ObjectDeltaObject}.
     */
    protected ItemDeltaItem(@NotNull D definition) {
        this.itemOld = null;
        this.delta = null;
        this.definition = Objects.requireNonNull(definition, "No definition");
        this.resolvePath = EMPTY_PATH;
        this.residualPath = null;
        this.subItemDeltas = null;
    }

    public ItemDeltaItem(
            @Nullable Item<V,D> itemOld,
            @Nullable ItemDelta<V,D> delta,
            @Nullable Item<V,D> itemNew,
            @Nullable D explicitDefinition) {
        validate(itemOld, "itemOld");
        validate(delta);
        validate(itemNew, "itemNew");
        this.itemOld = itemOld;
        this.delta = delta;
        this.itemNew = itemNew;
        this.definition = determineDefinition(itemOld, delta, itemNew, explicitDefinition);
        this.resolvePath = EMPTY_PATH;
        this.residualPath = null;
        this.subItemDeltas = null;
    }

    public static ItemDeltaItem<?, ?> forUnchanged(@NotNull Item<?, ?> item) {
        return new ItemDeltaItem<>(item);
    }

    /** Presumably for "value creation" delta. The object must be recomputed after returning from the method. */
    public static <V extends PrismValue, D extends ItemDefinition<?>> ItemDeltaItem<V, D> forDelta(
            @NotNull ItemDelta<V, D> delta) {
        return new ItemDeltaItem<>(null, delta, null, delta.getDefinition());
    }

    @SuppressWarnings("DuplicatedCode")
    protected static @NotNull <V extends PrismValue, D extends ItemDefinition<?>> D determineDefinition(
            Item<V, D> itemOld, ItemDelta<V, D> delta, Item<V, D> itemNew, D explicitDefinition) {
        if (explicitDefinition != null) {
            return explicitDefinition;
        }
        if (itemNew != null && itemNew.getDefinition() != null) {
            return itemNew.getDefinition();
        }
        if (itemOld != null && itemOld.getDefinition() != null) {
            return itemOld.getDefinition();
        }
        if (delta != null && delta.getDefinition() != null) {
            return delta.getDefinition();
        }
        throw new IllegalArgumentException(
                "Cannot determine definition from content for IDI %s + %s -> %s".formatted(itemOld, delta, itemNew));
    }

    public ItemDeltaItem(@NotNull Item<V,D> item) {
        this.itemOld = item;
        this.itemNew = item;
        validate(itemOld, "item");
        this.delta = null;
        this.definition = Objects.requireNonNull(item.getDefinition(), "No definition in item, cannot create IDI");
        this.resolvePath = EMPTY_PATH;
        this.residualPath = null;
        this.subItemDeltas = null;
    }

    public ItemDeltaItem(@Nullable Item<V,D> item, @NotNull D definition) {
        this.itemOld = item;
        this.itemNew = item;
        validate(itemOld, "item");
        this.delta = null;
        this.definition = Objects.requireNonNull(definition, "No definition in item, cannot create IDI");
        this.resolvePath = EMPTY_PATH;
        this.residualPath = null;
        this.subItemDeltas = null;
    }

    public ItemDeltaItem(
            @Nullable Item<V,D> item,
            @NotNull D definition,
            @NotNull ItemPath resolvePath,
            @Nullable Collection<? extends ItemDelta<?, ?>> subItemDeltas) {
        this.itemOld = item;
        this.itemNew = item;
        validate(itemOld, "item");
        this.delta = null;
        this.definition = Objects.requireNonNull(definition, "No definition in item, cannot create IDI");
        this.resolvePath = resolvePath;
        this.residualPath = null;
        this.subItemDeltas = subItemDeltas;
    }

    public @Nullable Item<V,D> getItemOld() {
        return itemOld;
    }

    public @Nullable ItemDelta<V,D> getDelta() {
        return delta;
    }

    /**
     * Returns new item that is a result of delta application. May return null if there is no
     * new item.
     *
     * WARNING: Output of this method should be used for preview only.
     * It should NOT be placed into prism structures. Not even cloned.
     * This method may return dummy items or similar items that are not usable.
     * Values in the items should be OK, but they may need cloning.
     */
    public @Nullable Item<V,D> getItemNew() {
        return itemNew;
    }

    public Item<V,D> getAnyItem() {
        if (itemOld != null) {
            return itemOld;
        }
        return itemNew;
    }

    public ItemPath getResidualPath() {
        return residualPath;
    }

    public @NotNull ItemPath getResolvePath() {
        return resolvePath;
    }

    public Collection<? extends ItemDelta<?,?>> getSubItemDeltas() {
        return subItemDeltas;
    }

    @Override
    public boolean isNull() {
        return itemOld == null && itemNew == null && delta == null && subItemDeltas == null; // FIXME !null
    }

    public QName getElementName() {
        Item<V,D> anyItem = getAnyItem();
        if (anyItem != null) {
            return anyItem.getElementName();
        }
        if (delta != null) {
            return delta.getElementName();
        }
        return null;
    }

    @Override
    public @NotNull D getDefinition() {
        return definition;
    }

    @Override
    public void recompute() throws SchemaException {
        if (delta == null && (subItemDeltas == null || subItemDeltas.isEmpty())) {
            itemNew = itemOld;
            return;
        }
        itemNew = null;
        if (delta != null) {
            itemNew = delta.getItemNewMatchingPath(itemOld);
        }
        if (subItemDeltas != null && !subItemDeltas.isEmpty()) {
            // TODO fix these ugly hacks
            if (itemNew != null && itemOld != null && itemOld.getPath().size() == 1) {
                // The path for itemNew will be OK in this case.
            } else {
                // We need to have itemNew with the correct path. Currently, the only way how to ensure this is to
                // create a new one.
                Item<V,D> dummyItemNew =
                        definition.getPrismContext().itemFactory().createDummyItem(itemOld, definition, resolvePath);
                if (itemNew != null) {
                    dummyItemNew.addAll(
                            itemNew.getClonedValues());
                }
                itemNew = dummyItemNew;
            }
            for (ItemDelta<?,?> subItemDelta : subItemDeltas) {
                subItemDelta.applyTo(itemNew);
            }
        }
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> ItemDeltaItem<IV,ID> findIdi(
            @NotNull ItemPath path, @Nullable DefinitionResolver<D, ID> additionalDefinitionResolver) throws SchemaException {
        if (path.isEmpty()) {
            //noinspection unchecked
            return (ItemDeltaItem<IV,ID>) this;
        }

        Item<IV,ID> subItemOld;
        ItemPath subResidualPath = null;
        ItemPath newResolvePath = resolvePath.append(path);
        if (itemOld != null) {
            PartiallyResolvedItem<IV,ID> partialItemOld = itemOld.findPartial(path);
            if (partialItemOld != null) {
                subItemOld = partialItemOld.getItem();
                subResidualPath = partialItemOld.getResidualPath();
            } else {
                subItemOld = null;
            }
        } else {
            subItemOld = null;
        }

        Item<IV,ID> subItemNew;
        if (itemNew != null) {
            PartiallyResolvedItem<IV,ID> partialItemNew = itemNew.findPartial(path);
            if (partialItemNew != null) {
                subItemNew = partialItemNew.getItem();
                if (subResidualPath == null) {
                    subResidualPath = partialItemNew.getResidualPath();
                }
            } else {
                subItemNew = null;
            }
        } else {
            subItemNew = null;
        }

        ItemDelta<IV,ID> subDelta = null;
        if (delta != null) {
            if (delta instanceof ContainerDelta<?>) {
                //noinspection unchecked
                subDelta = (ItemDelta<IV,ID>) delta.getSubDelta(path);
            } else {
                CompareResult compareComplex = delta.getPath().compareComplex(newResolvePath);
                if (compareComplex == CompareResult.EQUIVALENT || compareComplex == CompareResult.SUBPATH) {
                    //noinspection unchecked
                    subDelta = (ItemDelta<IV,ID>) delta;
                }
            }
        }

        ID subDefinition;
        if (definition instanceof PrismContainerDefinition<?>) {
            subDefinition = ((PrismContainerDefinition<?>) definition).findItemDefinition(path);
        } else {
            throw new IllegalArgumentException("Attempt to resolve definition on non-container " + definition + " in " +this);
        }
        if (subDefinition == null && subItemNew != null) {
            subDefinition = subItemNew.getDefinition();
        }
        if (subDefinition == null && subDelta != null) {
            subDefinition = subDelta.getDefinition();
        }
        if (subDefinition == null && subItemOld != null) {
            subDefinition = subItemOld.getDefinition();
        }
        if (subDefinition == null && additionalDefinitionResolver != null) {
            subDefinition = additionalDefinitionResolver.resolve(definition, path);
        }
        if (subDefinition == null) {
            throw new SchemaException("No definition for item " + path + " in " + this);
        }

        Item<IV, ID> subItemAny = MiscUtil.getFirstNonNull(subItemOld, subItemNew);
        Collection<ItemDelta<?,?>> subSubItemDeltas = null;
        if (subItemDeltas != null) {
            subSubItemDeltas = new ArrayList<>();
            if (subItemAny != null) {
                for (ItemDelta<?, ?> subItemDelta : subItemDeltas) {
                    CompareResult compareComplex = subItemDelta.getPath().compareComplex(subItemAny.getPath());
                    if (compareComplex == CompareResult.EQUIVALENT || compareComplex == CompareResult.SUBPATH) {
                        subSubItemDeltas.add(subItemDelta);
                    }
                }
            }
            if (!subSubItemDeltas.isEmpty()) {
                // Niceness optimization
                if (subDelta == null && subSubItemDeltas.size() == 1) {
                    ItemDelta<?,?> subSubItemDelta = subSubItemDeltas.iterator().next();
                    if (subSubItemDelta.isApplicableTo(subItemAny)) {
                        //noinspection unchecked
                        subDelta = (ItemDelta<IV,ID>) subSubItemDelta;
                        subSubItemDeltas = null;
                    }
                }
            }
        }

        return new ItemDeltaItem<>(
                subItemOld, subDelta, subItemNew, subDefinition, newResolvePath, subResidualPath, subSubItemDeltas);
    }

    public PrismValueDeltaSetTriple<V> toDeltaSetTriple() throws SchemaException {
        return ItemDeltaUtil.toDeltaSetTriple(itemOld, delta);
    }

    @Override
    public boolean isContainer() {
        Item<V,D> item = getAnyItem();
        if (item != null) {
            return item instanceof PrismContainer<?>;
        }
        if (getDelta() != null) {
            return getDelta() instanceof ContainerDelta<?>;
        }
        return false;
    }

    @Override
    public boolean isProperty() {
        Item<V,D> item = getAnyItem();
        if (item != null) {
            return item instanceof PrismProperty<?>;
        }
        if (getDelta() != null) {
            return getDelta() instanceof PropertyDelta<?>;
        }
        return false;
    }

    @Override
    public boolean isStructuredProperty() {
        if (!isProperty()) {
            return false;
        }
        PrismProperty<?> property = (PrismProperty<?>) getAnyItem();
        Object realValue = property.getAnyRealValue();
        if (realValue != null) {
            return realValue instanceof Structured;
        }
        PropertyDelta<?> delta = (PropertyDelta<?>) getDelta();
        //noinspection DataFlowIssue
        realValue = delta.getAnyRealValue();
        if (realValue != null) {
            return realValue instanceof Structured;
        }
        return false;
    }

    /**
     * Assumes that this IDI represents structured property
     */
    public <X> ItemDeltaItem<PrismPropertyValue<X>,PrismPropertyDefinition<X>> resolveStructuredProperty(
            ItemPath resolvePath, PrismPropertyDefinition<X> outputDefinition) {
        //noinspection unchecked
        ItemDeltaItem<PrismPropertyValue<Structured>, PrismPropertyDefinition<Structured>> thisIdi =
                (ItemDeltaItem<PrismPropertyValue<Structured>, PrismPropertyDefinition<Structured>>) this;
        return new ItemDeltaItem<>(
                resolveStructuredPropertyItem((PrismProperty<Structured>) thisIdi.getItemOld(), resolvePath, outputDefinition),
                resolveStructuredPropertyDelta((PropertyDelta<Structured>) thisIdi.getDelta(), resolvePath, outputDefinition),
                resolveStructuredPropertyItem((PrismProperty<Structured>) thisIdi.getItemNew(), resolvePath, outputDefinition),
                outputDefinition);
    }

    private <X> PrismProperty<X> resolveStructuredPropertyItem(
            PrismProperty<Structured> sourceProperty, ItemPath resolvePath, PrismPropertyDefinition<X> outputDefinition) {
        if (sourceProperty == null) {
            return null;
        }
        PrismProperty<X> outputProperty = outputDefinition.instantiate();
        for (Structured sourceRealValue: sourceProperty.getRealValues()) {
            //noinspection unchecked
            X outputRealValue = (X) sourceRealValue.resolve(resolvePath);
            outputProperty.addRealValue(outputRealValue);
        }
        return outputProperty;
    }

    private <X> PropertyDelta<X> resolveStructuredPropertyDelta(
            PropertyDelta<Structured> sourceDelta, ItemPath resolvePath, PrismPropertyDefinition<X> outputDefinition) {
        if (sourceDelta == null) {
            return null;
        }
        // Path in output delta has no meaning anyway. The delta will never be applied, as it references sub-property object.
        PropertyDelta<X> outputDelta = outputDefinition.createEmptyDelta(ItemPath.EMPTY_PATH);
        Collection<PrismPropertyValue<X>> outputValuesToAdd = resolveStructuredDeltaSet(sourceDelta.getValuesToAdd(), resolvePath);
        if (outputValuesToAdd != null) {
            outputDelta.addValuesToAdd(outputValuesToAdd);
        }
        Collection<PrismPropertyValue<X>> outputValuesToDelete = resolveStructuredDeltaSet(sourceDelta.getValuesToDelete(), resolvePath);
        if (outputValuesToDelete != null) {
            outputDelta.addValuesToDelete(outputValuesToDelete);
        }
        Collection<PrismPropertyValue<X>> outputValuesToReplace = resolveStructuredDeltaSet(sourceDelta.getValuesToReplace(), resolvePath);
        if (outputValuesToReplace != null) {
            outputDelta.setValuesToReplace(outputValuesToReplace);
        }
        return outputDelta;
    }

    private <X> Collection<PrismPropertyValue<X>> resolveStructuredDeltaSet(
            Collection<PrismPropertyValue<Structured>> set, ItemPath resolvePath) {
        if (set == null) {
            return null;
        }
        Collection<PrismPropertyValue<X>> outputSet = new ArrayList<>(set.size());
        for (PrismPropertyValue<Structured> structuredPVal: set) {
            Structured structured = structuredPVal.getValue();
            //noinspection unchecked
            X outputRval = (X) structured.resolve(resolvePath);
            outputSet.add(PrismContext.get().itemFactory().createPropertyValue(outputRval));
        }
        return outputSet;
    }

    public void applyDefinition(D def, boolean force) throws SchemaException {
        if (itemNew != null) {
            itemNew.applyDefinition(def, force);
        }
        if (itemOld != null) {
            itemOld.applyDefinition(def, force);
        }
        if (delta != null) {
            delta.applyDefinition(def, force);
        }
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public ItemDeltaItem<V,D> clone() {
        return new ItemDeltaItem<>(
                itemOld != null ? itemOld.clone() : null,
                delta != null ? delta.clone() : null,
                itemNew != null ? itemNew.clone() : null,
                definition,
                resolvePath,
                residualPath,
                subItemDeltas != null ? ItemDeltaCollectionsUtil.cloneCollection(this.subItemDeltas) : null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemDeltaItem<?, ?> that = (ItemDeltaItem<?, ?>) o;
        return Objects.equals(itemOld, that.itemOld)
                && Objects.equals(delta, that.delta)
                && Objects.equals(itemNew, that.itemNew)
                && Objects.equals(definition, that.definition)
                && Objects.equals(resolvePath, that.resolvePath)
                && Objects.equals(residualPath, that.residualPath)
                && Objects.equals(subItemDeltas, that.subItemDeltas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemOld, delta, itemNew, definition, resolvePath, residualPath, subItemDeltas);
    }

    @Override
    public String toString() {
        return "IDI(old=" + itemOld + ", delta=" + delta + ", new=" + itemNew + ")";
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.debugDumpLabelLn(sb, "ItemDeltaItem", indent);
        DebugUtil.debugDumpWithLabelLn(sb, "itemOld", itemOld, indent + 1);
        DebugUtil.debugDumpWithLabelLn(sb, "delta", delta, indent + 1);
        DebugUtil.debugDumpWithLabelLn(sb, "itemNew", itemNew, indent + 1);
        DebugUtil.debugDumpWithLabelLn(sb, "subItemDeltas", subItemDeltas, indent + 1);
        DebugUtil.debugDumpWithLabelLn(sb, "definition", definition, indent + 1);
        DebugUtil.debugDumpWithLabelToStringLn(sb, "resolvePath", resolvePath, indent + 1);
        DebugUtil.debugDumpWithLabelToString(sb, "residualPath", residualPath, indent + 1);
        return sb.toString();
    }

    private V getSingleValue(Item<V, D> item) {
        if (item == null || item.isEmpty()) {
            return null;
        } else if (item.size() == 1) {
            return item.getAnyValue();
        } else {
            throw new IllegalStateException("Multiple values where single one was expected: " + item);
        }
    }

    public V getSingleValue(boolean old) {
        return getSingleValue(old ? itemOld : itemNew);
    }

    private void validate(Item<V, D> item, String desc) {
        if (item != null && item.getDefinition() == null) {
            throw new IllegalArgumentException("Attempt to set " + desc + " without definition");
        }
    }

    private void validate(ItemDelta<V, D> delta) {
        if (delta != null && delta.getDefinition() == null) {
            throw new IllegalArgumentException("Attempt to set delta without definition");
        }
    }
}
