/*
 * Copyright (C) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

import static com.evolveum.midpoint.prism.CloneStrategy.LITERAL_ANY;
import static com.evolveum.midpoint.prism.CloneStrategy.LITERAL_MUTABLE;
import static com.evolveum.midpoint.util.MiscUtil.stateCheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.util.CloneUtil;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.SchemaLookup;
import com.evolveum.midpoint.util.annotation.Unused;
import com.evolveum.midpoint.util.exception.CommonException;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * @author semancik
 */
public interface PrismContainerValue<C extends Containerable> extends PrismValue, ParentVisitable, Walkable {

    static <T extends Containerable> T asContainerable(PrismContainerValue<T> value) {
        return value != null ? value.asContainerable() : null;
    }

    /**
     * Returns a collection of items that the property container contains.
     * The items may be properties or inner property containers.
     * <p>
     * Returned collection is mutable, but the caller should NOT modify it.
     * Instead - e.g. if it needs to remove values - it should call remove() method.
     *
     * @return collection of items that the property container contains.
     */
    @NotNull
    @Contract(pure = true)
    Collection<Item<?, ?>> getItems();

    int size();

    /**
     * Returns a set of properties that the property container contains.
     * <p>
     * Returned set is immutable! Any change to it will be ignored.
     * <p>
     * This method costs a bit, as the set of properties needs to be created.
     * Consider using other methods if possible.
     *
     * @return set of properties that the property container contains.
     */
    @NotNull
    @Unused
    Set<PrismProperty<?>> getProperties();

    Long getId();

    void setId(Long id);

    PrismContainerable<C> getParent();

    PrismContainer<C> getContainer();

    // For compatibility with other PrismValue types
    C getValue();

    @NotNull
    C asContainerable();

    Class<C> getCompileTimeClass();

    boolean canRepresent(Class<?> clazz);

    // returned class must be of type 'requiredClass' (or any of its subtypes)
    C asContainerable(Class<C> requiredClass);

    @NotNull
    Collection<QName> getItemNames();

    <IV extends PrismValue, ID extends ItemDefinition<?>> void add(Item<IV, ID> item) throws SchemaException;

    /**
     * Adds an item to a property container.
     *
     * @param item item to add.
     * @throws IllegalArgumentException an attempt to add value that already exists
     */
    <IV extends PrismValue, ID extends ItemDefinition<?>> void add(Item<IV, ID> item, boolean checkUniqueness) throws SchemaException;

    /**
     * Merges the provided item into this item. The values are joined together.
     * Returns true if new item or value was added.
     */
    <IV extends PrismValue, ID extends ItemDefinition<?>> boolean merge(Item<IV, ID> item) throws SchemaException;

    /**
     * Subtract the provided item from this item. The values of the provided item are deleted
     * from this item.
     * Returns true if this item was changed.
     */
    <IV extends PrismValue, ID extends ItemDefinition<?>> boolean subtract(Item<IV, ID> item) throws SchemaException;

    /**
     * Adds an item to a property container. Existing value will be replaced.
     *
     * @param item item to add.
     */
    <IV extends PrismValue, ID extends ItemDefinition<?>> void addReplaceExisting(Item<IV, ID> item) throws SchemaException;

    <IV extends PrismValue, ID extends ItemDefinition<?>> void remove(Item<IV, ID> item);

    void removeAll();

    /**
     * Adds a collection of items to a property container.
     *
     * @param itemsToAdd items to add
     * @throws IllegalArgumentException an attempt to add value that already exists
     */
    void addAll(Collection<? extends Item<?, ?>> itemsToAdd) throws SchemaException;

    /**
     * Adds a collection of items to a property container. Existing values will be replaced.
     *
     * @param itemsToAdd items to add
     */
    void addAllReplaceExisting(Collection<? extends Item<?, ?>> itemsToAdd) throws SchemaException;

    <IV extends PrismValue, ID extends ItemDefinition<?>> void replace(Item<IV, ID> oldItem, Item<IV, ID> newItem) throws SchemaException;

    void clear();

    // Avoid using because of performance penalty (it is faster to search by item name).
    // ... or reimplement ;)
    boolean contains(Item item);

    boolean contains(ItemName itemName);

    static <C extends Containerable> boolean containsRealValue(Collection<PrismContainerValue<C>> cvalCollection,
            PrismContainerValue<C> cval) {
        for (PrismContainerValue<C> colVal : cvalCollection) {
            if (colVal.equals(cval, EquivalenceStrategy.REAL_VALUE)) {
                return true;
            }
        }
        return false;
    }

