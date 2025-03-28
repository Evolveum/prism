/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.delta;

import static com.evolveum.midpoint.prism.path.ItemPath.CompareResult.*;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import static com.evolveum.midpoint.prism.path.ItemPath.CompareResult;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.util.CloneUtil;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.*;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ItemPathCollectionsUtil;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * @see ObjectDelta
 */
public class ObjectDeltaImpl<O extends Objectable> extends AbstractFreezable implements ObjectDelta<O> {

    private static final Trace LOGGER = TraceManager.getTrace(ObjectDeltaImpl.class);

    private static final long serialVersionUID = -528560467958335366L;

    private ChangeType changeType;

    /**
     * OID of the object that this delta applies to.
     */
    private String oid;

    /**
     * New object to add. Valid only if changeType==ADD
     */
    private PrismObject<O> objectToAdd;

    /**
     * Set of relative property deltas. Valid only if changeType==MODIFY
     */
    @NotNull private final Collection<? extends ItemDelta<?, ?>> modifications;

    /**
     * Class of the object that we describe.
     */
    private Class<O> objectTypeClass;


    public ObjectDeltaImpl(Class<O> objectTypeClass, ChangeType changeType) {
        Validate.notNull(objectTypeClass, "No objectTypeClass");
        Validate.notNull(changeType, "No changeType");
        //Validate.notNull(prismContext, "No prismContext");

        this.changeType = changeType;
        this.objectTypeClass = objectTypeClass;
        objectToAdd = null;
        modifications = createEmptyModifications();
    }

    @NotNull
    private ObjectDeltaImpl<O> createOffspring() {
        ObjectDeltaImpl<O> offspring = new ObjectDeltaImpl<>(objectTypeClass, ChangeType.MODIFY);
        offspring.setOid(getAnyOid());
        return offspring;
    }

    @Override
    public void accept(Visitor visitor) {
        accept(visitor, true);
    }

    @Override
    public void accept(Visitor visitor, boolean includeOldValues) {
        visitor.visit(this);
        if (isAdd()) {
            objectToAdd.accept(visitor);
        } else if (isModify()) {
            for (ItemDelta<?, ?> delta : getModifications()) {
                delta.accept(visitor, includeOldValues);
            }
        }
        // Nothing to visit for delete
    }

    @Override
    public void accept(Visitor visitor, ItemPath path, boolean recursive) {
        if (path == null || path.isEmpty()) {
            if (recursive) {
                accept(visitor);
            } else {
                visitor.visit(this);
            }
        } else {
            ItemDeltaCollectionsUtil.accept(getModifications(), visitor, path, recursive);
        }
    }

    @Override
    public ChangeType getChangeType() {
        return changeType;
    }

    @Override
    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    @Override
    public boolean isAdd() {
        return changeType == ChangeType.ADD;
    }

    @Override
    public boolean isDelete() {
        return changeType == ChangeType.DELETE;
    }

    @Override
    public boolean isModify() {
        return changeType == ChangeType.MODIFY;
    }

    @Override
    public String getOid() {
        return oid;
    }

    @Override
    public void setOid(String oid) {
        checkMutable();
        this.oid = oid;
        if (objectToAdd != null && !objectToAdd.isImmutable()) {
            objectToAdd.setOid(oid);
        }
    }

    // these two (oid vs. objectToAdd.oid) should be the same ... but what if they are not?
    private String getAnyOid() {
        if (objectToAdd != null && objectToAdd.getOid() != null) {
            return objectToAdd.getOid();
        } else {
            return oid;
        }
    }

    @Override
    public void setPrismContext(PrismContext prismContext) {
    }

    @Override
    public PrismObject<O> getObjectToAdd() {
        return objectToAdd;
    }

    @Override
    public void setObjectToAdd(PrismObject<O> objectToAdd) {
        checkMutable();
        if (getChangeType() != ChangeType.ADD) {
            throw new IllegalStateException("Cannot set object to " + getChangeType() + " delta");
        }
        this.objectToAdd = objectToAdd;
        if (objectToAdd != null) {
            this.objectTypeClass = objectToAdd.getCompileTimeClass();
        }
    }

