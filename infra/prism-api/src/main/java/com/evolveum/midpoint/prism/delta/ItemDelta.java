/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.delta;

import static com.evolveum.midpoint.prism.PrismValueCollectionsUtil.getRealValuesOfCollectionPreservingNull;
import static com.evolveum.midpoint.util.MiscUtil.emptyIfNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.util.MiscUtil;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.Foreachable;
import com.evolveum.midpoint.util.Processor;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.Nullable;

/**
 * Item Delta describes a change of an item which is a property, container or a reference.
 * It describes only a very small change - a change of a <i>single item</i>.
 * Therefore complex changes can only be described by using several item deltas together.
 * <p>
 * A group of item deltas is called <i>modifications</i> because they describe how an object
 * is modified (they cannot apply to add or delete object delta).
 * Item delta describes <i>values</i> that are being added, removed or replaced with respect to an item.
 * Therefore the item delta may also be of several types:
 * <ul>
 * <li><b>add</b> of new values. The values in item delta are added to the existing values. Existing values are left as they are.</li>
 * <li><b>delete</b> of existing values. The values in item delta are removed from the set of existing values. Other existing values are left as they are.</li>
 * <li><b>replace</b> of the values. All existing values are removed and all the values in item delta are added.</li>
 * </ul>
 * See <a href="https://docs.evolveum.com/midpoint/prism/deltas/">this document</a> for more.
 */