    <IV extends PrismValue, ID extends ItemDefinition<?>> PartiallyResolvedItem<IV, ID> findPartial(ItemPath path);

    <X> PrismProperty<X> findProperty(ItemPath propertyPath);

    /**
     * Finds a specific property in the container by definition.
     * <p>
     * Returns null if nothing is found.
     *
     * @param propertyDefinition property definition to find.
     * @return found property or null
     */
    <X> PrismProperty<X> findProperty(PrismPropertyDefinition<X> propertyDefinition);

    <X extends Containerable> PrismContainer<X> findContainer(QName containerName);

    PrismReference findReference(QName elementName);

    <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findItem(ItemPath itemPath, Class<I> type);

//    <IV extends PrismValue,ID extends ItemDefinition> Item<IV,ID> findItem(String itemName);

    default <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> findItem(ItemPath itemPath) {
        //noinspection unchecked
        return (Item<IV, ID>) findItem(itemPath, Item.class);
    }

    <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findItem(ItemDefinition itemDefinition,
            Class<I> type);

    boolean containsItem(ItemPath propPath, boolean acceptEmptyItem) throws SchemaException;

    <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I createDetachedSubItem(QName name,
            Class<I> type, ID itemDefinition, boolean immutable) throws SchemaException, RemovedItemDefinitionException;

    <T extends Containerable> PrismContainer<T> findOrCreateContainer(QName containerName) throws SchemaException;

    PrismReference findOrCreateReference(QName referenceName) throws SchemaException;

    <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> findOrCreateItem(QName containerName) throws SchemaException;

    <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findOrCreateItem(QName containerName, Class<I> type) throws SchemaException;

    <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findOrCreateItem(ItemPath path, Class<I> type,
            ID definition) throws SchemaException;

    //    <X> PrismProperty<X> findOrCreateProperty(QName propertyQName) throws SchemaException;
//
    <X> PrismProperty<X> findOrCreateProperty(ItemPath propertyPath) throws SchemaException;

    <X> PrismProperty<X> findOrCreateProperty(PrismPropertyDefinition propertyDef) throws SchemaException;

    <X> PrismProperty<X> createProperty(QName propertyName) throws SchemaException;

    <X> PrismProperty<X> createProperty(PrismPropertyDefinition<X> propertyDefinition) throws SchemaException;

    /** Useful when removing something without the knowledge of its kind. */
    void removeItem(@NotNull ItemPath path);

    void removeProperty(ItemPath path);

    void removeContainer(ItemPath path);

    void removeReference(ItemPath path);

    <T> void setPropertyRealValue(QName propertyName, T realValue) throws SchemaException;

    <T> T getPropertyRealValue(QName propertyName, Class<T> type);

    default <T> T getItemRealValue(ItemName itemName, Class<T> type) {
        Item<?, ?> item = findItem(itemName);
        return item != null ? item.getRealValue(type) : null;
    }

    void recompute(PrismContext prismContext);

    @Override
    void accept(Visitor visitor);

    @Override
    void accept(Visitor visitor, ItemPath path, boolean recursive);

    /**
     * Returns true if all contained items have complete definitions.
     * (Currently does not check the definition of this value itself.)
     */
    boolean hasCompleteDefinition();

    boolean addRawElement(Object element) throws SchemaException;

    boolean deleteRawElement(Object element) throws SchemaException;

    boolean removeRawElement(Object element);

    PrismContainerValue<C> applyDefinition(@NotNull ItemDefinition<?> itemDefinition) throws SchemaException;

    PrismContainerValue<C> applyDefinition(@NotNull ItemDefinition<?> itemDefinition, boolean force) throws SchemaException;

    PrismContainerValue<C> applyDefinition(@NotNull PrismContainerDefinition<C> containerDef, boolean force) throws SchemaException;

    boolean isIdOnly();

    static boolean isIdOnly(PrismValue value) {
        return value instanceof PrismContainerValue && ((PrismContainerValue<?>) value).isIdOnly();
    }

    static Long getId(PrismValue value) {
        return value instanceof PrismContainerValue ? ((PrismContainerValue<?>) value).getId() : null;
    }

    static boolean idsMatch(PrismValue v1, PrismValue v2) {
        Long id1 = getId(v1);
        Long id2 = getId(v2);
        return id1 != null && id1.equals(id2);
    }

    void assertDefinitions(Supplier<String> sourceDescriptionSupplier) throws SchemaException;

    void assertDefinitions(boolean tolerateRaw, Supplier<String> sourceDescriptionSupplier) throws SchemaException;

