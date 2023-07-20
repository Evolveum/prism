/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.util.MiscUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.delta.ChangeType;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * A class defining old object state (before change), delta (change) and new object state (after change). This is a useful
 * class used to describe how the object has changed or is going to be changed without the need to re-apply the delta
 * several times. The delta can be applied once, and then all the rest of the code will have all the data
 * available. This is mostly just a convenience class that groups those three things together.
 * There is only a very little logic on top of that.
 *
 * @author Radovan Semancik
 */
public class ObjectDeltaObject<O extends Objectable>
        implements AbstractItemDeltaItem<PrismObjectDefinition<O>> {

    @Nullable private final PrismObject<O> oldObject;

    /**
     * Delta cannot be `final` because of {@link #update(ItemDelta)} method. That method is wicked anyway, and should be
     * perhaps replaced. But not now.
     */
    @Nullable private ObjectDelta<O> delta;
    @Nullable private PrismObject<O> newObject;
    @NotNull private final PrismObjectDefinition<O> definition;

    public ObjectDeltaObject(
            @Nullable PrismObject<O> oldObject,
            @Nullable ObjectDelta<O> delta,
            @Nullable PrismObject<O> newObject,
            @Nullable PrismObjectDefinition<O> explicitDefinition) {
        this.oldObject = oldObject;
        this.delta = delta;
        this.newObject = newObject;
        this.definition = determineDefinition(explicitDefinition, oldObject, newObject);
    }

    public static <O extends Objectable> ObjectDeltaObject<O> forUnchanged(@NotNull PrismObject<O> prismObject) {
        return new ObjectDeltaObject<>(prismObject, null, prismObject, prismObject.getDefinition());
    }

    @SuppressWarnings("DuplicatedCode")
    private static @NotNull <O extends Objectable> PrismObjectDefinition<O> determineDefinition(
            PrismObjectDefinition<O> explicitDefinition,
            PrismObject<O> oldObject,
            PrismObject<O> newObject) {
        if (explicitDefinition != null) {
            return explicitDefinition;
        }
        if (newObject != null && newObject.getDefinition() != null) {
            return newObject.getDefinition();
        }
        if (oldObject != null && oldObject.getDefinition() != null) {
            return oldObject.getDefinition();
        }
        throw new IllegalStateException("No definition");
    }

    public @Nullable PrismObject<O> getOldObject() {
        return oldObject;
    }

    public @NotNull PrismObject<O> getOldObjectRequired() {
        return MiscUtil.stateNonNull(
                getOldObject(),
                () -> "No old object in odo " + this);
    }

    public ObjectDelta<O> getObjectDelta() {
        return delta;
    }

    public @Nullable PrismObject<O> getNewObject() {
        return newObject;
    }

    public @NotNull PrismObject<O> getNewObjectRequired() {
        return MiscUtil.stateNonNull(
                getNewObject(),
                () -> "No new object in odo " + this);
    }

    // FIXME fragile!!! better don't use if you don't have to
    public void update(ItemDelta<?, ?> itemDelta) throws SchemaException {
        if (delta == null) {
            delta = getAnyObject().getPrismContext().deltaFactory().object()
                    .createModifyDelta(getAnyObject().getOid(), itemDelta, getAnyObject().getCompileTimeClass());
        } else {
            delta.swallow(itemDelta);
            itemDelta.applyTo(newObject);
        }
    }

    public PrismObject<O> getAnyObject() {
        if (newObject != null) {
            return newObject;
        }
        return oldObject;
    }

    public @NotNull PrismObject<O> getAnyObjectRequired() {
        return MiscUtil.stateNonNull(
                getAnyObject(),
                () -> "No object in odo " + this);
    }

    @Override
    public boolean isNull() {
        return oldObject == null && newObject == null && delta == null;
    }

    @Override
    public @NotNull PrismObjectDefinition<O> getDefinition() {
        return definition;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean isProperty() {
        return false;
    }

    @Override
    public boolean isStructuredProperty() {
        return false;
    }

    @Override
    public <IV extends PrismValue,ID extends ItemDefinition<?>> ItemDeltaItem<IV,ID> findIdi(
            @NotNull ItemPath path, @Nullable DefinitionResolver<PrismObjectDefinition<O>,ID> additionalDefinitionResolver)
            throws SchemaException {
        Item<IV,ID> subItemOld = null;
        ItemPath subResidualPath = null;
        if (oldObject != null) {
            PartiallyResolvedItem<IV,ID> partialOld = oldObject.findPartial(path);
            if (partialOld != null) {
                subItemOld = partialOld.getItem();
                subResidualPath = partialOld.getResidualPath();
            }
        }
        Item<IV,ID> subItemNew = null;
        if (newObject != null) {
            PartiallyResolvedItem<IV,ID> partialNew = newObject.findPartial(path);
            if (partialNew != null) {
                subItemNew = partialNew.getItem();
                if (subResidualPath == null) {
                    subResidualPath = partialNew.getResidualPath();
                }
            }
        }
        ItemDelta<IV,ID> itemDelta = null;
        Collection<? extends ItemDelta<?,?>> subSubItemDeltas = null;
        if (delta != null) {
            if (delta.getChangeType() == ChangeType.ADD) {
                PrismObject<O> objectToAdd = delta.getObjectToAdd();
                PartiallyResolvedItem<IV,ID> partialValue = objectToAdd.findPartial(path);
                if (partialValue != null && partialValue.getItem() != null) {
                    Item<IV,ID> item = partialValue.getItem();
                    itemDelta = item.createDelta();
                    itemDelta.addValuesToAdd(item.getClonedValues());
                } else {
                    // No item for this path, itemDelta will stay empty.
                }
            } else if (delta.getChangeType() == ChangeType.DELETE) {
                if (subItemOld != null) {
                    ItemPath subPath = subItemOld.getPath().remainder(path);
                    PartiallyResolvedItem<IV,ID> partialValue = subItemOld.findPartial(subPath);
                    if (partialValue != null && partialValue.getItem() != null) {
                        Item<IV,ID> item = partialValue.getItem();
                        itemDelta = item.createDelta();
                        itemDelta.addValuesToDelete(item.getClonedValues());
                    } else {
                        // No item for this path, itemDelta will stay empty.
                    }
                }
            } else if (delta.getChangeType() == ChangeType.MODIFY) {
                for (ItemDelta<?,?> modification: delta.getModifications()) {
                    ItemPath.CompareResult compareComplex = modification.getPath().compareComplex(path);
                    if (compareComplex == ItemPath.CompareResult.EQUIVALENT) {
                        if (itemDelta != null) {
                            throw new IllegalStateException(
                                    "Conflicting modification in delta " + delta + ": " + itemDelta + " and " + modification);
                        }
                        //noinspection unchecked
                        itemDelta = (ItemDelta<IV,ID>) modification;
                    } else if (compareComplex == ItemPath.CompareResult.SUPERPATH) {
                        if (subSubItemDeltas == null) {
                            subSubItemDeltas = new ArrayList<>();
                        }
                        //noinspection unchecked,rawtypes
                        ((Collection)subSubItemDeltas).add(modification);
                    } else if (compareComplex == ItemPath.CompareResult.SUBPATH) {
                        if (itemDelta != null) {
                            throw new IllegalStateException(
                                    "Conflicting modification in delta " + delta + ": " + itemDelta + " and " + modification);
                        }
                        //noinspection unchecked
                        itemDelta = (ItemDelta<IV,ID>) modification.getSubDelta(path.remainder(modification.getPath()));
                    }
                }
            }
        }
        ID subDefinition = definition.findItemDefinition(path);
        if (subDefinition == null) {
            // This may be a bit redundant, because IDI constructor does similar logic.
            // But we want to know the situation here, so we can provide better error message.
            if (subItemNew != null && subItemNew.getDefinition() != null) {
                subDefinition = subItemNew.getDefinition();
            } else if (subItemOld != null && subItemOld.getDefinition() != null) {
                subDefinition = subItemOld.getDefinition();
            } else if (itemDelta != null && itemDelta.getDefinition() != null) {
                subDefinition = itemDelta.getDefinition();
            }
            if (subDefinition == null && additionalDefinitionResolver != null) {
                subDefinition = additionalDefinitionResolver.resolve(definition, path);
            }
            if (subDefinition == null) {
                throw new SchemaException("Cannot find definition of a sub-item "+path+" of "+this);
            }
        }
        return new ItemDeltaItem<>(
                subItemOld, itemDelta, subItemNew, subDefinition, path, subResidualPath, subSubItemDeltas);
    }

    public void recompute() throws SchemaException {
        if (delta == null) {
            // TODO: do we need clone() here? new object may be read-only
            newObject = oldObject != null ? oldObject.clone() : null;
            return;
        }
        if (delta.isAdd()) {
            newObject = delta.getObjectToAdd();
            return;
        }
        if (delta.isDelete()) {
            newObject = null;
            return;
        }
        if (oldObject == null) {
            return;
        }
        newObject = oldObject.clone();
        delta.applyTo(newObject);
    }

    public void recomputeIfNeeded(boolean deep) throws SchemaException {
        if (delta == null) {
            if (newObject == null) {
                if (deep) {
                    // TODO: do we need clone() here? new object may be read-only
                    newObject = oldObject != null ? oldObject.clone() : null;
                } else {
                    newObject = oldObject;
                }
            }
            return;
        }
        if (delta.isAdd()) {
            if (newObject == null) {
                newObject = delta.getObjectToAdd();
            }
            return;
        }
        if (delta.isDelete()) {
            newObject = null;
            return;
        }
        if (oldObject == null) {
            return;
        }
        if (newObject == null) {
            newObject = oldObject.clone();
            delta.applyTo(newObject);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ObjectDeltaObject<?> that = (ObjectDeltaObject<?>) o;
        return Objects.equals(oldObject, that.oldObject)
                && Objects.equals(delta, that.delta)
                && Objects.equals(newObject, that.newObject)
                && Objects.equals(definition, that.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), oldObject, delta, newObject, definition);
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append("ObjectDeltaObject():");
        dumpObject(sb, oldObject, "old", indent +1);
        if (delta != null) {
            sb.append("\n");
            DebugUtil.indentDebugDump(sb, indent + 1);
            sb.append("delta:");
            if (delta == null) {
                sb.append(" null");
            } else {
                sb.append("\n");
                sb.append(delta.debugDump(indent + 2));
            }
        }
        dumpObject(sb, newObject, "new", indent +1);
        sb.append("\n");
        DebugUtil.debugDumpWithLabel(sb, "definition", definition, indent + 1);
        return sb.toString();
    }

    private void dumpObject(StringBuilder sb, PrismObject<O> object, String label, int indent) {
        sb.append("\n");
        DebugUtil.indentDebugDump(sb, indent);
        sb.append(label).append(":");
        if (object == null) {
            sb.append(" null");
        } else {
            sb.append("\n");
            sb.append(object.debugDump(indent + 1));
        }
    }

    @Override
    public String toString() {
        return "ObjectDeltaObject(" + oldObject + " + " + delta + " = " + newObject + ")";
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public ObjectDeltaObject<O> clone() {
        return new ObjectDeltaObject<>(
                CloneUtil.clone(oldObject),
                CloneUtil.clone(delta),
                CloneUtil.clone(newObject),
                definition);
    }

    public ObjectDeltaObject<O> normalizeValuesToDelete(boolean doClone) {
        if (delta == null || delta.getChangeType() != ChangeType.MODIFY) {
            return this;
        }
        boolean foundIdOnlyDeletion = false;
        main: for (ItemDelta<?, ?> itemDelta : delta.getModifications()) {
            for (PrismValue valueToDelete : CollectionUtils.emptyIfNull(itemDelta.getValuesToDelete())) {
                if (valueToDelete instanceof PrismContainerValue<?> pcv && pcv.isIdOnly()) {
                    foundIdOnlyDeletion = true;
                    break main;
                }
            }
        }
        if (!foundIdOnlyDeletion) {
            return this;
        }
        ObjectDeltaObject<O> object = doClone ? this.clone() : this;

        boolean anyRealChange = false;
        for (ItemDelta<?, ?> itemDelta : Objects.requireNonNull(object.delta).getModifications()) {
            if (itemDelta.getValuesToDelete() == null) {
                continue;
            }
            boolean itemDeltaChanged = false;
            List<PrismValue> newValuesToDelete = new ArrayList<>();
            for (PrismValue valueToDelete : itemDelta.getValuesToDelete()) {
                if (valueToDelete instanceof PrismContainerValue<?> pcv
                        && pcv.isIdOnly()
                        && object.oldObject != null /* should always be */) {
                    Object oldItem = object.oldObject.find(itemDelta.getPath());
                    if (oldItem instanceof PrismContainer) {
                        PrismContainerValue<?> oldValue =
                                ((PrismContainer<?>) oldItem)
                                        .getValue(((PrismContainerValue<?>) valueToDelete).getId());
                        if (oldValue != null) {
                            newValuesToDelete.add(oldValue.clone());
                            itemDeltaChanged = true;
                            continue;
                        }
                    }
                }
                newValuesToDelete.add(valueToDelete);
            }
            if (itemDeltaChanged) {
                itemDelta.resetValuesToDelete();
                //noinspection unchecked,rawtypes
                ((ItemDelta) itemDelta).addValuesToDelete(newValuesToDelete);
                anyRealChange = true;
            }
        }
        return anyRealChange ? object : this;
    }
}
