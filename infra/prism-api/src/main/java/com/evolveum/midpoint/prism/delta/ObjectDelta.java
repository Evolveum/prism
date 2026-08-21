/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.delta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.Nullable;

import static com.evolveum.midpoint.prism.PrismObject.asObjectable;

/**
 * Relative difference (delta) of the object.
 * This class describes how the object changes. It can describe either object addition, modification of deletion.
 * <ul>
 * <li>Addition describes complete new (absolute) state of the object.</li>
 * <li>Modification contains a set property deltas that describe relative changes to individual properties</li>
 * <li>Deletion does not contain anything. It only marks object for deletion.</li>
 * </ul>
 * The OID is mandatory for modification and deletion.
 * <p>
 * See <a href="https://docs.evolveum.com/midpoint/prism/deltas/">this document</a> for more.
 *
 * @author Radovan Semancik
 * @see PropertyDelta
 */
public interface ObjectDelta<O extends Objectable>
        extends DebugDumpable, Visitable, PathVisitable, Serializable, Freezable, Cloneable {

    void accept(Visitor visitor, boolean includeOldValues);

    @Override
    void accept(Visitor visitor, ItemPath path, boolean recursive);

    ChangeType getChangeType();

    void setChangeType(ChangeType changeType);

    static boolean isAdd(ObjectDelta<?> objectDelta) {
        return objectDelta != null && objectDelta.isAdd();
    }

    boolean isAdd();

    static boolean isDelete(ObjectDelta<?> objectDelta) {
        return objectDelta != null && objectDelta.isDelete();
    }

    boolean isDelete();

    static boolean isModify(ObjectDelta<?> objectDelta) {
        return objectDelta != null && objectDelta.isModify();
    }

    boolean isModify();

    String getOid();

    void setOid(String oid);

    void setPrismContext(PrismContext prismContext);

    PrismObject<O> getObjectToAdd();

    default O getObjectableToAdd() {
        return asObjectable(getObjectToAdd());
    }

    void setObjectToAdd(PrismObject<O> objectToAdd);

    /** Directly updatable. */
    @NotNull
    Collection<? extends ItemDelta<?, ?>> getModifications();

    /**
     * Adds modification (itemDelta) and returns the modification that was added.
     * NOTE: the modification that was added may be different from the modification that was
     * passed into this method! E.g. in case if two modifications must be merged to keep the delta
     * consistent. Therefore always use the returned modification after this method is invoked.
     */
    <D extends ItemDelta> D addModification(D itemDelta);

    /**
     * Deletes a modification, if it exists in a given MODIFY delta. (Throws an exception if the delta is not a MODIFY one.)
     *
     * @return true if the modification was found and removed
     */
    @Experimental
    boolean deleteModification(ItemDelta<?, ?> itemDelta);

    boolean containsModification(ItemDelta itemDelta, EquivalenceStrategy strategy);

    boolean containsAllModifications(Collection<? extends ItemDelta<?, ?>> itemDeltas, EquivalenceStrategy strategy);

    void addModifications(Collection<? extends ItemDelta> itemDeltas);

    void addModifications(ItemDelta<?, ?>... itemDeltas);

    /**
     * TODO specify this method!
     * <p>
     * An attempt:
     * <p>
     * Given this ADD or MODIFY object delta OD, finds an item delta ID such that "ID has the same effect on an item specified
     * by itemPath as OD" (simply said).
     * <p>
     * More precisely,
     * - if OD is ADD delta: ID is ADD delta that adds values of the item present in the object being added
     * - if OD is MODIFY delta: ID is such delta that:
     * 1. Given ANY object O, let O' be O after application of OD.
     * 2. Let I be O(itemPath), I' be O'(itemPath).
     * 3. Then I' is the same as I after application of ID.
     * ID is null if no such item delta exists - or cannot be found easily.
     * <p>
     * Problem:
     * - If OD contains more than one modification that affects itemPath the results from findItemDelta can be differ
     * from the above definition.
     */
    <IV extends PrismValue, ID extends ItemDefinition<?>> ItemDelta<IV, ID> findItemDelta(ItemPath itemPath);

    <IV extends PrismValue, ID extends ItemDefinition<?>> ItemDelta<IV, ID> findItemDelta(ItemPath itemPath, boolean strict);

    <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>, DD extends ItemDelta<IV, ID>>
    DD findItemDelta(ItemPath itemPath, Class<DD> deltaType, Class<I> itemType, boolean strict);

    <IV extends PrismValue, ID extends ItemDefinition<?>> Collection<PartiallyResolvedDelta<IV, ID>> findPartial(
            ItemPath propertyPath);

    /**
     * Returns true if object delta contains delta for specified item.
     * This method only checks whether the delta is present,
     * it does not check whether real value of the item changed value in any way.
     * E.g. in case that there is a REPLACE delta for the same value, it returns true.
     *
     * Limitation: For OBJECT DELETE deltas, we do not know the values that were deleted.
     * In that case the method returns false for any path.
     */
    boolean hasItemDelta(ItemPath propertyPath);

    /**
     * Returns true if object delta contains delta for specified item, or any of its subitems.
     * This method only checks whether the delta is present,
     * it does not check whether real value of the item changed value in any way.
     * E.g. in case that there is a REPLACE delta for the same value, it returns true.
     *
     * Limitation: For OBJECT DELETE deltas, we do not know the values that were deleted.
     * In that case the method returns false for any path.
     */
    boolean hasItemOrSubitemDelta(ItemPath propertyPath);

    /**
     * Returns `true` if the delta may have an effect on the specified item.
     *
     * - For `ADD` deltas, returns `true` if the object has the specified item.
     * - For `DELETE` deltas, returns `false` (as we don't know the object).
     * - For `MODIFY` deltas, returns `true` if there is any overlap between the specified item and the modified items
     * (either exact match, sub- or super-path match).
     *
     * May not be quite precise when multivalued containers are involved, e.g. if we are asking about `assignment[1]/targetRef`
     * and the delta contains an addition of `assignment` with a different PCV ID, e.g. [2]. The result would be a false positive.
     */
    boolean hasRelatedDelta(ItemPath itemPath);

    /**
     * Returns true if the item identified by path was changed.
     * This method checks whether the real value of the item changed value in any way.
     * E.g. in case that there is a REPLACE delta for the same value as was the original value, it returns false.
     *
     * Limitation: This function relies on estimated old values,
     * therefore it may not be entirelly reliable in some situations.
     * For OBJECT DELETE deltas, we do not know the values that were deleted.
     * In that case the method returns false for any path.
     */
    boolean isItemChanged(ItemPath itemPath) throws SchemaException;

    boolean hasCompleteDefinition();

    Class<O> getObjectTypeClass();

    void setObjectTypeClass(Class<O> objectTypeClass);

    /**
     * Top-level path is assumed.
     */
    <X> PropertyDelta<X> findPropertyDelta(ItemPath parentPath, QName propertyName);

    <X> PropertyDelta<X> findPropertyDelta(ItemPath propertyPath);

    <X extends Containerable> ContainerDelta<X> findContainerDelta(ItemPath propertyPath);

    ReferenceDelta findReferenceModification(ItemPath itemPath);

    /**
     * Returns all item deltas at or below a specified path.
     */
    @NotNull Collection<? extends ItemDelta<?, ?>> findItemDeltasSubPath(ItemPath itemPath);

    void removeModification(ItemDelta<?, ?> itemDelta);

    void removeReferenceModification(ItemPath itemPath);

    void removeContainerModification(ItemPath itemName);

    void removePropertyModification(ItemPath itemPath);

    boolean isEmpty();

    void normalize();

    ObjectDelta<O> narrow(PrismObject<O> existingObject, @NotNull ParameterizedEquivalenceStrategy plusStrategy,
            @NotNull ParameterizedEquivalenceStrategy minusStrategy, boolean assumeMissingItems);

    // TODO better name
    void applyDefinitionIfPresent(PrismObjectDefinition<O> definition, boolean tolerateNoDefinition) throws SchemaException;

    /**
     * Deep clone.
     */
    ObjectDelta<O> clone();

    /**
     * Merge provided delta into this delta.
     * This delta is assumed to be chronologically earlier, delta in the parameter is assumed to come chronologicaly later.
     */
    void merge(ObjectDelta<O> deltaToMerge) throws SchemaException;

    void mergeModifications(Collection<? extends ItemDelta> modificationsToMerge) throws SchemaException;

    void mergeModification(ItemDelta<?, ?> modificationToMerge) throws SchemaException;

    /**
     * Applies this object delta to specified object, returns updated object.
     * It modifies the provided object.
     */
    void applyTo(PrismObject<O> targetObject) throws SchemaException;

    /**
     * Applies this object delta to specified object, returns updated object.
     * It leaves the original object unchanged.
     *
     * @param objectOld object before change
     * @return object with applied changes or null if the object should not exit (was deleted)
     */
    PrismObject<O> computeChangedObject(PrismObject<O> objectOld) throws SchemaException;

    /**
     * Incorporates the property delta into the existing property deltas
     * (regardless of the change type).
     */
    void swallow(ItemDelta<?, ?> newItemDelta) throws SchemaException;

    void swallow(List<ItemDelta<?, ?>> itemDeltas) throws SchemaException;

    <X> PropertyDelta<X> createPropertyModification(ItemPath path);

    <C> PropertyDelta<C> createPropertyModification(ItemPath path, PrismPropertyDefinition propertyDefinition);

    ReferenceDelta createReferenceModification(ItemPath path, PrismReferenceDefinition referenceDefinition);

    <C extends Containerable> ContainerDelta<C> createContainerModification(ItemPath path);

    <C extends Containerable> ContainerDelta<C> createContainerModification(ItemPath path,
            PrismContainerDefinition<C> containerDefinition);

    @SuppressWarnings("unchecked")
    <X> PropertyDelta<X> addModificationReplaceProperty(ItemPath propertyPath, X... propertyValues);

    @SuppressWarnings("unchecked")
    <X> void addModificationAddProperty(ItemPath propertyPath, X... propertyValues);

    @SuppressWarnings("unchecked")
    <X> void addModificationDeleteProperty(ItemPath propertyPath, X... propertyValues);

    @SuppressWarnings("unchecked")
    <C extends Containerable> void addModificationAddContainer(ItemPath propertyPath, C... containerables) throws SchemaException;

    @SuppressWarnings("unchecked")
    <C extends Containerable> void addModificationAddContainer(ItemPath propertyPath, PrismContainerValue<C>... containerValues);

    @SuppressWarnings("unchecked")
    <C extends Containerable> void addModificationDeleteContainer(ItemPath propertyPath, C... containerables) throws SchemaException;

    @SuppressWarnings("unchecked")
    <C extends Containerable> void addModificationDeleteContainer(ItemPath propertyPath,
            PrismContainerValue<C>... containerValues);

    @SuppressWarnings("unchecked")
    <C extends Containerable> void addModificationReplaceContainer(ItemPath propertyPath,
            PrismContainerValue<C>... containerValues);

    void addModificationAddReference(ItemPath path, PrismReferenceValue... refValues);

    void addModificationDeleteReference(ItemPath path, PrismReferenceValue... refValues);

    void addModificationReplaceReference(ItemPath path, PrismReferenceValue... refValues);

    ReferenceDelta createReferenceModification(ItemPath refPath);

    ObjectDelta<O> createReverseDelta() throws SchemaException;

    void checkConsistence();

    void checkConsistence(ConsistencyCheckScope scope);

    void checkConsistence(boolean requireOid, boolean requireDefinition, boolean prohibitRaw);

    void checkConsistence(boolean requireOid, boolean requireDefinition, boolean prohibitRaw, ConsistencyCheckScope scope);

    void assertDefinitions() throws SchemaException;

    void assertDefinitions(Supplier<String> sourceDescription) throws SchemaException;

    void assertDefinitions(boolean tolerateRawElements) throws SchemaException;

    /**
     * Assert that all the items has appropriate definition.
     */
    void assertDefinitions(boolean tolerateRawElements, Supplier<String> sourceDescription) throws SchemaException;

    void revive(PrismContext prismContext) throws SchemaException;

    void applyDefinition(@NotNull PrismObjectDefinition<O> objectDefinition, boolean force) throws SchemaException;

    boolean equivalent(ObjectDelta other);

    /**
     * Returns short string identification of object type. It should be in a form
     * suitable for log messages. There is no requirement for the type name to be unique,
     * but it rather has to be compact. E.g. short element names are preferred to long
     * QNames or URIs.
     */
    String toDebugType();

    static boolean isEmpty(ObjectDelta delta) {
        return delta == null || delta.isEmpty();
    }

    /**
     * Returns modifications that are related to the given paths; removes them from the original delta.
     * Applicable only to modify deltas.
     * Currently compares paths by "equals" predicate -- in the future we might want to treat sub/super/equivalent paths!
     * So consider this method highly experimental.
     */
    ObjectDelta<O> subtract(@NotNull Collection<ItemPath> paths);

    /**
     * Check if delta is redundant w.r.t. given object - i.e. if its application would have no visible effect on that object.
     */
    boolean isRedundant(PrismObject<O> object, @NotNull ParameterizedEquivalenceStrategy plusStrategy,
            @NotNull ParameterizedEquivalenceStrategy minusStrategy, boolean assumeMissingItems) throws SchemaException;

    @Experimental
    void removeOperationalItems();

    @Experimental
    void removeEstimatedOldValues();

    /** Checks if the delta is of given type (including subtypes). */
    default boolean isOfType(@NotNull Class<? extends Objectable> objectType) {
        var clazz = getObjectTypeClass();
        return clazz != null && objectType.isAssignableFrom(clazz);
    }

    class FactorOutResultMulti<T extends Objectable> {
        public final ObjectDelta<T> remainder;
        public final List<ObjectDelta<T>> offsprings = new ArrayList<>();

        public FactorOutResultMulti(ObjectDelta<T> remainder) {
            this.remainder = remainder;
        }
    }

    class FactorOutResultSingle<T extends Objectable> {
        public final ObjectDelta<T> remainder;
        public final ObjectDelta<T> offspring;

        public FactorOutResultSingle(ObjectDelta<T> remainder, ObjectDelta<T> offspring) {
            this.remainder = remainder;
            this.offspring = offspring;
        }
    }

    @NotNull
    FactorOutResultSingle<O> factorOut(Collection<? extends ItemPath> paths, boolean cloneDelta);

    @NotNull
    FactorOutResultMulti<O> factorOutValues(ItemPath path, boolean cloneDelta) throws SchemaException;

    /*
     * Some comments for modify deltas:
     *
     * Works if we are looking e.g. for modification to inducement item,
     * and delta contains e.g. REPLACE(inducement[1]/validTo, "...").
     *
     * Does NOT work the way around: if we are looking for modification to inducement/validTo and
     * delta contains e.g. ADD(inducement, ...). In such a case we would need to do more complex processing,
     * involving splitting value-to-be-added into remainder and offspring delta. It's probably doable,
     * but some conditions would have to be met, e.g. inducement to be added must have an ID.
     */

    /**
     * Checks if the delta tries to add (or set) a 'value' for the item identified by 'itemPath'.
     * If yes, it removes it.
     * <p>
     * TODO consider changing return value to 'incremental delta' (or null)
     *
     * @param dryRun only testing if value could be subtracted; not changing anything
     * @return true if the delta originally contained an instruction to add (or set) 'itemPath' to 'value'.
     */
    boolean subtract(@NotNull ItemPath itemPath, @NotNull PrismValue value, boolean fromMinusSet, boolean dryRun);

    @NotNull
    List<ItemPath> getModifiedItems();

    /**
     * Returns new values for specified item.
     * Only values that are explicitly added or replaced are present.
     * The returned values are taken from replace and add item deltas only.
     * Estimated old values are not used in this computation.
     */
    // Consider whether this method has a good name, as it is not a pure getter. It makes computation.
    // Probably something like determineNewValuesFor() would be better.
    List<PrismValue> getNewValuesFor(ItemPath itemPath);

    /**
     * Returns estimated new values for specified item.
     * This method computes the best estimate of the new values the item could have after delta is applied.
     * All (known) new values are returned, both those that were changed by the delta and those that were not.
     *
     * NOTE: This method provides just an estimate, based on estimated old values stored in the delta (if available).
     * Even if the estimated old values are present, results of delta application may differ,
     * e.g. if more than one delta was applied, or if the object have changed in the meantime.
     *
     * @return Estimated new values for specified item.
     *         Empty list is returned when it is estimated that the item will have no values after delta application
     *         (value is known, it is empty).
     *         Null is returned if the estimation method cannot determine the value (value is unknown).
     */
    // Designed for use in scripts
    @Nullable
    Collection<PrismValue> estimateNewValuesFor(ItemPath itemPath) throws SchemaException;

    /**
     * Returns estimated added values for specified item.
     * This method computes the best estimate of added values of the item.
     * Final (new) state of the values is presented, a state as it should look like after the delta is applied.
     * Only the values that were added by the delta are returned,
     * i.e. the values that were not present before the operation and are present after the operation.
     *
     * This method has "relativistic" character, as it present relative change of the values.
     * It is designed to be used primarily for multi-valued items.
     * This method may be inefficient and/or provide non-intuitive results when used on single-valued items.
     *
     * NOTE: This method provides just an estimate, based on estimated old values stored in the delta (if available).
     * Even if the estimated old values are present, results of delta application may differ,
     * e.g. if more than one delta was applied, or if the object have changed in the meantime.
     *
     * @return Estimated added values for specified item.
     *         Empty list is returned when it is estimated that the item will have no values after delta application
     *         (value is known, it is empty).
     *         Null is returned if the estimation method cannot determine the value (value is unknown).
     */
    // Designed for use in scripts
    @Nullable
    Collection<PrismValue> estimateAddedValuesFor(ItemPath itemPath) throws SchemaException;

    /**
     * Returns estimated modified values for specified item.
     * This method computes the best estimate of modified values of the item.
     * Final (new) state of the values is presented, a state as it should look like after the delta is applied.
     * Only the values that were modified by the delta are returned,
     * i.e. the values that were present before the operation and are present after the operation,
     * however their content is different.
     *
     * This method has "relativistic" character, as it present relative change of the values.
     * It is designed to be used primarily for complex multi-valued items (containers).
     * This method may be inefficient and/or provide non-intuitive results when used on single-valued items.
     *
     * NOTE: This method provides just an estimate, based on estimated old values stored in the delta (if available).
     * Even if the estimated old values are present, results of delta application may differ,
     * e.g. if more than one delta was applied, or if the object have changed in the meantime.
     *
     * @return Estimated modified values for specified item.
     *         Empty list is returned when it is estimated that the item will have no values after delta application
     *         (value is known, it is empty).
     *         Null is returned if the estimation method cannot determine the value (value is unknown).
     */
    // Designed for use in scripts
    @Nullable
    Collection<PrismValue> estimateModifiedValuesFor(ItemPath itemPath) throws SchemaException;

    /**
     * Returns estimated deleted values for specified item.
     * This method computes the best estimate of deleted values of the item.
     * Original (old) state of the values is presented, a state as they looked like before the delta was applied.
     * Only the values that were deleted by the delta are returned,
     * i.e. the values that were present before the operation and are supposed to be not present after the operation.
     *
     * This method has "relativistic" character, as it present relative change of the values.
     * It is designed to be used primarily for multi-valued items.
     * This method may be inefficient and/or provide non-intuitive results when used on single-valued items.
     *
     * NOTE: This method provides just an estimate, based on estimated old values stored in the delta (if available).
     * Even if the estimated old values are present, results of delta application may differ,
     * e.g. if more than one delta was applied, or if the object have changed in the meantime.
     *
     * LIMITATION: This method does not work reliably for object delete deltas.
     * In that case the delta does not contain any information regarding the items.
     *
     * @return Estimated deleted values for specified item.
     *         Empty list is returned when it is estimated that the item will have no values after delta application
     *         (value is known, it is empty).
     *         Null is returned if the estimation method cannot determine the value (value is unknown).
     */
    // Designed for use in scripts
    @Nullable
    Collection<PrismValue> estimateDeletedValuesFor(ItemPath itemPath) throws SchemaException;

    /**
     * Returns estimated changed values for specified item.
     * This method computes the best estimate of changed values of the item.
     * Final (new) state of the values is presented, a state as it should look like after the delta is applied.
     * Only the values that were changed by the delta are returned.
     *
     * This method has somehow "absolutists" character, as it present the "absolute" state of the changed values.
     * It is designed to be used primarily for single-valued items.
     * This method may be inefficient and/or provide non-intuitive results when used on multi-valued items.
     *
     * NOTE: This method provides just an estimate, based on estimated old values stored in the delta (if available).
     * Even if the estimated old values are present, results of delta application may differ,
     * e.g. if more than one delta was applied, or if the object have changed in the meantime.
     *
     * LIMITATION: This method does not work reliably for object delete deltas.
     * In that case the delta does not contain any information regarding the deleted items.
     *
     * @return Estimated changed values for specified item.
     *         Empty list is returned when it is estimated that the item will have no values after delta application
     *         (value is known, it is empty).
     *         Null is returned if the estimation method cannot determine the value (value is unknown).
     */
    // Designed for use in scripts
    @Nullable
    Collection<PrismValue> estimateChangedValuesFor(ItemPath itemPath) throws SchemaException;

    /**
     * Limitations:
     * (1) For DELETE object delta, we don't know what values were in the object's item.
     * (2) For REPLACE item delta, we don't know what values were in the object's item (but these deltas are quite rare
     * for multivalued items; and eventually there will be normalized into ADD+DELETE form)
     * (3) For DELETE item delta for PrismContainers, content of items deleted might not be known
     * (only ID could be provided on PCVs).
     */
    @SuppressWarnings("unused")
    // used from scripts
    List<PrismValue> getDeletedValuesFor(ItemPath itemPath);

    void clear();

    boolean isImmutable();

    /**
     * Creates {@link PrismValueDeltaSetTriple} (plus/minus/zero sets) for the value of {@link PrismObject}.
     *
     * Quite inefficient, as uses object cloning.
     */
    @Experimental
    PrismValueDeltaSetTriple<PrismObjectValue<O>> toDeltaSetTriple(PrismObject<O> objectOld) throws SchemaException;
}