    @Override
    @NotNull
    public Collection<? extends ItemDelta<?, ?>> getModifications() {
        return modifications;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends ItemDelta> D addModification(D itemDelta) {
        checkMutable();
        checkModifyDelta();
        ItemPath itemPath = itemDelta.getPath();
        // We use 'strict' finding mode because of MID-4690 (TODO)
        D existingModification = (D) findModification(itemPath, itemDelta.getClass(), true);
        if (existingModification != null) {
            existingModification.merge(itemDelta);
            return existingModification;
        } else {
            ((Collection) modifications).add(itemDelta);
            return itemDelta;
        }
    }

    private void checkModifyDelta() {
        if (getChangeType() != ChangeType.MODIFY) {
            throw new IllegalStateException("Cannot add/delete modifications to " + getChangeType() + " delta");
        }
    }

    @Override
    public boolean deleteModification(ItemDelta<?, ?> itemDelta) {
        checkMutable();
        checkModifyDelta();
        return modifications.remove(itemDelta);
    }

    @Override
    public boolean containsModification(ItemDelta itemDelta, EquivalenceStrategy strategy) {
        for (ItemDelta<?, ?> modification : modifications) {
            if (modification.contains(itemDelta, strategy)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAllModifications(Collection<? extends ItemDelta<?, ?>> itemDeltas, EquivalenceStrategy strategy) {
        for (ItemDelta<?, ?> itemDelta : itemDeltas) {
            if (!containsModification(itemDelta, strategy)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addModifications(Collection<? extends ItemDelta> itemDeltas) {
        for (ItemDelta<?, ?> modDelta : itemDeltas) {
            addModification(modDelta);
        }
    }

    @Override
    public void addModifications(ItemDelta<?, ?>... itemDeltas) {
        for (ItemDelta<?, ?> modDelta : itemDeltas) {
            addModification(modDelta);
        }
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> ItemDelta<IV, ID> findItemDelta(ItemPath itemPath) {
        //noinspection unchecked
        return findItemDelta(itemPath, ItemDelta.class, Item.class, false);
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> ItemDelta<IV, ID> findItemDelta(ItemPath itemPath, boolean strict) {
        //noinspection unchecked
        return findItemDelta(itemPath, ItemDelta.class, Item.class, strict);
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>, DD extends ItemDelta<IV, ID>>
    DD findItemDelta(ItemPath propertyPath, Class<DD> deltaType, Class<I> itemType, boolean strict) {
        if (changeType == ChangeType.ADD) {
            I item = objectToAdd.findItem(propertyPath, itemType);
            if (item == null) {
                return null;
            }
            DD itemDelta = createEmptyDelta(propertyPath, item.getDefinition(), item.getClass());
            itemDelta.addValuesToAdd(item.getClonedValues());
            return itemDelta;
        } else if (changeType == ChangeType.MODIFY) {
            return findModification(propertyPath, deltaType, strict);
        } else {
            return null;
        }
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> Collection<PartiallyResolvedDelta<IV, ID>> findPartial(
            ItemPath propertyPath) {
        if (changeType == ChangeType.ADD) {
            PartiallyResolvedItem<IV, ID> partialValue = objectToAdd.findPartial(propertyPath);
            if (partialValue == null || partialValue.getItem() == null) {
                return new ArrayList<>(0);
            }
            Item<IV, ID> item = partialValue.getItem();
            ItemDelta<IV, ID> itemDelta = item.createDelta();
            itemDelta.addValuesToAdd(item.getClonedValues());
            Collection<PartiallyResolvedDelta<IV, ID>> deltas = new ArrayList<>(1);
            deltas.add(new PartiallyResolvedDelta<>(itemDelta, partialValue.getResidualPath()));
            return deltas;
        } else if (changeType == ChangeType.MODIFY) {
            Collection<PartiallyResolvedDelta<IV, ID>> deltas = new ArrayList<>();
            for (ItemDelta<?, ?> modification : modifications) {
                CompareResult compareComplex = modification.getPath().compareComplex(propertyPath);
                if (compareComplex == EQUIVALENT) {
                    deltas.add(new PartiallyResolvedDelta<>((ItemDelta<IV, ID>) modification, null));
                } else if (compareComplex == CompareResult.SUBPATH) {   // path in modification is shorter than propertyPath
                    deltas.add(new PartiallyResolvedDelta<>((ItemDelta<IV, ID>) modification, null));
                } else if (compareComplex == CompareResult.SUPERPATH) { // path in modification is longer than propertyPath
                    deltas.add(new PartiallyResolvedDelta<>((ItemDelta<IV, ID>) modification,
                            modification.getPath().remainder(propertyPath)));
                }
            }
            return deltas;
        } else {
            return new ArrayList<>(0);
        }
    }

    @Override
    public boolean hasItemDelta(ItemPath propertyPath) {
        if (changeType == ChangeType.ADD) {
            Item item = objectToAdd.findItem(propertyPath, Item.class);
            return item != null;
        } else if (changeType == ChangeType.MODIFY) {
            ItemDelta modification = findModification(propertyPath, ItemDelta.class, false);
            return modification != null;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasItemOrSubitemDelta(ItemPath itemPath) {
        if (changeType == ChangeType.ADD) {
            // Easy case. Even if there is a sub-sub-property there must be also a container.
            //noinspection unchecked
            return objectToAdd.findItem(itemPath, Item.class) != null;
        } else if (changeType == ChangeType.MODIFY) {
            for (ItemDelta<?, ?> modification : getModifications()) {
                CompareResult compare = modification.getPath().compareComplex(itemPath);
                if (compare == EQUIVALENT || compare == CompareResult.SUBPATH) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasRelatedDelta(ItemPath itemPath) {
        if (changeType == ChangeType.ADD) {
            //noinspection unchecked
            return objectToAdd.findItem(itemPath, Item.class) != null;
        } else if (changeType == ChangeType.MODIFY) {
            for (ItemDelta<?, ?> modification : getModifications()) {
                CompareResult compare = modification.getPath().compareComplex(itemPath);
                if (compare != CompareResult.NO_RELATION) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasCompleteDefinition() {
        if (isAdd()) {
            return getObjectToAdd().hasCompleteDefinition();
        } else if (isModify()) {
            for (ItemDelta modification : getModifications()) {
                if (!modification.hasCompleteDefinition()) {
                    return false;
                }
//                return true;
            }
            return true;
        } else if (isDelete()) {
            return true;
        }
        throw new IllegalStateException("Strange things happen");
    }

    private <D extends ItemDelta, I extends Item> D createEmptyDelta(
            ItemPath propertyPath, ItemDefinition itemDef, Class<I> itemType) {

        if (PrismProperty.class.isAssignableFrom(itemType)) {
            return (D) new PropertyDeltaImpl<>(propertyPath, (PrismPropertyDefinition) itemDef);
        } else if (PrismContainer.class.isAssignableFrom(itemType)) {
            return (D) new ContainerDeltaImpl<>(propertyPath, (PrismContainerDefinition) itemDef);
        } else if (PrismReference.class.isAssignableFrom(itemType)) {
            return (D) new ReferenceDeltaImpl(propertyPath, (PrismReferenceDefinition) itemDef);
        } else {
            throw new IllegalArgumentException("Unknown item type " + itemType);
        }
    }

    @Override
    public Class<O> getObjectTypeClass() {
        return objectTypeClass;
    }

    @Override
    public void setObjectTypeClass(Class<O> objectTypeClass) {
        this.objectTypeClass = objectTypeClass;
    }

    @Override
    protected void performFreeze() {
        if (objectToAdd != null) {
            objectToAdd.freeze();
        }
        for (ItemDelta<?, ?> modification : modifications) {
            modification.freeze();
        }
    }

    /**
     * Top-level path is assumed.
     */
    @Override
    public <X> PropertyDelta<X> findPropertyDelta(ItemPath parentPath, QName propertyName) {
        return findPropertyDelta(ItemPath.create(parentPath, propertyName));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> PropertyDelta<X> findPropertyDelta(ItemPath propertyPath) {
        return findItemDelta(propertyPath, PropertyDelta.class, PrismProperty.class, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X extends Containerable> ContainerDelta<X> findContainerDelta(ItemPath propertyPath) {
        return findItemDelta(propertyPath, ContainerDelta.class, PrismContainer.class, false);
    }

    private <D extends ItemDelta> D findModification(ItemPath propertyPath, Class<D> deltaType, boolean strict) {
        if (isModify()) {
            return ItemDeltaCollectionsUtil.findItemDelta(modifications, propertyPath, deltaType, strict);
        } else if (isAdd()) {
            Item<PrismValue, ItemDefinition<?>> item = getObjectToAdd().findItem(propertyPath);
            if (item == null) {
                return null;
            }
            D itemDelta = (D) item.createDelta();
            itemDelta.addValuesToAdd(item.getClonedValues());
            return itemDelta;
        } else {
            return null;
        }
    }

    @Override
    public ReferenceDelta findReferenceModification(ItemPath itemPath) {
        return findModification(itemPath, ReferenceDelta.class, false);
    }

    /**
     * Returns all item deltas at or below a specified path.
     */
    @Override
    public @NotNull Collection<? extends ItemDelta<?, ?>> findItemDeltasSubPath(ItemPath itemPath) {
        return ItemDeltaCollectionsUtil.findItemDeltasSubPath(modifications, itemPath);
    }

    private <D extends ItemDelta> void removeModification(ItemPath propertyPath, Class<D> deltaType) {
        checkMutable();
        ItemDeltaCollectionsUtil.removeItemDelta(modifications, propertyPath, deltaType);
    }

    @Override
    public void removeModification(ItemDelta<?, ?> itemDelta) {
        checkMutable();
        ItemDeltaCollectionsUtil.removeItemDelta(modifications, itemDelta);
    }

    @Override
    public void removeReferenceModification(ItemPath itemPath) {
        removeModification(itemPath, ReferenceDelta.class);
    }

    @Override
    public void removeContainerModification(ItemPath itemName) {
        removeModification(itemName, ContainerDelta.class);
    }

    @Override
    public void removePropertyModification(ItemPath itemPath) {
        removeModification(itemPath, PropertyDelta.class);
    }

    @Override
    public boolean isEmpty() {
        if (getChangeType() == ChangeType.DELETE) {
            // Delete delta is never empty
            return false;
        }
        if (getChangeType() == ChangeType.ADD) {
            // Even if the object to add is empty, the delta as such is NOT empty!
            // (If the object to add is null, the delta is invalid.)
            return objectToAdd == null;
        }
        for (ItemDelta<?, ?> mod : modifications) {
            if (!mod.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void normalize() {
        checkMutable();
        if (objectToAdd != null && !objectToAdd.isImmutable()) {
            objectToAdd.normalize();
        }
        Iterator<? extends ItemDelta> iterator = modifications.iterator();
        while (iterator.hasNext()) {
            ItemDelta<?, ?> modification = iterator.next();
            modification.normalize();
            if (modification.isEmpty()) {
                iterator.remove();
            }
        }
    }

    @Override
    public ObjectDeltaImpl<O> narrow(PrismObject<O> existingObject, @NotNull ParameterizedEquivalenceStrategy plusStrategy,
            @NotNull ParameterizedEquivalenceStrategy minusStrategy, boolean assumeMissingItems) {
        checkMutable();
        if (!isModify()) {
            throw new UnsupportedOperationException("Narrow is supported only for modify deltas");
        }
        ObjectDeltaImpl<O> narrowedDelta = new ObjectDeltaImpl<>(this.objectTypeClass, this.changeType);
        narrowedDelta.oid = this.oid;
        for (ItemDelta<?, ?> modification : modifications) {
            ItemDelta<?, ?> narrowedModification = modification.narrow(existingObject, plusStrategy.prismValueComparator(),
                    minusStrategy.prismValueComparator(), assumeMissingItems);
            if (!ItemDelta.isEmpty(narrowedModification)) {
                narrowedDelta.addModification(narrowedModification);
            }
        }
        return narrowedDelta;
    }

    // TODO better name
    @Override
    public void applyDefinitionIfPresent(PrismObjectDefinition<O> definition, boolean tolerateNoDefinition) throws SchemaException {
        if (objectToAdd != null) {
            objectToAdd.applyDefinition(definition);            // TODO tolerateNoDefinition
        }
        ItemDeltaCollectionsUtil.applyDefinitionIfPresent(getModifications(), definition, tolerateNoDefinition);
    }

    /**
     * Deep clone.
     */
    @Override
    public ObjectDeltaImpl<O> clone() {
        ObjectDeltaImpl<O> clone = new ObjectDeltaImpl<>(this.objectTypeClass, this.changeType);
        copyValues(clone);
        return clone;
    }

    protected void copyValues(ObjectDeltaImpl<O> clone) {
        clone.oid = this.oid;
        for (ItemDelta<?, ?> thisModification : this.modifications) {
            ((Collection) clone.modifications).add(thisModification.clone());
        }
        if (this.objectToAdd == null) {
            clone.objectToAdd = null;
        } else {
            clone.objectToAdd = this.objectToAdd.clone();
        }
    }

    /**
     * Merge provided delta into this delta.
     * This delta is assumed to be chronologically earlier, delta in the parameter is assumed to come chronologicaly later.
     */
    @Override
    public void merge(ObjectDelta<O> deltaToMerge) throws SchemaException {
        checkMutable();
        if (deltaToMerge == null) {
            return;
        }
        if (changeType == ChangeType.ADD) {
            if (deltaToMerge.getChangeType() == ChangeType.ADD) {
                // Maybe we can, be we do not want. This is usually an error anyway.
                throw new IllegalArgumentException("Cannot merge two ADD deltas: " + this + ", " + deltaToMerge);
            } else if (deltaToMerge.getChangeType() == ChangeType.MODIFY) {
                if (objectToAdd == null) {
                    throw new IllegalStateException("objectToAdd is null");
                }
                deltaToMerge.applyTo(objectToAdd);
            } else if (deltaToMerge.getChangeType() == ChangeType.DELETE) {
                this.changeType = ChangeType.DELETE;
            }
        } else if (changeType == ChangeType.MODIFY) {
            if (deltaToMerge.getChangeType() == ChangeType.ADD) {
                throw new IllegalArgumentException("Cannot merge 'add' delta to a 'modify' object delta");
            } else if (deltaToMerge.getChangeType() == ChangeType.MODIFY) {
                mergeModifications(deltaToMerge.getModifications());
            } else if (deltaToMerge.getChangeType() == ChangeType.DELETE) {
                this.changeType = ChangeType.DELETE;
            }
        } else { // DELETE
            if (deltaToMerge.getChangeType() == ChangeType.ADD) {
                this.changeType = ChangeType.ADD;
                // TODO: clone?
                this.objectToAdd = deltaToMerge.getObjectToAdd();
            } else if (deltaToMerge.getChangeType() == ChangeType.MODIFY) {
                // Just ignore the modification of a deleted object
            } else if (deltaToMerge.getChangeType() == ChangeType.DELETE) {
                // Nothing to do
            }
        }
    }

    @Override
    public void mergeModifications(Collection<? extends ItemDelta> modificationsToMerge) throws SchemaException {
        for (ItemDelta<?, ?> propDelta : emptyIfNull(modificationsToMerge)) {
            mergeModification(propDelta);
        }
    }

    @Override
    public void mergeModification(ItemDelta<?, ?> modificationToMerge) throws SchemaException {
        swallow(modificationToMerge);
    }

    /**
     * Incorporates the property delta into the existing property deltas
     * (regardless of the change type).
     * <p>
     * TODO incorporate equivalence strategy
     */
    @Override
    public void swallow(ItemDelta<?, ?> newItemDelta) throws SchemaException {
        checkMutable();
        if (changeType == ChangeType.ADD) {
            newItemDelta.applyTo(objectToAdd);
        } else if (changeType == ChangeType.MODIFY) {
            swallowToModifyDelta(newItemDelta);
        }
        // nothing to do for DELETE
    }

    private void swallowToModifyDelta(ItemDelta<?, ?> newItemDelta) throws SchemaException {
        ItemPath newDeltaPath = newItemDelta.getPath();
        for (var existingDelta : modifications) {
            var existingDeltaPath = existingDelta.getPath();
            var relation = existingDeltaPath.compareComplex(newDeltaPath);
            if (relation == EQUIVALENT) {
                // Both items refer to the same item, so we assume the value types will be matching as well.
                //noinspection unchecked,rawtypes
                existingDelta.merge((ItemDelta) newItemDelta);
                return;
            } else if (relation == SUBPATH) {
                // The new delta updates a part of an item value that was touched by the previous delta.
                // For example, the previous delta may add an assignment, and the new delta may update it (e.g. its description).
                // We try to find the matching value in the existing delta and update it.
                var remainderWithValueId = newDeltaPath.remainder(existingDeltaPath);
                var valueId = remainderWithValueId.firstToIdOrNull();
                if (valueId != null) {
                    var remainder = remainderWithValueId.rest();
                    if (remainder.isEmpty()) {
                        // Strange situation. We are applying a delta right to a PCV? Most probably an invalid delta.
                        // Let's not try to solve it here, just pass it through.
                        continue;
                    }
                    for (var valueInExistingDelta : existingDelta.getNewValues()) {
                        if (valueInExistingDelta instanceof PrismContainerValue<?> pcv
                                && valueId.equals(pcv.getId())) {
                            newItemDelta.applyTo(pcv, remainder);
                            return;
                        }
                    }
                    continue; // The value was not found. The deltas are not related.
                }
                // There can be other cases, like for the single-valued containers.
                // We can implement them later, if needed. For now, let's continue looking for other matching delta.
                continue;
            } else if (relation == SUPERPATH) {
                // Either the deltas are unrelated (like older one adding an assignment, and the new one updating
                // a different assignment), or there is a conflict. We don't care at this moment.
                continue;
            } else if (relation == NO_RELATION) {
                // The deltas are completely unrelated
                continue;
            }
            assert false : "Unexpected relation " + relation;
        }

        // Nowhere to merge, so we just add the new delta to the list.
        //noinspection unchecked,rawtypes
        ((Collection) modifications).add(newItemDelta);
    }

    @Override
    public void applyTo(PrismObject<O> targetObject) throws SchemaException {
        if (isEmpty()) {
            // nothing to do
            return;
        }
        if (changeType != ChangeType.MODIFY) {
            throw new IllegalStateException("Can apply only MODIFY delta to object, got " + changeType + " delta");
        }
        applyTo(targetObject, modifications);
    }

    private static <O extends Objectable> void applyTo(PrismObject<O> targetObject,
            Collection<? extends ItemDelta<?, ?>> modifications) throws SchemaException {
        for (ItemDelta itemDelta : modifications) {
            itemDelta.applyTo(targetObject);
        }
    }

    /**
     * Applies this object delta to specified object, returns updated object.
     * It leaves the original object unchanged.
     *
     * @param objectOld object before change
     * @return object with applied changes or null if the object should not exit (was deleted)
     */
    @Override
    public PrismObject<O> computeChangedObject(PrismObject<O> objectOld) throws SchemaException {
        if (objectOld == null) {
            if (getChangeType() == ChangeType.ADD) {
                return CloneUtil.cloneIfMutable(getObjectToAdd());
            } else {
                //throw new IllegalStateException("Cannot apply "+getChangeType()+" delta to a null old object");
                // This seems to be quite OK
                return null;
            }
        }
        if (getChangeType() == ChangeType.DELETE) {
            return null;
        }
        // MODIFY change
        PrismObject<O> objectNew = objectOld.clone();
        for (ItemDelta modification : modifications) {
            modification.applyTo(objectNew);
        }
        return objectNew;
    }

    @Override
    public void swallow(List<ItemDelta<?, ?>> itemDeltas) throws SchemaException {
        for (ItemDelta<?, ?> itemDelta : itemDeltas) {
            swallow(itemDelta);
        }
    }

    private Collection<? extends ItemDelta<?, ?>> createEmptyModifications() {
        // Lists are easier to debug
        return new ArrayList<>();
    }

    @Override
    public <X> PropertyDelta<X> createPropertyModification(ItemPath path) {
        PrismObjectDefinition<O> objDef = PrismContext.get().getSchemaRegistry().findObjectDefinitionByCompileTimeClass(getObjectTypeClass());
        PrismPropertyDefinition<X> propDef = objDef.findPropertyDefinition(path);
        return createPropertyModification(path, propDef);
    }

    @Override
    public <C> PropertyDelta<C> createPropertyModification(ItemPath path, PrismPropertyDefinition propertyDefinition) {
        PropertyDelta<C> propertyDelta = new PropertyDeltaImpl<>(path, propertyDefinition);
        // No point in adding the modification to this delta. It will get merged anyway and it may disappear
        // it is not reliable and therefore it is better not to add it now.
        return propertyDelta;
    }

    public ReferenceDelta createReferenceModification(QName name, PrismReferenceDefinition referenceDefinition) {
        ReferenceDelta referenceDelta = new ReferenceDeltaImpl(ItemName.fromQName(name), referenceDefinition);
        return addModification(referenceDelta);
    }

    @Override
    public ReferenceDelta createReferenceModification(ItemPath path, PrismReferenceDefinition referenceDefinition) {
        ReferenceDelta referenceDelta = new ReferenceDeltaImpl(path, referenceDefinition);
        return addModification(referenceDelta);
    }

    @Override
    public <C extends Containerable> ContainerDelta<C> createContainerModification(ItemPath path) {
        PrismObjectDefinition<O> objDef = PrismContext.get().getSchemaRegistry().findObjectDefinitionByCompileTimeClass(getObjectTypeClass());
        PrismContainerDefinition<C> propDef = objDef.findContainerDefinition(path);
        return createContainerModification(path, propDef);
    }

    @Override
    public <C extends Containerable> ContainerDelta<C> createContainerModification(ItemPath path, PrismContainerDefinition<C> containerDefinition) {
        ContainerDelta<C> containerDelta = new ContainerDeltaImpl<>(path, containerDefinition);
        return addModification(containerDelta);
    }

    @Override
    @SafeVarargs
    public final <X> PropertyDelta<X> addModificationReplaceProperty(ItemPath propertyPath, X... propertyValues) {
        return ObjectDeltaFactoryImpl.fillInModificationReplaceProperty(this, propertyPath, propertyValues);
    }

    @SafeVarargs
    @Override
    public final <X> void addModificationAddProperty(ItemPath propertyPath, X... propertyValues) {
        ObjectDeltaFactoryImpl.fillInModificationAddProperty(this, propertyPath, propertyValues);
    }

    @Override
    public <X> void addModificationDeleteProperty(ItemPath propertyPath, X... propertyValues) {
        ObjectDeltaFactoryImpl.fillInModificationDeleteProperty(this, propertyPath, propertyValues);
    }

    @Override
    public <C extends Containerable> void addModificationAddContainer(ItemPath propertyPath, C... containerables) throws SchemaException {
        ObjectDeltaFactoryImpl.fillInModificationAddContainer(this, propertyPath, containerables);
    }

    @Override
    public <C extends Containerable> void addModificationAddContainer(ItemPath propertyPath, PrismContainerValue<C>... containerValues) {
        ObjectDeltaFactoryImpl.fillInModificationAddContainer(this, propertyPath, containerValues);
    }

    @Override
    public <C extends Containerable> void addModificationDeleteContainer(ItemPath propertyPath, C... containerables) throws SchemaException {
        ObjectDeltaFactoryImpl.fillInModificationDeleteContainer(this, propertyPath, containerables);
    }

    @Override
    public <C extends Containerable> void addModificationDeleteContainer(ItemPath propertyPath, PrismContainerValue<C>... containerValues) {
        ObjectDeltaFactoryImpl.fillInModificationDeleteContainer(this, propertyPath, containerValues);
    }

    @Override
    public <C extends Containerable> void addModificationReplaceContainer(ItemPath propertyPath, PrismContainerValue<C>... containerValues) {
        ObjectDeltaFactoryImpl.fillInModificationReplaceContainer(this, propertyPath, containerValues);
    }

    @Override
    public void addModificationAddReference(ItemPath path, PrismReferenceValue... refValues) {
        ObjectDeltaFactoryImpl.fillInModificationAddReference(this, path, refValues);
    }

    @Override
    public void addModificationDeleteReference(ItemPath path, PrismReferenceValue... refValues) {
        ObjectDeltaFactoryImpl.fillInModificationDeleteReference(this, path, refValues);
    }

    @Override
    public void addModificationReplaceReference(ItemPath path, PrismReferenceValue... refValues) {
        ObjectDeltaFactoryImpl.fillInModificationReplaceReference(this, path, refValues);
    }

    @Override
    public ReferenceDelta createReferenceModification(ItemPath refPath) {
        PrismObjectDefinition<O> objDef = PrismContext.get().getSchemaRegistry().findObjectDefinitionByCompileTimeClass(getObjectTypeClass());
        PrismReferenceDefinition refDef = objDef.findReferenceDefinition(refPath);
        return createReferenceModification(refPath, refDef);
    }

    public static <O extends Objectable> ObjectDeltaImpl<O> createEmptyModifyDelta(Class<O> type, String oid) {
        return createEmptyDelta(type, oid, ChangeType.MODIFY);
    }

    public static <O extends Objectable> ObjectDeltaImpl<O> createEmptyDeleteDelta(Class<O> type, String oid) {
        return createEmptyDelta(type, oid, ChangeType.DELETE);
    }

    public static <O extends Objectable> ObjectDeltaImpl<O> createEmptyDelta(Class<O> type, String oid, ChangeType changeType) {
        ObjectDeltaImpl<O> objectDelta = new ObjectDeltaImpl<>(type, changeType);
        objectDelta.setOid(oid);
        return objectDelta;
    }

    public static <O extends Objectable> ObjectDeltaImpl<O> createAddDelta(PrismObject<O> objectToAdd) {
        ObjectDeltaImpl<O> objectDelta = new ObjectDeltaImpl<>(objectToAdd.getCompileTimeClass(), ChangeType.ADD);
        objectDelta.setOid(objectToAdd.getOid());
        objectDelta.setObjectToAdd(objectToAdd);
        return objectDelta;
    }

    public static <O extends Objectable> ObjectDeltaImpl<O> createDeleteDelta(Class<O> type, String oid) {
        ObjectDeltaImpl<O> objectDelta = new ObjectDeltaImpl<>(type, ChangeType.DELETE);
        objectDelta.setOid(oid);
        return objectDelta;
    }

    @Override
    public ObjectDeltaImpl<O> createReverseDelta() throws SchemaException {
        if (isAdd()) {
            return createDeleteDelta(getObjectTypeClass(), getOid());
        }
        if (isDelete()) {
            throw new SchemaException("Cannot reverse delete delta");
        }
        ObjectDeltaImpl<O> reverseDelta = createEmptyModifyDelta(getObjectTypeClass(), getOid());
        for (ItemDelta<?, ?> modification : getModifications()) {
            reverseDelta.addModification(modification.createReverseDelta());
        }
        return reverseDelta;
    }

    @Override
    public void checkConsistence() {
        checkConsistence(ConsistencyCheckScope.THOROUGH);
    }

    @Override
    public void checkConsistence(ConsistencyCheckScope scope) {
        checkConsistence(true, false, false, scope);
    }

    @Override
    public void checkConsistence(boolean requireOid, boolean requireDefinition, boolean prohibitRaw) {
        checkConsistence(requireOid, requireDefinition, prohibitRaw, ConsistencyCheckScope.THOROUGH);
    }

    @Override
    public void checkConsistence(boolean requireOid, boolean requireDefinition, boolean prohibitRaw, ConsistencyCheckScope scope) {
        if (getChangeType() == ChangeType.ADD) {
            if (scope.isThorough() && !getModifications().isEmpty()) {
                throw new IllegalStateException("Modifications present in ADD delta " + this);
            }
            if (getObjectToAdd() != null) {
                getObjectToAdd().checkConsistence(requireDefinition, prohibitRaw, scope);
            } else {
                throw new IllegalStateException("Delta is ADD, but there is not object to add in " + this);
            }
        } else if (getChangeType() == ChangeType.MODIFY) {
            if (scope.isThorough()) {
                checkIdentifierConsistence(requireOid);
                if (getObjectToAdd() != null) {
                    throw new IllegalStateException("Object to add present in MODIFY delta " + this);
                }
                if (getModifications() == null) {
                    throw new IllegalStateException("Null modification in MODIFY delta " + this);
                }
            }
            ItemDeltaCollectionsUtil.checkConsistence(getModifications(), requireDefinition, prohibitRaw, scope);
        } else if (getChangeType() == ChangeType.DELETE) {
            if (scope.isThorough()) {
                if (requireOid && getOid() == null) {
                    throw new IllegalStateException("Null oid in delta " + this);
                }
                if (getObjectToAdd() != null) {
                    throw new IllegalStateException("Object to add present in DELETE delta " + this);
                }
                if (!getModifications().isEmpty()) {
                    throw new IllegalStateException("Modifications present in DELETE delta " + this);
                }
            }
        } else {
            throw new IllegalStateException("Unknown change type " + getChangeType() + " in delta " + this);
        }
    }

    protected void checkIdentifierConsistence(boolean requireOid) {
        if (requireOid && getOid() == null) {
            throw new IllegalStateException("Null oid in delta " + this);
        }
    }

    @Override
    public void assertDefinitions() throws SchemaException {
        assertDefinitions(() -> "");
    }

    @Override
    public void assertDefinitions(Supplier<String> sourceDescription) throws SchemaException {
        assertDefinitions(false, sourceDescription);
    }

    @Override
    public void assertDefinitions(boolean tolerateRawElements) throws SchemaException {
        assertDefinitions(tolerateRawElements, () -> "");
    }

    /**
     * Assert that all the items has appropriate definition.
     */
    @Override
    public void assertDefinitions(boolean tolerateRawElements, Supplier<String> sourceDescriptionSupplier) throws SchemaException {
        if (changeType == ChangeType.ADD) {
            objectToAdd.assertDefinitions(() -> "add delta in " + sourceDescriptionSupplier.get());
        }
        if (changeType == ChangeType.MODIFY) {
            for (ItemDelta<?, ?> mod : modifications) {
                mod.assertDefinitions(tolerateRawElements, () -> "modify delta for " + getOid() + " in " + sourceDescriptionSupplier.get());
            }
        }
    }

    @Override
    public void revive(PrismContext prismContext) throws SchemaException {
        if (objectToAdd != null) {
            objectToAdd.revive(prismContext);
        }
        for (ItemDelta<?, ?> modification : modifications) {
            modification.revive(prismContext);
        }
    }

    @Override
    public void applyDefinition(@NotNull PrismObjectDefinition<O> objectDefinition, boolean force) throws SchemaException {
        if (objectToAdd != null) {
            objectToAdd.applyDefinition(objectDefinition, force);
        }
        for (ItemDelta<?, ?> modification : modifications) {
            ItemPath path = modification.getPath();
            ItemDefinition<?> itemDefinition = objectDefinition.findItemDefinition(path);
            if (itemDefinition != null) {
                //noinspection unchecked,rawtypes
                ((ItemDelta) modification).applyDefinition(itemDefinition, force);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((changeType == null) ? 0 : changeType.hashCode());
        result = prime * result
                + ((objectToAdd == null) ? 0 : objectToAdd.hashCode());
        result = prime * result
                + ((objectTypeClass == null) ? 0 : objectTypeClass.hashCode());
        result = prime * result + ((oid == null) ? 0 : oid.hashCode());
        return result;
    }

    @Override
    public boolean equivalent(ObjectDelta o) {
        ObjectDeltaImpl other = (ObjectDeltaImpl) o;
        if (changeType != other.changeType) { return false; }
        if (objectToAdd == null) {
            if (other.objectToAdd != null) { return false; }
        } else if (!objectToAdd.equivalent(other.objectToAdd)) {
            return false;
        }
        if (!MiscUtil.unorderedCollectionEquals(this.modifications, other.modifications, (o1, o2) -> o1.equivalent((ItemDelta) o2))) {
            return false;
        }
        return Objects.equals(objectTypeClass, other.objectTypeClass) && Objects.equals(oid, other.oid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        ObjectDeltaImpl<?> other = (ObjectDeltaImpl<?>) obj;
        if (changeType != other.changeType) { return false; }
        //noinspection RedundantCast,unchecked
        if (!MiscUtil.unorderedCollectionEquals((Collection) this.modifications, (Collection) other.modifications)) {
            return false;
        }
        if (objectToAdd == null) {
            if (other.objectToAdd != null) { return false; }
        } else if (!objectToAdd.equals(other.objectToAdd)) {
            return false;
        }
        if (objectTypeClass == null) {
            if (other.objectTypeClass != null) { return false; }
        } else if (!objectTypeClass.equals(other.objectTypeClass)) {
            return false;
        }
        if (oid == null) {
            if (other.oid != null) { return false; }
        } else if (!oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(debugName());
        sb.append("(").append(debugIdentifiers());
        sb.append(",").append(changeType).append(": ");
        if (changeType == ChangeType.ADD) {
            if (objectToAdd == null) {
                sb.append("null");
            } else {
                sb.append(objectToAdd.toString());
            }
        } else if (changeType == ChangeType.MODIFY) {
            Iterator<? extends ItemDelta> i = modifications.iterator();
            while (i.hasNext()) {
                sb.append(i.next().toString());
                if (i.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        // Nothing to print for delete
        sb.append(")");
        return sb.toString();
    }

    protected String debugName() {
        return "ObjectDelta";
    }

    protected String debugIdentifiers() {
        return toDebugType() + ":" + getOid();
    }

    /**
     * Returns short string identification of object type. It should be in a form
     * suitable for log messages. There is no requirement for the type name to be unique,
     * but it rather has to be compact. E.g. short element names are preferred to long
     * QNames or URIs.
     */
    @Override
    public String toDebugType() {
        if (objectTypeClass == null) {
            return "(unknown)";
        }
        return objectTypeClass.getSimpleName();
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append(debugName());
        sb.append("<").append(objectTypeClass.getSimpleName()).append(">(");
        sb.append(debugIdentifiers()).append(",").append(changeType);
        if (changeType == ChangeType.DELETE) {
            // Nothing to print for delete
            sb.append(")");
        } else {
            sb.append("):\n");
            if (objectToAdd == null) {
                if (changeType == ChangeType.ADD) {
                    DebugUtil.indentDebugDump(sb, indent + 1);
                    sb.append("null");
                }
            } else {
                sb.append(objectToAdd.debugDump(indent + 1));
            }
            Iterator<? extends ItemDelta> i = modifications.iterator();
            while (i.hasNext()) {
                sb.append(i.next().debugDump(indent + 1));
                if (i.hasNext()) {
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Returns modifications that are related to the given paths; removes them from the original delta.
     * Applicable only to modify deltas.
     * Currently compares paths by "equals" predicate -- in the future we might want to treat sub/super/equivalent paths!
     * So consider this method highly experimental.
     */
    @Override
    public ObjectDeltaImpl<O> subtract(@NotNull Collection<ItemPath> paths) {
        checkMutable();
        if (!isModify()) {
            throw new UnsupportedOperationException("Only for MODIFY deltas, not for " + this);
        }
        ObjectDeltaImpl<O> rv = new ObjectDeltaImpl<>(this.objectTypeClass, ChangeType.MODIFY);
        rv.oid = this.oid;
        Iterator<? extends ItemDelta<?, ?>> iterator = modifications.iterator();
        while (iterator.hasNext()) {
            ItemDelta<?, ?> itemDelta = iterator.next();
            if (paths.contains(itemDelta.getPath())) {
                rv.addModification(itemDelta);
                iterator.remove();
            }
        }
        return rv;
    }

    @Override
    @NotNull
    public FactorOutResultSingle<O> factorOut(Collection<? extends ItemPath> paths, boolean cloneDelta) {
        if (isAdd()) {
            return factorOutForAddDelta(paths, cloneDelta);
        } else if (isDelete()) {
            throw new UnsupportedOperationException("factorOut is not supported for delete deltas");
        } else {
            return factorOutForModifyDelta(paths, cloneDelta);
        }
    }

    @Override
    @NotNull
    public FactorOutResultMulti<O> factorOutValues(ItemPath path, boolean cloneDelta) throws SchemaException {
        if (isAdd()) {
            return factorOutValuesForAddDelta(path, cloneDelta);
        } else if (isDelete()) {
            throw new UnsupportedOperationException("factorOut is not supported for delete deltas");
        } else {
            return factorOutValuesForModifyDelta(path, cloneDelta);
        }
    }

    /**
     * Works if we are looking e.g. for modification to inducement item,
     * and delta contains e.g. REPLACE(inducement[1]/validTo, "...").
     * <p>
     * Does NOT work the way around: if we are looking for modification to inducement/validTo and
     * delta contains e.g. ADD(inducement, ...). In such a case we would need to do more complex processing,
     * involving splitting value-to-be-added into remainder and offspring delta. It's probably doable,
     * but some conditions would have to be met, e.g. inducement to be added must have an ID.
     */
    private FactorOutResultSingle<O> factorOutForModifyDelta(Collection<? extends ItemPath> paths, boolean cloneDelta) {
        checkMutable();
        ObjectDeltaImpl<O> remainder = cloneIfRequested(cloneDelta);
        ObjectDeltaImpl<O> offspring = null;
        List<ItemDelta<?, ?>> modificationsFound = new ArrayList<>();
        for (Iterator<? extends ItemDelta<?, ?>> iterator = remainder.modifications.iterator(); iterator.hasNext(); ) {
            ItemDelta<?, ?> modification = iterator.next();
            if (ItemPathCollectionsUtil.containsSubpathOrEquivalent(paths, modification.getPath())) {
                modificationsFound.add(modification);
                iterator.remove();
            }
        }
        if (!modificationsFound.isEmpty()) {
            offspring = createOffspring();
            modificationsFound.forEach(offspring::addModification);
        }
        return new FactorOutResultSingle<>(remainder, offspring);
    }

    private FactorOutResultSingle<O> factorOutForAddDelta(Collection<? extends ItemPath> paths, boolean cloneDelta) {
        List<Item<?, ?>> itemsFound = new ArrayList<>();
        for (ItemPath path : paths) {
            Item<?, ?> item = objectToAdd.findItem(path);
            if (item != null && !item.isEmpty()) {
                itemsFound.add(item);
            }
        }
        if (itemsFound.isEmpty()) {
            return new FactorOutResultSingle<>(this, null);
        }
        ObjectDeltaImpl<O> remainder = cloneIfRequested(cloneDelta);
        ObjectDeltaImpl<O> offspring = createOffspring();
        for (Item<?, ?> item : itemsFound) {
            remainder.getObjectToAdd().remove(item);
            offspring.addModification(ItemDeltaUtil.createAddDeltaFor(item));
        }
        return new FactorOutResultSingle<>(remainder, offspring);
    }

    private ObjectDeltaImpl<O> cloneIfRequested(boolean cloneDelta) {
        return cloneDelta ? clone() : this;
    }

    /**
     * Works if we are looking e.g. for modification to inducement item,
     * and delta contains e.g. REPLACE(inducement[1]/validTo, "...").
     * <p>
     * Does NOT work the way around: if we are looking for modification to inducement/validTo and
     * delta contains e.g. ADD(inducement, ...). In such a case we would need to do more complex processing,
     * involving splitting value-to-be-added into remainder and offspring delta. It's probably doable,
     * but some conditions would have to be met, e.g. inducement to be added must have an ID.
     */
    private FactorOutResultMulti<O> factorOutValuesForModifyDelta(ItemPath path, boolean cloneDelta) throws SchemaException {
        checkMutable();
        ObjectDeltaImpl<O> remainder = cloneIfRequested(cloneDelta);
        FactorOutResultMulti<O> rv = new FactorOutResultMulti<>(remainder);

        MultiValuedMap<Long, ItemDelta<?, ?>> modificationsForId = new ArrayListValuedHashMap<>();
        PrismObjectDefinition<O> objDef = objectTypeClass != null ? PrismContext.get().getSchemaRegistry().findObjectDefinitionByCompileTimeClass(objectTypeClass) : null;
        ItemDefinition itemDef = objDef != null ? objDef.findItemDefinition(path) : null;
        Boolean isSingle = itemDef != null ? itemDef.isSingleValue() : null;
        if (isSingle == null) {
            LOGGER.warn("Couldn't find definition for {}:{}", objectTypeClass, path);
            isSingle = false;
        }
        // first we take whole values being added or deleted
        for (Iterator<? extends ItemDelta<?, ?>> iterator = remainder.modifications.iterator(); iterator.hasNext(); ) {
            ItemDelta<?, ?> modification = iterator.next();
            if (path.equivalent(modification.getPath())) {
                if (modification.isReplace()) {
                    throw new UnsupportedOperationException("Cannot factor out values for replace item delta. Path = "
                            + path + ", modification = " + modification);
                }
                for (PrismValue prismValue : emptyIfNull(modification.getValuesToAdd())) {
                    //noinspection unchecked
                    createNewDelta(rv, modification).addValueToAdd(prismValue.clone());
                }
                for (PrismValue prismValue : emptyIfNull(modification.getValuesToDelete())) {
                    //noinspection unchecked
                    createNewDelta(rv, modification).addValueToDelete(prismValue.clone());
                }
                iterator.remove();
            } else if (path.isSubPath(modification.getPath())) {
                // e.g. factoring inducement, having REPLACE(inducement[x]/activation/validTo, ...) or ADD(inducement[x]/activation)
                ItemPath remainingPath = modification.getPath().remainder(path);
                Long id = remainingPath.firstToIdOrNull();
                modificationsForId.put(id, modification);
                iterator.remove();
            }
        }
        // and then we take modifications inside the values
        if (Boolean.TRUE.equals(isSingle)) {
            ObjectDeltaImpl<O> offspring = createOffspring();
            modificationsForId.values().forEach(mod -> offspring.addModification(mod));
            rv.offsprings.add(offspring);
        } else {
            for (Long id : modificationsForId.keySet()) {
                ObjectDeltaImpl<O> offspring = createOffspring();
                modificationsForId.get(id).forEach(mod -> offspring.addModification(mod));
                rv.offsprings.add(offspring);
            }
        }
        return rv;
    }

    private ItemDelta createNewDelta(FactorOutResultMulti<O> rv, ItemDelta<?, ?> modification)
            throws SchemaException {
        ObjectDeltaImpl<O> offspring = createOffspring();
        ItemDelta delta = modification.getDefinition().instantiate().createDelta(modification.getPath());
        offspring.addModification(delta);
        rv.offsprings.add(offspring);
        return delta;
    }

    private FactorOutResultMulti<O> factorOutValuesForAddDelta(ItemPath path, boolean cloneDelta) {
        Item<?, ?> item = objectToAdd.findItem(path);
        if (item == null || item.isEmpty()) {
            return new FactorOutResultMulti<>(this);
        }
        ObjectDeltaImpl<O> remainder = cloneIfRequested(cloneDelta);
        remainder.getObjectToAdd().remove(item);
        FactorOutResultMulti<O> rv = new FactorOutResultMulti<>(remainder);
        for (PrismValue value : item.getValues()) {
            ObjectDeltaImpl<O> offspring = createOffspring();
            offspring.addModification(ItemDeltaUtil.createAddDeltaFor(item, value));
            rv.offsprings.add(offspring);
        }
        return rv;
    }

    /**
     * Checks if the delta tries to add (or set) a 'value' for the item identified by 'itemPath'. If yes, it removes it.
     * <p>
     * TODO consider changing return value to 'incremental delta' (or null)
     *
     * @param dryRun only testing if value could be subtracted; not changing anything
     * @return true if the delta originally contained an instruction to add (or set) 'itemPath' to 'value'.
     */
    @Override
    public boolean subtract(@NotNull ItemPath itemPath, @NotNull PrismValue value, boolean fromMinusSet, boolean dryRun) {
        checkMutable();
        if (isAdd()) {
            return !fromMinusSet && subtractFromObject(objectToAdd, itemPath, value, dryRun);
        } else {
            return subtractFromModifications(modifications, itemPath, value, fromMinusSet, dryRun);
        }
    }

    private static <V extends PrismValue> boolean subtractFromModifications(Collection<? extends ItemDelta<?, ?>> modifications,
            @NotNull ItemPath itemPath, @NotNull V value, boolean fromMinusSet, boolean dryRun) {
        if (modifications == null) {
            return false;
        }
        boolean wasPresent = false;
        Iterator<? extends ItemDelta<?, ?>> itemDeltaIterator = modifications.iterator();
        while (itemDeltaIterator.hasNext()) {
            //noinspection unchecked
            ItemDelta<V, ?> itemDelta = (ItemDelta<V, ?>) itemDeltaIterator.next();
            if (itemPath.equivalent(itemDelta.getPath())) {
                if (!fromMinusSet) {
                    // The equivalence strategy used (real value considering different IDs) is the same for dry & normal runs.
                    // ("removeValueToXXX" methods use REAL_VALUE_CONSIDER_DIFFERENT_IDS strategy.)
                    if (dryRun) {
                        wasPresent = wasPresent
                                || itemDelta.containsValueToAdd(value, EquivalenceStrategy.REAL_VALUE_CONSIDER_DIFFERENT_IDS)
                                || itemDelta.containsValueToReplace(value, EquivalenceStrategy.REAL_VALUE_CONSIDER_DIFFERENT_IDS);
                    } else {
                        boolean removed1 = itemDelta.removeValueToAdd(value);
                        boolean removed2 = itemDelta.removeValueToReplace(value);
                        wasPresent = wasPresent || removed1 || removed2;
                    }
                } else {
                    if (itemDelta.getValuesToReplace() != null) {
                        throw new UnsupportedOperationException("Couldn't subtract 'value to be deleted' from REPLACE itemDelta: " + itemDelta);
                    }
                    if (dryRun) {
                        wasPresent = wasPresent || itemDelta.containsValueToDelete(value, EquivalenceStrategy.REAL_VALUE_CONSIDER_DIFFERENT_IDS);
                    } else {
                        wasPresent = wasPresent || itemDelta.removeValueToDelete(value);
                    }
                }
                if (!dryRun && itemDelta.isEmpty()) {
                    itemDeltaIterator.remove();
                }
            }
        }
        return wasPresent;
    }

    private static boolean subtractFromObject(@NotNull PrismObject<?> object, @NotNull ItemPath itemPath,
            @NotNull PrismValue value, boolean dryRun) {
        Item<PrismValue, ItemDefinition<?>> item = object.findItem(itemPath);
        if (item == null) {
            return false;
        }
        if (dryRun) {
            return item.contains(value);
        } else {
            return item.remove(value);
        }
    }

    @Override
    @NotNull
    public List<ItemPath> getModifiedItems() {
        if (!isModify()) {
            throw new UnsupportedOperationException("Supported only for modify deltas");
        }
        return modifications.stream().map(ItemDelta::getPath).collect(Collectors.toList());
    }

    @Override
    public List<PrismValue> getNewValuesFor(ItemPath itemPath) {
        if (isAdd()) {
            Item<PrismValue, ItemDefinition<?>> item = objectToAdd.findItem(itemPath);
            return item != null ? item.getValues() : Collections.emptyList();
        } else if (isDelete()) {
            return Collections.emptyList();
        } else {
            ItemDelta itemDelta = ItemDeltaCollectionsUtil.findItemDelta(modifications, itemPath, ItemDelta.class, false);
            if (itemDelta != null) {
                if (itemDelta.getValuesToReplace() != null) {
                    return (List<PrismValue>) itemDelta.getValuesToReplace();
                } else if (itemDelta.getValuesToAdd() != null) {
                    return (List<PrismValue>) itemDelta.getValuesToAdd();
                } else {
                    return Collections.emptyList();
                }
            } else {
                return Collections.emptyList();
            }
        }
    }

    /**
     * Limitations:
     * (1) For DELETE object delta, we don't know what values were in the object's item.
     * (2) For REPLACE item delta, we don't know what values were in the object's item (but these deltas are quite rare
     * for multivalued items; and eventually there will be normalized into ADD+DELETE form)
     * (3) For DELETE item delta for PrismContainers, content of items deleted might not be known
     * (only ID could be provided on PCVs).
     */
    @Override
    @SuppressWarnings("unused")            // used from scripts
    public List<PrismValue> getDeletedValuesFor(ItemPath itemPath) {
        if (isAdd()) {
            return Collections.emptyList();
        } else if (isDelete()) {
            return Collections.emptyList();
        } else {
            ItemDelta itemDelta = ItemDeltaCollectionsUtil.findItemDelta(modifications, itemPath, ItemDelta.class, false);
            if (itemDelta != null) {
                if (itemDelta.getValuesToDelete() != null) {
                    return (List<PrismValue>) itemDelta.getValuesToDelete();
                } else {
                    return Collections.emptyList();
                }
            } else {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public void clear() {
        checkMutable();
        if (isAdd()) {
            setObjectToAdd(null);
        } else if (isModify()) {
            modifications.clear();
        } else if (isDelete()) {
            // hack: convert to empty ADD delta
            setChangeType(ChangeType.ADD);
            setObjectToAdd(null);
            setOid(null);
        } else {
            throw new IllegalStateException("Unsupported delta type: " + getChangeType());
        }
    }

    @Override
    public boolean isRedundant(PrismObject<O> object, @NotNull ParameterizedEquivalenceStrategy plusStrategy,
            @NotNull ParameterizedEquivalenceStrategy minusStrategy, boolean assumeMissingItems) throws SchemaException {
        switch (changeType) {
            case MODIFY:
                if (object == null) {
                    throw new SchemaException("Cannot apply MODIFY delta to a null object");    // TODO reconsider this exception
                } else {
                    return ObjectDelta.isEmpty(narrow(object, plusStrategy, minusStrategy, assumeMissingItems));
                }
            case DELETE:
                return object == null;
            case ADD:
                return object != null && object.equals(objectToAdd);
            default:
                throw new AssertionError("Unknown change type: " + changeType);
        }
    }

    @Experimental // todo review and write some tests
    @Override
    public void removeOperationalItems() {
        switch (changeType) {
            case ADD:
                objectToAdd.getValue().removeOperationalItems();
                return;
            case MODIFY:
                Iterator<? extends ItemDelta<?, ?>> iterator = modifications.iterator();
                while (iterator.hasNext()) {
                    ItemDelta<?, ?> itemDelta = iterator.next();
                    ItemDefinition<?> definition = itemDelta.getDefinition();
                    if (definition != null && definition.isOperational()) {
                        iterator.remove();
                    } else {
                        emptyIfNull(itemDelta.getValuesToAdd()).forEach(this::removeOperationalItems);
                        emptyIfNull(itemDelta.getValuesToDelete()).forEach(this::removeOperationalItems);
                        emptyIfNull(itemDelta.getValuesToReplace()).forEach(this::removeOperationalItems);
                        emptyIfNull(itemDelta.getEstimatedOldValues()).forEach(this::removeOperationalItems);
                    }
                }
                return;
            case DELETE:
                // nothing to do here
        }
    }

    private void removeOperationalItems(PrismValue value) {
        if (value instanceof PrismContainerValue) {
            ((PrismContainerValue<?>) value).removeOperationalItems();
        }
    }

    @Experimental // todo review and write some tests
    @Override
    public void removeEstimatedOldValues() {
        if (changeType == ChangeType.MODIFY) {
            for (ItemDelta<?, ?> modification : modifications) {
                modification.setEstimatedOldValues(null);
            }
        }
    }

    @Override
    public PrismValueDeltaSetTriple<PrismObjectValue<O>> toDeltaSetTriple(PrismObject<O> objectOld) throws SchemaException {
        if (objectOld == null) {
            if (isAdd()) {
                PrismValueDeltaSetTriple<PrismObjectValue<O>> triple = new PrismValueDeltaSetTripleImpl<>();
                triple.addToPlusSet(objectToAdd.getValue());
                return triple;
            } else {
                throw new IllegalArgumentException("Couldn't create delta set triple for null objectOld and delta: " + this);
            }
        } else {
            if (isAdd()) {
                throw new IllegalArgumentException("Couldn't create delta set triple for " + objectOld + " and delta: " + this);
            }
            PrismValueDeltaSetTriple<PrismObjectValue<O>> triple = new PrismValueDeltaSetTripleImpl<>();
            triple.addToMinusSet(objectOld.getValue().clone());
            if (isModify()) {
                PrismObject<O> objectNew = objectOld.clone();
                applyTo(objectNew);
                triple.addToPlusSet(objectNew.getValue());
            } else {
                assert isDelete();
            }
            return triple;
        }
    }
}