    @Override
    PrismContainerValue<C> clone();

    @Override
    PrismContainerValue<C> createImmutableClone();

    @Override
    PrismContainerValue<C> cloneComplex(@NotNull CloneStrategy strategy);

    default PrismContainerValue<C> copy() {
        return cloneComplex(LITERAL_ANY);
    }

    default PrismContainerValue<C> mutableCopy() {
        return cloneComplex(LITERAL_MUTABLE);
    }

    default PrismContainerValue<C> immutableCopy() {
        return CloneUtil.immutableCopy(this);
    }

    boolean equivalent(PrismContainerValue<?> other);

    @Nullable
    ComplexTypeDefinition getComplexTypeDefinition();

    static <T extends Containerable> List<PrismContainerValue<T>> toPcvList(List<T> beans) {
        List<PrismContainerValue<T>> rv = new ArrayList<>(beans.size());
        for (T bean : beans) {
            //noinspection unchecked
            rv.add(bean.asPrismContainerValue());
        }
        return rv;
    }

    /**
     * Returns a single-valued container (with a single-valued definition) holding just this value.
     *
     * @param itemName Item name for newly-created container.
     */
    PrismContainer<C> asSingleValuedContainer(@NotNull QName itemName) throws SchemaException;

    // EXPERIMENTAL. TODO write some tests
    // BEWARE, it expects that definitions for items are present. Otherwise definition-less single valued items will get overwritten.
    void mergeContent(@NotNull PrismContainerValue<?> other, @NotNull List<QName> overwrite) throws SchemaException;

    @Override
    @NotNull PrismContainerValue<?> getRootValue();

    static <C extends Containerable> List<PrismContainerValue<C>> asPrismContainerValues(List<C> containerables) {
        //noinspection unchecked
        return containerables.stream()
                .map(c -> (PrismContainerValue<C>) c.asPrismContainerValue())
                .collect(Collectors.toList());
    }

    static <C extends Containerable> List<C> asContainerables(List<PrismContainerValue<C>> pcvs) {
        return pcvs.stream().map(c -> c.asContainerable()).collect(Collectors.toList());
    }

    static <C extends Containerable> Collection<C> asContainerables(Collection<PrismContainerValue<C>> pcvs) {
        return pcvs.stream().map(c -> c.asContainerable()).collect(Collectors.toList());
    }

    /**
     * Set origin type to all values and subvalues
     */
    void setOriginTypeRecursive(OriginType originType);

    // TODO optimize a bit + test thoroughly
    void keepPaths(List<? extends ItemPath> keep) throws SchemaException;

    // TODO optimize a bit + test thoroughly
    void removePaths(List<? extends ItemPath> remove) throws SchemaException;

    /**
     * Remove metadata from specified paths
     *
     * Can also remove metadata from the object on which it's called, if the paths contains a "root" path (e.g.
     * {@code ItemPath.fromString("/")}).
     *
     * @param pathsToRemoveMetadata the paths to items on which metadata should be removed.
     * @throws SchemaException
     */
    void removeMetadataFromPaths(List<? extends ItemPath> pathsToRemoveMetadata) throws SchemaException;

    void removeItems(List<? extends ItemPath> itemsToRemove);

    void removeOperationalItems();

    PrismContainerDefinition<C> getDefinition();

    void acceptParentVisitor(Visitor visitor);

    /**
     * Like isEmpty but ignores presence of container value ID.
     */
    boolean hasNoItems();

    /** For simplicity, everything must be qualified: names to check, and existing names. */
    default void checkNothingExceptFor(QName... allowedItemNames) {
        var actualItems = new HashSet<>(getItemNames());
        var expectedItems = Set.of(allowedItemNames);
        actualItems.removeAll(expectedItems);
        stateCheck(actualItems.isEmpty(), "Unexpected items in %s: %s", this, actualItems);
    }

    /** Used when accessing an item whose definition was removed. To be used only at very specific places! */
    class RemovedItemDefinitionException extends CommonException {
        @Override
        public String getErrorTypeMessage() {
            return "Removed item definition problem"; // irrelevant
        }
    }

    @Override
    default SchemaLookup schemaLookup() {
        if (getDefinition() != null) {
            return getDefinition().schemaLookup();
        }

        return PrismValue.super.schemaLookup();
    }

    @Override
    default boolean acceptVisitor(PrismVisitor visitor) {
        var ret = PrismValue.super.acceptVisitor(visitor);
        if (ret) {
            for (var item : getItems()) {
                item.acceptVisitor(visitor);
            }
        }
        return ret;
    }
}