public interface ItemDelta<V extends PrismValue, D extends ItemDefinition<?>>
        extends Itemable, DebugDumpable, Visitable, PathVisitable, Foreachable<V>, Serializable, Freezable {

    ItemName getElementName();

    void setElementName(QName elementName);

    ItemPath getParentPath();

    void setParentPath(ItemPath parentPath);

    @NotNull
    @Override
    ItemPath getPath();

    D getDefinition();

    default boolean isOperational() {
        D definition = getDefinition();
        return definition != null && definition.isOperational();
    }

    @Override
    void accept(Visitor visitor);

    void accept(Visitor visitor, boolean includeOldValues);

    int size();

    // TODO think if estimated old values have to be visited as well
    @Override
    void accept(Visitor visitor, ItemPath path, boolean recursive);

    /** Note: this may change the implementation of the prism values in the delta. */
    void applyDefinition(@NotNull D definition) throws SchemaException;

    void applyTransformer(@NotNull Transformer<V, D> transformer) throws SchemaException;

    /** TODO do we need this method publicly? */
    void setDefinition(@NotNull D definition);

    boolean hasCompleteDefinition();

    Class<? extends Item> getItemClass();

    Collection<V> getValuesToAdd();

    default Collection<?> getRealValuesToAdd() {
        return getRealValuesOfCollectionPreservingNull(getValuesToAdd());
    }

    void clearValuesToAdd();

    Collection<V> getValuesToDelete();

    default Collection<?> getRealValuesToDelete() {
        return getRealValuesOfCollectionPreservingNull(getValuesToDelete());
    }

    void clearValuesToDelete();

    Collection<V> getValuesToReplace();

    default Collection<?> getRealValuesToReplace() {
        return getRealValuesOfCollectionPreservingNull(getValuesToReplace());
    }

    default Collection<V> getValues(@NotNull ModificationType currentSet) {
        return switch (currentSet) {
            case ADD -> getValuesToAdd();
            case DELETE -> getValuesToDelete();
            case REPLACE -> getValuesToReplace();
        };
    }

    /** Values that are added or potentially added by this delta. Returns live values; the collection may not be live. */
    default @NotNull Collection<V> getNewValues() {
        var valuesToReplace = getValuesToReplace();
        //noinspection ReplaceNullCheck
        if (valuesToReplace != null) {
            return valuesToReplace; // This is a REPLACE delta
        } else {
            return emptyIfNull(getValuesToAdd()); // This is an ADD/DELETE delta
        }
    }

    void clearValuesToReplace();

    void addValuesToAdd(Collection<V> newValues);

    void addValuesToAdd(V... newValues);

    void addValueToAdd(V newValue);

    void addValue(@NotNull ModificationType modification, @NotNull V newValue);

    /** Uses {@link EquivalenceStrategy#REAL_VALUE_CONSIDER_DIFFERENT_IDS} for value matching. */
    boolean removeValueToAdd(PrismValue valueToRemove);

    /** Uses {@link EquivalenceStrategy#REAL_VALUE_CONSIDER_DIFFERENT_IDS} for value matching. */
    boolean removeValueToDelete(PrismValue valueToRemove);

    /** Uses {@link EquivalenceStrategy#REAL_VALUE_CONSIDER_DIFFERENT_IDS} for value matching. */
    boolean removeValueToReplace(PrismValue valueToRemove);

    boolean removeValue(@NotNull ModificationType modification, @NotNull PrismValue valueToRemove);

    default boolean containsValueToAdd(V value, ParameterizedEquivalenceStrategy strategy) {
        return MiscUtil.findWithComparator(getValuesToAdd(), value, strategy.prismValueComparator()) != null;
    }

    default boolean containsValueToDelete(V value, ParameterizedEquivalenceStrategy strategy) {
        return MiscUtil.findWithComparator(getValuesToDelete(), value, strategy.prismValueComparator()) != null;
    }

    default boolean containsValueToReplace(V value, ParameterizedEquivalenceStrategy strategy) {
        return MiscUtil.findWithComparator(getValuesToReplace(), value, strategy.prismValueComparator()) != null;
    }

    void mergeValuesToAdd(Collection<V> newValues);

    void mergeValuesToAdd(V[] newValues);

    void mergeValueToAdd(V newValue);

    void addValuesToDelete(Collection<V> newValues);

    void addValuesToDelete(V... newValues);

    void addValueToDelete(V newValue);

    void mergeValuesToDelete(Collection<V> newValues);

    void mergeValuesToDelete(V[] newValues);

    void mergeValueToDelete(V newValue);

    void resetValuesToAdd();

    void resetValuesToDelete();

    void resetValuesToReplace();

    void setValuesToReplace(Collection<V> newValues);

    @SuppressWarnings("unchecked")
    void setValuesToReplace(V... newValues);

    /**
     * Sets empty value to replace. This efficiently means removing all values.
     */
    void setValueToReplace();

    void setValueToReplace(V newValue);

    void addValueToReplace(V newValue);

    void mergeValuesToReplace(Collection<V> newValues);

    void mergeValuesToReplace(V[] newValues);

    void mergeValueToReplace(V newValue);

    boolean isValueToAdd(V value);

    boolean isValueToDelete(V value);

    boolean isValueToReplace(V value);

    V getAnyValue();

    boolean isEmpty();

    static boolean isEmpty(ItemDelta<?, ?> itemDelta) {
        return itemDelta == null || itemDelta.isEmpty();
    }

    /**
     * The original semantics of {@link #isEmpty()} method: returns true
     * if all of values to add, delete, replace are null.
     *
     * TODO is this really needed?
     */
    boolean isLiterallyEmpty();

    boolean addsAnyValue();

    default boolean addsAnyValueMatching(Predicate<V> predicate) {
        return emptyIfNull(getValuesToAdd()).stream().anyMatch(predicate)
                || emptyIfNull(getValuesToReplace()).stream().anyMatch(predicate);
    }

    void foreach(Processor<V> processor);

    /**
     * Returns estimated state of the old value before the delta is applied.
     * This information is not entirely reliable. The state might change
     * between the value is read and the delta is applied. This is property
     * is optional and even if provided it is only for for informational
     * purposes.
     * <p>
     * If this method returns null then it should be interpreted as "I do not know".
     * In that case the delta has no information about the old values.
     * If this method returns empty collection then it should be interpreted that
     * we know that there were no values in this item before the delta was applied.
     *
     * @return estimated state of the old value before the delta is applied (may be null).
     */
    Collection<V> getEstimatedOldValues();

    void setEstimatedOldValues(Collection<V> estimatedOldValues);

    default void setEstimatedOldValuesWithCloning(Collection<V> estimatedOldValues) {
        var valuesToStore = new ArrayList<V>(estimatedOldValues.size());
        for (V originalValue : estimatedOldValues) {
            if (originalValue.isImmutable()) {
                // There will be a parent but in estimated old values it should not be a problem
                valuesToStore.add(originalValue);
            } else {
                //noinspection unchecked
                valuesToStore.add(
                        (V) originalValue.cloneComplex(CloneStrategy.LITERAL_IGNORING_EMBEDDED_OBJECTS_MUTABLE));
            }
        }
        setEstimatedOldValues(valuesToStore);
    }

    void addEstimatedOldValues(Collection<V> newValues);

    void addEstimatedOldValues(V... newValues);

    void addEstimatedOldValue(V newValue);

    void normalize();

    boolean isReplace();

    boolean isAdd();

    boolean isDelete();

    void clear();

    /**
     * Returns the narrowed delta that will have the same effect on the object as the current one.
     * <p>
     * We can skip deletion of vDel if there is no vEx ~ vDel (under minusComparator).
     * <p>
     * We can skip addition of vAdd if there is existing vEx ~ vAdd (under plusComparator).
     * But if we do that we must be sure
     * to skip deletion of all vDel ~ vAdd (under minusComparator).
     * Otherwise we would delete vDel but fail to add equivalent vAdd.
     * <p>
     * We can skip replacing of a set of values if and only if existing item has equivalent values under plusComparator.
     * <p>
     * This reasoning is bound to the actual application algorithm in ItemDeltaImpl.
     * But we should be aware that there are deltas that are applied by other code, e.g. those than are applied on a resource.
     *
     * @param plusComparator Comparator we want to use when determining skippability of values being added.
     * @param minusComparator Comparator we want to use when determining skippability of values being deleted.
     */
    ItemDelta<V, D> narrow(PrismObject<? extends Objectable> object,
            @NotNull Comparator<V> plusComparator, @NotNull Comparator<V> minusComparator, boolean assumeMissingItems);

    /**
     * Checks if the delta is redundant w.r.t. current state of the object.
     * I.e. if it changes the current object state.
     *
     * @param assumeMissingItems Assumes that some items in the object may be missing. So delta that replaces them by null
     */
    boolean isRedundant(PrismObject<? extends Objectable> object, ParameterizedEquivalenceStrategy strategy, boolean assumeMissingItems);

    void validate() throws SchemaException;

    void validate(String contextDescription) throws SchemaException;

    void validateValues(ItemDeltaValidator<V> validator) throws SchemaException;

    void validateValues(ItemDeltaValidator<V> validator, Collection<V> oldValues) throws SchemaException;

    void checkConsistence();

    void checkConsistence(ConsistencyCheckScope scope);

    void checkConsistence(boolean requireDefinition, boolean prohibitRaw, ConsistencyCheckScope scope);

    /**
     * Distributes the replace values of this delta to add and delete with
     * respect to provided existing values.
     */
    void distributeReplace(Collection<V> existingValues);

    /**
     * Merge specified delta to this delta. This delta is assumed to be
     * chronologically earlier, delta provided in the parameter is chronologically later.
     *
     * Both deltas must have the same path, i.e., they must refer to the same item.
     */
    void merge(ItemDelta<V, D> deltaToMerge);

    Collection<V> getValueChanges(PlusMinusZero mode);

    /**
     * Transforms the delta to the simplest (and safest) form. E.g. it will transform add delta for
     * single-value properties to replace delta.
     */
    void simplify();

    void applyTo(PrismContainerValue<?> containerValue) throws SchemaException;

    /** Applies this delta to given PCV; putting the values at `targetPath` (which must not be empty). */
    void applyTo(@NotNull PrismContainerValue<?> containerValue, @NotNull ItemPath targetPath) throws SchemaException;

    void applyTo(Item<?, ?> item) throws SchemaException;

    /**
     * Applies delta to item. Assumes that path of the delta and path of the item matches
     * (does not do path checks).
     */
    void applyToMatchingPath(Item item) throws SchemaException;

    ItemDelta<?, ?> getSubDelta(ItemPath path);

    boolean isApplicableTo(Item item);

    /**
     * Returns the "new" state of the property - the state that would be after
     * the delta is applied.
     */
    Item<V, D> getItemNew() throws SchemaException;

    /**
     * Returns the "new" state of the property - the state that would be after
     * the delta is applied.
     */
    Item<V, D> getItemNew(Item<V, D> itemOld) throws SchemaException;

    Item<V, D> getItemNewMatchingPath(Item<V, D> itemOld) throws SchemaException;

    /**
     * Returns true if the other delta is a complete subset of this delta.
     * I.e. if all the statements of the other delta are already contained
     * in this delta. As a consequence it also returns true if the two
     * deltas are equal.
     */
    boolean contains(ItemDelta<V, D> other);

    /**
     * Returns true if the other delta is a complete subset of this delta.
     * I.e. if all the statements of the other delta are already contained
     * in this delta. As a consequence it also returns true if the two
     * deltas are equal.
     */
    boolean contains(ItemDelta<V, D> other, EquivalenceStrategy strategy);

    void filterValues(Function<V, Boolean> function);

    void filterYields(BiFunction<V, PrismContainerValue, Boolean> function);

    ItemDelta<V, D> clone();

    ItemDelta<V, D> cloneWithChangedParentPath(ItemPath newParentPath);

    PrismValueDeltaSetTriple<V> toDeltaSetTriple(Item<V, D> itemOld) throws SchemaException;

    void assertDefinitions(Supplier<String> sourceDescriptionSupplier) throws SchemaException;

    void assertDefinitions(boolean tolerateRawValues, Supplier<String> sourceDescriptionSupplier) throws SchemaException;

    boolean isRaw();

    void revive(PrismContext prismContext) throws SchemaException;

    void applyDefinition(@NotNull D itemDefinition, boolean force) throws SchemaException;

    /**
     * Deltas are equivalent if they have the same result when
     * applied to an object. I.e. meta-data and other "decorations"
     * such as old values are not considered in this comparison.
     */
    boolean equivalent(ItemDelta other);

    @Override
    boolean equals(Object obj);

    @Override
    String toString();

    @Override
    String debugDump(int indent);

    void addToReplaceDelta();

    ItemDelta<V, D> createReverseDelta();

    V findValueToAddOrReplace(V value);

    /**
     * Set origin type to all values and subvalues
     */
    void setOriginTypeRecursive(OriginType originType);

    boolean isImmutable();

    /** Beware: approximate implementation, see the called method for details. */
    default boolean isMetadataRelated() {
        return getPath().isMetadataRelated();
    }

    /**
     * Returns set of actual modify operations that was performed, when delta was executed.
     *
     * @return null if apply results are unvavailable.
     */
    @Nullable
    default  Collection<ItemModifyResult<V>> applyResults() {
        return null;
    }

    interface Transformer<V extends PrismValue, D extends ItemDefinition<?>> {

        /** Transforms a given value; returns null if the value should be removed. */
        @Nullable V transformValue(@NotNull V value) throws SchemaException;
    }
}
