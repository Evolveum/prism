/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.util.CloneUtil;
import com.evolveum.midpoint.util.MiscUtil;

import com.evolveum.midpoint.util.annotation.OneUseOnly;
import com.evolveum.midpoint.util.annotation.Unused;

import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

import static com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy.REAL_VALUE_CONSIDER_DIFFERENT_IDS;
import static com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy.DEFAULT_FOR_EQUALS;

/**
 * Item is a common abstraction of Property, Reference and Container.
 * <p>
 * This is supposed to be a superclass for all items. Items are things
 * that can appear in containers, which generally means only a property, reference
 * and container itself. Therefore this is in fact superclass for those
 * three definitions.
 *
 * @author Radovan Semancik
 */
public interface Item<V extends PrismValue, D extends ItemDefinition<?>>
        extends Itemable, DebugDumpable, Visitable, PrismVisitable, PathVisitable, ParentVisitable, Serializable, Revivable,
        Freezable, Cloneable, ComplexCopyable<Item<V, D>> {

    String KEY_NAMESPACE_CONTEXT = PrismNamespaceContext.class.getSimpleName();

    /**
     * Returns applicable definition.
     * <p>
     * May return null if no definition is applicable or the definition is not known.
     *
     * @return applicable definition
     */
    @Override
    D getDefinition();

    /**
     * Returns true if this item and all contained items have definitions.
     */
    default boolean hasCompleteDefinition() {
        return getDefinition() != null;
    }

    /**
     * Returns the name of the item.
     * <p>
     * The name is a QName. It uniquely defines an item.
     * <p>
     * The name may be null, but such an item will not work.
     * <p>
     * The name is the QName of XML element in the XML representation.
     *
     * @return item name
     *
     * TODO consider making element name obligatory
     */
    @Override
    ItemName getElementName();

    /**
     * Sets the name of the item.
     * <p>
     * The name is a QName. It uniquely defines an item.
     * <p>
     * The name may be null, but such an item will not work.
     * <p>
     * The name is the QName of XML element in the XML representation.
     *
     * @param elementName the name to set
     *
     * TODO consider removing this method
     */
    void setElementName(QName elementName);

    /**
     * Sets applicable item definition.
     *
     * @param definition the definition to set
     */
    void setDefinition(@NotNull D definition);

    /**
     * Returns a display name for the item.
     * <p>
     * Returns null if the display name cannot be determined.
     * <p>
     * The display name is fetched from the definition. If no definition
     * (schema) is available, the display name will not be returned.
     *
     * @return display name for the item
     */
    default String getDisplayName() {
        return getDefinition() == null ? null : getDefinition().getDisplayName();
    }

    /**
     * Returns help message defined for the item.
     * <p>
     * Returns null if the help message cannot be determined.
     * <p>
     * The help message is fetched from the definition. If no definition
     * (schema) is available, the help message will not be returned.
     *
     * @return help message for the item
     */
    @Unused
    default String getHelp() {
        return getDefinition() == null ? null : getDefinition().getHelp();
    }

    /**
     * Flag that indicates incomplete item. If set to true then the
     * values in this item are not complete. If this flag is true
     * then it can be assumed that the object that this item represents
     * has at least one value. This is a method how to indicate that
     * the item really has some values, but are not here. This may
     * be used for variety of purposes. It may indicate that the
     * account has a password, but the password value is not revealed.
     * This may indicate that a user has a photo, but the photo was not
     * requested and therefore is not returned. This may be used to indicate
     * that only part of the attribute values were returned from the search.
     * And so on.
     *
     * *Behavior*: For single-valued items, the `incomplete` flag is cleared when a known value is set up
     * (via delta or Java API).
     *
     * See also https://docs.evolveum.com/midpoint/devel/design/incomplete-items-4.9.1/.
     */
    boolean isIncomplete();

    /**
     * Flags the item as incomplete.
     *
     * FIXME: Should be package-visible to implementation
     *
     * @param incomplete The new value
     *
     * @see Item#isIncomplete()
     */
    void setIncomplete(boolean incomplete);

    /**
     * Returns the parent of this item (if exists). Currently this has to be a PrismContainerValue.
     *
     * @return The parent if exists
     */
    @Nullable
    PrismContainerValue<?> getParent();

    /**
     * Sets the parent of this item.
     *
     * @param parentValue The new parent
     */
    void setParent(@Nullable PrismContainerValue<?> parentValue);

    /**
     * Returns the path of this item (sequence of names from the "root" container or similar object to this item).
     * Note that if the containing object is a delta (usually a container delta), then the path
     *
     * @return the path
     */
    @Override
    @NotNull
    ItemPath getPath();

    /**
     * Returns the namespace context for this item if available.
     * Namespace context is currently stored in user data with key
     *
     * Namespace context is stored only if {@link PrismParser#preserveNamespaceContext()} is flag was used during parsing
     *
     * @return The namespace context for this item if available.
     *
     * @see PrismParser#preserveNamespaceContext()
     * @see ParsingContext#isPreserveNamespaceContext()
     */
    @Nullable
    default PrismNamespaceContext getNamespaceContext() {
        return getUserData(KEY_NAMESPACE_CONTEXT);
    }

    /**
     * Returns the "user data", a map that allows attaching arbitrary named data to this item.
     * @return the user data map
     */
    @NotNull
    Map<String, Object> getUserData();

    /**
     * Returns the user data for the given key (name).
     */
    <T> T getUserData(String key);

    /**
     * Sets the user data for the given key (name).
     */
    void setUserData(String key, Object value);

    /**
     * Returns the values for this item. Although the ordering of this values is not important, and each value should
     * be present at most once, we currently return them as a list instead of a set.
     *
     * TODO reconsider this
     *
     * FIXME we should return immutable list, to avoid manipulating the values directly
     *
     * FIXME and maybe we should return List<? extends V> to avoid specializing the values, see ShadowAssociationValue in midPoint
     */
    @NotNull
    List<V> getValues();

    default Stream<V> valuesStream() {
        return getValues().stream();
    }

    /**
     * Returns the number of values for this item.
     */
    default int size() {
        return getValues().size();
    }

    /**
     * Returns any of the values. Usually called when we are quite confident that there is only a single value;
     * or we don't care which of the values we get. Does not create values if there are none.
     */
    default V getAnyValue() {
        return !getValues().isEmpty() ? getValues().get(0) : null;
    }

    /**
     * Returns the value, if there is only one. Throws exception if there are more values.
     * If there is no value, this method either:
     *
     * - returns null (for properties)
     * - throws an exception (for items that can hold multiple values)
     * - creates an empty value (for containers and references).
     *
     * TODO think again whether getOrCreateValue would not be better
     */
    V getValue();

    /**
     * Returns a value matching given selector (or null if none exists).
     */
    default V getAnyValue(@NotNull ValueSelector<V> selector) {
        return getValues().stream()
                .filter(selector)
                .findAny()
                .orElse(null);
    }

    /**
     * Returns the "real value" (content) of this item:
     *  - value contained in PrismPropertyValue
     *  - Referencable in PrismReferenceValue
     *  - Containerable in PrismContainerValue
     *  - Objectable in PrismObjectValue
     *
     * Note that the real value can contain operational items.
     *
     * It can also contain container IDs (although they are not considered to be part of the real value).
     *
     * It does not contain information about item element name nor other metadata like origin, definition, etc.
     * (Although e.g. Containerable can be converted back into PrismContainerValue that can be used to retrieve this information.)
     */
    @Nullable
    Object getRealValue();

    /**
     * Type override, also for compatibility.
     */
    <X> X getRealValue(Class<X> type);

    /**
     * Type override, also for compatibility.
     */
    @OneUseOnly("connectorConfiguration")
    <X> X[] getRealValuesArray(Class<X> type);

    /**
     * Returns (potentially empty) collection of "real values".
     *
     * The list itself is detached, freely modifiable.
     * (Note that the values can still embed a parent, e.g., for containers and references.)
     *
     * @see Item#getRealValue()
     */
    @NotNull
    Collection<?> getRealValues();

    /**
     * Returns detached collection of real values, although the values are still _connected_ to the original item
     * (in case of complex properties, references, and containers).
     *
     * BEWARE, it's not always possible to get the real values.
     */
    default <X> @NotNull Collection<X> getRealValues(Class<X> type) {
        return getRealValues().stream() // TODO should we avoid using streams here?
                .map(type::cast)
                .collect(Collectors.toList());
    }

    @OneUseOnly("Delta serialization")
    @Experimental // Do NOT use !!!!
    @NotNull
    default Collection<Object> getRealValuesOrRawTypes() {
        List<Object> rv = new ArrayList<>();
        for (V value : getValues()) {
            if (value != null) {
                rv.add(value.getRealValueOrRawType());
            } else {
                rv.add("null"); // fixme
            }
        }
        return rv;
    }

    /**
     * Returns true if the item contains 0 or 1 values and (by definition) is not multivalued.
     */
    default boolean isSingleValue() {
        // TODO what about dynamic definitions? See MID-3922
        D definition = getDefinition();
        if (definition != null) {
            if (definition.isMultiValue()) {
                return false;
            }
        }
        return getValues().size() <= 1;
    }

    default boolean isSingleValueByDefinition() {
        D definition = getDefinition();
        return definition != null && definition.isSingleValue();
    }

    //region Add and remove

    /**
     * Adds a given value, overwriting existing one.
     *
     * It compares values using DEFAULT_FOR_EQUALS (DATA) strategy, so it e.g. takes value metadata differences into account.
     * It is because this method is used during parsing, internal computations (typically using generated beans),
     * and similar situations where we expect little sophistication when it comes to value comparison.
     * The less surprises the better.
     */
    default boolean add(@NotNull V newValue) throws SchemaException {
        return add(newValue, DEFAULT_FOR_EQUALS);
    }

    /**
     * Adds a value, overwriting existing one(s). Uses specified equivalence strategy.
     *
     * @return true if this item changed as a result of the call. This is either during real value addition
     * or during overwriting existing value with a different one. The "difference" is taken using the
     * DEFAULT_FOR_EQUALS (DATA) equivalence strategy.
     */
    @OneUseOnly("convenience")
    boolean add(@NotNull V newValue, @NotNull EquivalenceStrategy strategy) throws SchemaException;

    /**
     * Adds a value, not looking for equivalent values. (This means that the new value is always added, if possible.)
     *
     * Note that we check the cardinality of the item according to its definition,
     * i.e. we do not allow single-valued item to contain more than one value.
     */
    ItemModifyResult<V> addIgnoringEquivalents(@NotNull V newValue) throws SchemaException;

    /**
     * Adds given values, with the same semantics as repeated add(..) calls.
     *
     * @return true if this item changed as a result of the call (i.e. if at least one value was really added)
     */
    default boolean addAll(Collection<V> newValues) throws SchemaException {
        return addAll(newValues, DEFAULT_FOR_EQUALS);
    }

    /**
     * Adds given values, with the same semantics as repeated add(..) calls.
     * For equality testing uses given strategy.
     *
     * @return true if this item changed as a result of the call (i.e. if at least one value was really added)
     */
    boolean addAll(Collection<V> newValues, @NotNull EquivalenceStrategy strategy) throws SchemaException;

    /**
     * Removes values equivalent to given value from the item.
     *
     * Note we use REAL_VALUE_CONSIDER_DIFFERENT_IDS strategy that ignores value metadata and operational
     * data. This may or may not be good! TODO reconsider
     */
    default boolean remove(V value) {
        return remove(value, REAL_VALUE_CONSIDER_DIFFERENT_IDS);
    }

    /**
     * Removes values equivalent to given value from the item; under specified equivalence strategy
     * OR when values represent the same value via "representsSameValue(.., lax=false)" method.
     *
     * @return true if this item changed as a result of the call (i.e. if at least one value was really removed)
     */
    boolean remove(V value, @NotNull EquivalenceStrategy strategy);

    default void removeIf(Predicate<V> predicate) {
        List<V> valuesToDelete = getValues().stream().filter(predicate).collect(Collectors.toList());
        for (V valueToDelete : valuesToDelete) {
            // We selected values directly from the item. So we can use rather strict equivalence strategy when deleting.
            remove(valueToDelete, EquivalenceStrategy.DATA);
        }
    }

    /**
     * Adds a value, respecting the metadata. I.e. if equivalent value exists, the metadata are merged.
     * (Replacing metadata of colliding provenance, adding all the others.)
     *
     * If a value is to be added as a whole, it is cloned.
     */
    ItemModifyResult<V> addRespectingMetadataAndCloning(V value, @NotNull EquivalenceStrategy strategy, EquivalenceStrategy metadataEquivalenceStrategy) throws SchemaException;

    /**
     * Removes values equivalent to given value from the item; under specified equivalence strategy
     * OR when values represent the same value via "representsSameValue(.., lax=false)" method.
     *
     * Respects metadata, i.e. if value to be removed has metadata specified, this method removes
     * only particular metadata. Only if this means that all metadata are gone, then the value is deleted.
     *
     * @return null if value was removed, otherwise value which was modified.
     */
    ItemModifyResult<V> removeRespectingMetadata(V value, @NotNull EquivalenceStrategy strategy, EquivalenceStrategy metadataEquivalenceStrategy);

    /**
     * Removes all given values from the item. It is basically a shortcut for repeated
     * {@link #remove(PrismValue, EquivalenceStrategy)} call.
     *
     * @return true if this item changed as a result of the call (i.e. if at least one value was really removed)
     */
    boolean removeAll(Collection<V> values, @NotNull EquivalenceStrategy strategy);

    /**
     * Removes all values from the item.
     */
    void clear();

    /**
     * Replaces all values of the item by given values.
     */
    void replaceAll(Collection<V> newValues, @NotNull EquivalenceStrategy strategy) throws SchemaException;

    /**
     * Replaces all values of the item by given value.
     */
    void replace(V newValue) throws SchemaException;

    //endregion

    //region Finding and comparing values

    /**
     * Compares this item to the specified object under DEFAULT_FOR_EQUALS (DATA) strategy.
     */
    @Override
    boolean equals(Object obj);

    /**
     * Compares this item to the specified object under given strategy.
     */
    boolean equals(Object obj, @NotNull EquivalenceStrategy equivalenceStrategy);

    /**
     * Compares this item to the specified object under given strategy.
     */
    boolean equals(Object obj, @NotNull ParameterizedEquivalenceStrategy equivalenceStrategy);

    /**
     * Computes hash code to be used under DEFAULT_FOR_EQUALS (currently DATA) equivalence strategy.
     */
    @Override
    int hashCode();

    /**
     * Computes hash code to be used under given equivalence strategy.
     */
    int hashCode(@NotNull EquivalenceStrategy equivalenceStrategy);

    /**
     * Computes hash code to be used under given equivalence strategy.
     */
    int hashCode(@NotNull ParameterizedEquivalenceStrategy equivalenceStrategy);

    /**
     * @return true if the item contains a given value (by default using DEFAULT_FOR_EQUALS
     * i.e. DATA strategy)
     *
     * Note that the "sameness" (ID-only value matching) is NOT considered here.
     */
    default boolean contains(@NotNull V value) {
        return findValue(value, DEFAULT_FOR_EQUALS) != null;
    }

    /**
     * @return true if the item contains a given value under specified equivalence strategy
     *
     * Note that the "sameness" (ID-only value matching) is NOT considered here.
     */
    default boolean contains(@NotNull V value, @NotNull EquivalenceStrategy strategy) {
        return findValue(value, strategy) != null;
    }

    /**
     * @return a value of this item that is equivalent to the given one under given equivalence strategy
     * (or null if no such value exists)
     */
    default V findValue(@NotNull V value, @NotNull EquivalenceStrategy strategy) {
        return findValue(value, strategy.prismValueComparator());
    }

    /**
     * @return a value of this item that is equivalent to the given one under given comparator
     * (or null if no such value exists)
     */
    default V findValue(V value, @NotNull Comparator<V> comparator) {
        return MiscUtil.findWithComparator(getValues(), value, comparator);
    }

    /**
     * Computes a difference (delta) with the specified item using DEFAULT_FOR_DELTA_APPLICATION
     * (REAL_VALUE_CONSIDER_DIFFERENT_IDS) equivalence strategy.
     *
     * Compares item values only -- does NOT dive into lower levels.
     */
    default ItemDelta<V,D> diff(Item<V,D> other) {
        return diff(other, ParameterizedEquivalenceStrategy.FOR_DELTA_ADD_APPLICATION);
    }

    /**
     * Computes a difference (delta) with the specified item using given equivalence strategy.
     * Note this method cannot accept general EquivalenceStrategy here; it needs the parameterized strategy.
     *
     * Compares item values only -- does NOT dive into lower levels.
     */
    ItemDelta<V,D> diff(Item<V,D> other, @NotNull ParameterizedEquivalenceStrategy strategy);

    //endregion

    default Collection<V> getClonedValues() {
        List<V> values = getValues();
        Collection<V> clonedValues = new ArrayList<>(values.size());
        for (V val: values) {
            //noinspection unchecked
            clonedValues.add((V)val.clone());
        }
        return clonedValues;
    }

    /** Currently doing nothing. */
    void normalize();

    /**
     * Merge all the values of other item to this item.
     */
    void merge(Item<V,D> otherItem) throws SchemaException;

    /**
     * Returns object (Item or PrismValue) pointed to by the given path.
     */
    Object find(ItemPath path);

    <IV extends PrismValue,ID extends ItemDefinition<?>> PartiallyResolvedItem<IV,ID> findPartial(ItemPath path);

    /**
     * Creates specific subclass of ItemDelta appropriate for type of item that this definition
     * represents (e.g. PropertyDelta, ContainerDelta, ...)
     */
    ItemDelta<V,D> createDelta();

    ItemDelta<V,D> createDelta(ItemPath path);

    /**
     * Accepts a visitor that visits each item/value on the way to the structure root.
     */
    @Override
    void acceptParentVisitor(@NotNull Visitor visitor);

    /**
     * Re-apply PolyString (and possible other) normalizations to the object.
     */
    void recomputeAllValues();

    default void filterValues(Function<V, Boolean> function) {
        Iterator<V> iterator = getValues().iterator();
        while (iterator.hasNext()) {
            Boolean keep = function.apply(iterator.next());
            if (keep == null || !keep) {
                iterator.remove();
            }
        }
    }

    default void filterYields(BiFunction<V, PrismContainerValue, Boolean> function) {
        Iterator<V> iterator = getValues().iterator();
        while (iterator.hasNext()) {
            V value = iterator.next();
            PrismContainer<Containerable> valueMetadata = value.getValueMetadataAsContainer();
            if (valueMetadata.hasNoValues()) {
                Boolean keep = function.apply(value, null);
                if (BooleanUtils.isNotTrue(keep)) {
                    iterator.remove();
                }
            } else {
                valueMetadata.getValues().removeIf(
                        md -> BooleanUtils.isNotTrue(function.apply(value, md)));
                if (valueMetadata.getValues().isEmpty()) {
                    iterator.remove();
                }
            }
        }
    }

    default void applyDefinition(@NotNull D definition) throws SchemaException {
        applyDefinition(definition, true);
    }

    default void applyDefinitionIfMissing(@NotNull D definition) throws SchemaException {
        applyDefinition(definition, false);
    }

    /**
     * Applies the definition to this item (and all its values, down to the lowest level).
     *
     * It may even convert the values, e.g. from their raw (unparsed) form to the parsed one,
     * or - for resource attributes - between {@link String} and {@link PolyString} values (due to the normalization).
     */
    void applyDefinition(@NotNull D definition, boolean force) throws SchemaException;

    /**
     * Literal clone.
     */
    @Deprecated // use copy(), mutableCopy() or immutableCopy() instead
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    default Item<V, D> clone() {
        return cloneComplex(CloneStrategy.LITERAL_MUTABLE);
    }

    @Deprecated // use immutableCopy(), although it's not exactly the same
    Item<V, D> createImmutableClone();

    /**
     * Complex clone with different cloning strategies.
     *
     * @see CloneStrategy
     */
    @NotNull Item<V, D> cloneComplex(@NotNull CloneStrategy strategy);

    /** A copy with no guarantees about mutability. */
    default @NotNull Item<V, D> copy() {
        return cloneComplex(CloneStrategy.LITERAL_ANY);
    }

    /** A literal copy that is guaranteed to be mutable. */
    default @NotNull Item<V, D> mutableCopy() {
        return cloneComplex(CloneStrategy.LITERAL_MUTABLE);
    }

    /** TODO define exact semantics of this regarding the parent - so, today it's safe to call this only on objects. */
    default @NotNull Item<V, D> immutableCopy() {
        return CloneUtil.immutableCopy(this);
    }

    static <T extends Item<?, ?>> Collection<T> cloneCollection(Collection<T> items) {
        Collection<T> clones = new ArrayList<>(items.size());
        for (T item: items) {
            //noinspection unchecked
            clones.add((T) item.mutableCopy());
        }
        return clones;
    }

    /**
     * Sets all parents to null. This is good if the items are to be "transplanted" into a
     * different Containerable.
     */
    @SuppressWarnings("unused")
    static <T extends Item<?, ?>> Collection<T> resetParentCollection(Collection<T> items) {
        for (T item: items) {
            item.setParent(null);
        }
        return items;
    }

    void checkConsistence(boolean requireDefinitions, ConsistencyCheckScope scope);

    void checkConsistence(boolean requireDefinitions, boolean prohibitRaw);

    void checkConsistence(boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope);

    void checkConsistence();

    void checkConsistence(ConsistencyCheckScope scope);

    void checkConsistenceInternal(Itemable rootItem, boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope);

    void assertDefinitions() throws SchemaException;

    void assertDefinitions(Supplier<String> sourceDescriptionSupplier) throws SchemaException;

    void assertDefinitions(boolean tolerateRawValues, Supplier<String> sourceDescriptionSupplier) throws SchemaException;

    /**
     * Returns true is all the values are raw.
     */
    default boolean isRaw() {
        for (V val: getValues()) {
            if (!val.isRaw()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true is at least one of the values is raw.
     */
    default boolean hasRaw() {
        for (V val: getValues()) {
            if (val.isRaw()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Normally the same as {@link #hasNoValues()}. But a container is considered empty also
     * if all its values (PCVs) are empty. This is a bit strange and should be revisited.
     */
    default boolean isEmpty() {
        return hasNoValues();
    }

    default boolean hasNoValues() {
        return getValues().isEmpty();
    }

    default boolean hasAnyValue() {
        return !getValues().isEmpty();
    }

    @SuppressWarnings("unused")
    static boolean hasNoValues(Item<?, ?> item) {
        return item == null || item.getValues().isEmpty();
    }

    /**
     * Returns true if this item is metadata item that should be ignored
     * for metadata-insensitive comparisons and hashCode functions.
     */
    default boolean isOperational() {
        D def = getDefinition();
        return def != null && def.isOperational();
    }

    @NotNull
    static <V extends PrismValue> Collection<V> getValues(Item<V, ?> item) {
        return item != null ? item.getValues() : Collections.emptySet();
    }

    /**
     * Returns all values corresponding to the provided path.
     *
     * The path may contain ambiguous segments (e.g. `assignment/targetRef` when there are more assignments).
     *
     * Limitations:
     *
     * . The path can contain only name segments and the "identifier" (`#`) segment - at least for now. No ID segments.
     * . There are no guarantees about duplicate values. They may or may not be present in the returned collections.
     * . The caller should not modify the returned collection in any way.
     *
     * Note to implementors: Please take care about the performance of this method.
     */
    @NotNull Collection<PrismValue> getAllValues(ItemPath path);

    /** See {@link #getAllValues(ItemPath)}. */
    static @NotNull Collection<PrismValue> getAllValues(Item<?, ?> item, ItemPath path) {
        return item != null ? item.getAllValues(path) : Collections.emptySet();
    }

    /**
     * Returns all items corresponding to the provided path.
     *
     * This is an analogue to {@link #getAllValues(ItemPath)}.
     * The only difference here is that `#` segment is not allowed, as it does not correspond to an item.
     *
     * (Shouldn't these methods be rather called findAllValues/findAllItems?)
     */
    @NotNull Collection<Item<?, ?>> getAllItems(@NotNull ItemPath path);

    Long getHighestId();

    @Override
    default boolean acceptVisitor(PrismVisitor visitor) {
        var ret = visitor.visit(this);
        if (ret) {
            valuesStream().forEach(v -> v.acceptVisitor(visitor));
        }
        return ret;
    }
}
