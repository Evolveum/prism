/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.delta.ItemMerger;
import com.evolveum.midpoint.prism.key.NaturalKeyDefinition;
import com.evolveum.midpoint.prism.lazy.FlyweightClonedValue;
import com.evolveum.midpoint.prism.path.InfraItemName;

import com.evolveum.midpoint.prism.util.PrismUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.ItemDefinitionTransformer.TransformableItem;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.impl.xnode.RootXNodeImpl;
import com.evolveum.midpoint.prism.marshaller.JaxbDomHack;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ItemPathCollectionsUtil;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.*;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;

/**
 * @author semancik
 */
public class PrismContainerValueImpl<C extends Containerable> extends PrismValueImpl implements PrismContainerValue<C> {

    public static final RuntimeException DIFFERENT_ITEMS_EXCEPTION = new ItemDifferentException();
    private static final boolean EARLY_EXIT = true;

    static {
        DIFFERENT_ITEMS_EXCEPTION.fillInStackTrace();
    }

    // We use linked map because we need to maintain the order internally to provide consistent
    // output in DOM and other ordering-sensitive representations.
    // The QNames here should be qualified if at all possible. Unqualified names are kept here nevertheless
    // (in order to maintain the ordering) but they are maintained in a separate set to know they require a separate
    // handling.
    protected final LinkedHashMap<QName, Item<?, ?>> items = new LinkedHashMap<>();
    protected final Set<String> unqualifiedItemNames = new HashSet<>();

    private Long id;

    private C containerable = null;

    // Definition of this value. Usually it is the same as CTD declared in the parent container.
    // However, in order to support polymorphism (as well as parent-less values) we distinguish between PC and PCV type definition.
    protected ComplexTypeDefinition complexTypeDefinition = null;

    public PrismContainerValueImpl() {
    }

    public PrismContainerValueImpl(C containerable) {
        this.containerable = containerable;
        getComplexTypeDefinition();
    }

    public PrismContainerValueImpl(
            OriginType type,
            Objectable source,
            PrismContainerable container,
            Long id,
            ComplexTypeDefinition complexTypeDefinition) {
        super(type, source, container);
        this.id = id;
        this.complexTypeDefinition = complexTypeDefinition;
    }

    public static <T extends Containerable> T asContainerable(PrismContainerValue<T> value) {
        return value != null ? value.asContainerable() : null;
    }

    /**
     * Returns a set of items that the property container contains. The items may be properties or inner property containers.
     * <p>
     * The set may be null. In case there are no properties an empty set is
     * returned.
     * <p>
     * Returned set is mutable. Live object is returned.
     *
     * @return set of items that the property container contains.
     */
    @Override
    @NotNull
    public Collection<Item<?, ?>> getItems() {
        if (isImmutable()) {
            return Collections.unmodifiableCollection(items.values());
        } else {
            return items.values();
        }
    }

    // avoid using because of performance penalty
    @SuppressWarnings("unchecked")
    public <I extends Item<?, ?>> List<I> getItems(Class<I> type) {
        List<I> rv = new ArrayList<>();
        for (Item<?, ?> item : items.values()) {
            if (type.isAssignableFrom(item.getClass())) {
                rv.add(((I) item));
            }
        }
        return rv;
    }

    @Override
    public int size() {
        return items.size();
    }

    /**
     * Returns a set of properties that the property container contains.
     * <p>
     * The set must not be null. In case there are no properties an empty set is
     * returned.
     * <p>
     * Returned set is immutable! Any change to it will be ignored.
     *
     * @return set of properties that the property container contains.
     */
    @Override
    @NotNull
    public Set<PrismProperty<?>> getProperties() {
        Set<PrismProperty<?>> properties = new HashSet<>();
        for (Item<?, ?> item : items.values()) {
            if (item instanceof PrismProperty) {
                properties.add((PrismProperty<?>) item);
            }
        }
        return properties;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        checkMutable();
        this.id = id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PrismContainerable<C> getParent() {
        Itemable parent = super.getParent();
        if (parent == null) {
            return null;
        }
        if (!(parent instanceof PrismContainerable)) {
            throw new IllegalStateException("Expected that parent of " + PrismContainerValue.class.getName() + " will be " +
                    PrismContainerable.class.getName() + ", but it is " + parent.getClass().getName());
        }
        return (PrismContainerable<C>) parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PrismContainer<C> getContainer() {
        Itemable parent = super.getParent();
        if (parent == null) {
            return null;
        }
        if (!(parent instanceof PrismContainer)) {
            throw new IllegalStateException("Expected that parent of " + PrismContainerValue.class.getName() + " will be " +
                    PrismContainer.class.getName() + ", but it is " + parent.getClass().getName());
        }
        return (PrismContainer<C>) super.getParent();
    }

    @Override
    @NotNull
    public ItemPath getPath() {
        Itemable parent = getParent();
        @NotNull ItemPath parentPath = ItemPath.EMPTY_PATH;
        if (parent != null) {
            parentPath = parent.getPath();
        }
        if (getId() != null) {
            return parentPath.append(getId());
        } else {
            return parentPath;
        }
    }

    @Override
    protected Object getPathComponent() {
        return getId();
    }

    // For compatibility with other PrismValue types
    @Override
    public C getValue() {
        return asContainerable();
    }

    @Override
    @NotNull
    public C asContainerable() {
        if (containerable != null) {
            return containerable;
        }
        if (getParent() == null && complexTypeDefinition == null) {
            throw new IllegalStateException("Cannot represent container value without a parent and complex type definition as containerable; value: " + this);
        }
        return asContainerableInternal(resolveClass(null));
    }

    @Override
    public Class<C> getCompileTimeClass() {
        if (containerable != null) {
            //noinspection unchecked
            return (Class<C>) containerable.getClass();
        } else {
            return resolveClass(null);
        }
    }

    @Override
    public boolean canRepresent(Class<?> clazz) {
        Class<C> compileTimeClass = getCompileTimeClass();
        if (compileTimeClass == null) {
            return false;
        }
        return clazz.isAssignableFrom(compileTimeClass);
    }

    // returned class must be of type 'requiredClass' (or any of its subtypes)
    @Override
    public C asContainerable(Class<C> requiredClass) {
        if (containerable != null) {
            return containerable;
        }
        return asContainerableInternal(resolveClass(requiredClass));
    }

    private Class<C> resolveClass(@Nullable Class<C> requiredClass) {
        if (complexTypeDefinition != null && complexTypeDefinition.getCompileTimeClass() != null) {
            Class<?> actualClass = complexTypeDefinition.getCompileTimeClass();
            if (requiredClass != null && !requiredClass.isAssignableFrom(actualClass)) {
                throw new IllegalStateException("asContainerable was called to produce " + requiredClass
                        + ", but the actual class in PCV is " + actualClass);
            } else {
                //noinspection unchecked
                return (Class<C>) actualClass;
            }
        } else {
            PrismContainerable<?> parent = getParent();
            if (parent != null) {
                Class<?> parentClass = parent.getCompileTimeClass();
                if (parentClass != null) {
                    if (requiredClass != null && !requiredClass.isAssignableFrom(parentClass)) {
                        // mismatch; but this can occur (see ShadowAttributesType vs ShadowIdentifiersType in association value)
                        // but TODO maybe this is only a workaround and the problem is in the schema itself (?)
                        return requiredClass;
                    } else {
                        //noinspection unchecked
                        return (Class<C>) parentClass;
                    }
                }
            }
        }
        return requiredClass;
    }

    @NotNull
    private C asContainerableInternal(Class<C> clazz) {
        if (clazz == null) {
            String elementName = getParent() != null ? String.valueOf(getParent().getElementName()) : String.valueOf(this);
            throw new SystemException("Unknown compile time class of container value of '" + elementName + "'.");
        }
        if (Modifier.isAbstract(clazz.getModifiers())) {
            throw new SystemException("Can't create instance of class '" + clazz.getSimpleName() + "', it's abstract.");
        }
        try {
            containerable = clazz.getDeclaredConstructor().newInstance();
            containerable.setupContainerValue(this);
            return containerable;
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SystemException("Couldn't create jaxb object instance of '" + clazz + "': " + ex.getMessage(), ex);
        }
    }

    @Override
    @NotNull
    public Collection<QName> getItemNames() {
        return new ArrayList<>(items.keySet());
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> void add(Item<IV, ID> item) throws SchemaException {
        add(item, true);
    }

    /**
     * Adds an item to a property container.
     *
     * @param item item to add.
     * @throws IllegalArgumentException an attempt to add value that already exists
     */
    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> void add(Item<IV, ID> item, boolean checkUniqueness) throws SchemaException {
        checkMutable();
        ItemName itemName = item.getElementName();
        if (itemName == null) {
            throw new IllegalArgumentException("Cannot add item without a name to value of container " + getParent());
        }
        if (checkUniqueness && findItem(itemName, Item.class) != null) {
            throw new IllegalArgumentException("Item " + itemName + " is already present in " + this.getClass().getSimpleName());
        }
        item.setParent(this);
        if (getComplexTypeDefinition() != null && item.getDefinition() == null) {
            ID definition = determineItemDefinition(itemName, getComplexTypeDefinition());
            if (definition instanceof RemovedItemDefinition) {
                throw new SchemaException("No definition for item " + itemName + " in " + getParent());
            }
            if (definition != null) {
                item.applyDefinitionIfMissing(definition);
            }
        }
        simpleAdd(item);
    }

    protected  <IV extends PrismValue, ID extends ItemDefinition<?>> void simpleAdd(Item<IV, ID> item) {
        @NotNull ItemName itemName = item.getElementName();
        items.put(itemName, item);
        if (QNameUtil.isUnqualified(itemName)) {
            unqualifiedItemNames.add(itemName.getLocalPart());
        }
    }

    /**
     * Merges the provided item into this item. The values are joined together.
     * Returns true if new item or value was added.
     */
    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> boolean merge(Item<IV, ID> item) throws SchemaException {
        checkMutable();
        Item<IV, ID> existingItem = findItem(item.getElementName(), Item.class);
        if (existingItem == null) {
            add(item);
            return true;
        } else {
            boolean changed = false;
            for (IV newVal : item.getValues()) {
                if (existingItem.add((IV) newVal.clone())) {
                    changed = true;
                }
            }
            if (item.isIncomplete()) {
                existingItem.setIncomplete(true);
            }
            return changed;
        }
    }

    /**
     * Subtract the provided item from this item. The values of the provided item are deleted
     * from this item.
     * Returns true if this item was changed.
     */
    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> boolean subtract(Item<IV, ID> item) throws SchemaException {
        checkMutable();
        Item<IV, ID> existingItem = findItem(item.getElementName(), Item.class);
        if (existingItem == null) {
            return false;
        } else {
            boolean changed = false;
            for (IV newVal : item.getValues()) {
                if (existingItem.remove(newVal)) {
                    changed = true;
                }
            }
            return changed;
        }
    }

    /**
     * Adds an item to a property container. Existing value will be replaced.
     *
     * @param item item to add.
     */
    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> void addReplaceExisting(Item<IV, ID> item) throws SchemaException {
        checkMutable();
        if (item == null) {
            return;
        }
        remove(item);
        add(item);
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> void remove(@NotNull Item<IV, ID> item) {
        checkMutable();

        Item<IV, ID> existingItem = findItem(item.getElementName(), Item.class);
        if (existingItem != null) {
            ItemName existingItemName = existingItem.getElementName();
            items.remove(existingItemName);
            removeFromUnqualifiedIfNeeded(existingItemName);
            existingItem.setParent(null);
        }
    }

    @Override
    public void removeAll() {
        checkMutable();
        Iterator<Item<?, ?>> iterator = items.values().iterator();
        while (iterator.hasNext()) {
            Item<?, ?> item = iterator.next();
            item.setParent(null);
            iterator.remove();
        }
        unqualifiedItemNames.clear();
    }

    /**
     * Adds a collection of items to a property container.
     *
     * @param itemsToAdd items to add
     * @throws IllegalArgumentException an attempt to add value that already exists
     */
    @Override
    public void addAll(Collection<? extends Item<?, ?>> itemsToAdd) throws SchemaException {
        for (Item<?, ?> item : itemsToAdd) {
            add(item);
        }
    }

    /**
     * Adds a collection of items to a property container. Existing values will be replaced.
     *
     * @param itemsToAdd items to add
     */
    @Override
    public void addAllReplaceExisting(Collection<? extends Item<?, ?>> itemsToAdd) throws SchemaException {
        checkMutable();
        for (Item<?, ?> item : itemsToAdd) {
            addReplaceExisting(item);
        }
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> void replace(Item<IV, ID> oldItem, Item<IV, ID> newItem) throws SchemaException {
        remove(oldItem);
        add(newItem);
    }

    @Override
    public void clear() {
        checkMutable();
        items.clear();
        unqualifiedItemNames.clear();
    }

    @Override
    public boolean contains(Item item) {
        return items.values().contains(item);
    }

    @Override
    public boolean contains(ItemName itemName) {
        return findItem(itemName) != null;
    }

    public static <C extends Containerable> boolean containsRealValue(Collection<PrismContainerValue<C>> cvalCollection,
            PrismContainerValue<C> cval) {
        for (PrismContainerValue<C> colVal : cvalCollection) {
            if (colVal.equals(cval, EquivalenceStrategy.REAL_VALUE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object find(ItemPath path) {
        if (path == null || path.isEmpty()) {
            return this;
        }
        Object first = path.first();
        if (!ItemPath.isName(first)) {
            throw new IllegalArgumentException("Attempt to lookup item using a non-name path " + path + " in " + this);
        }
        ItemName subName = ItemPath.toName(first);
        ItemPath rest = path.rest();
        Item<?, ?> subItem = findItem(subName);
        if (subItem == null) {
            return null;
        }
        return subItem.find(rest);
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> PartiallyResolvedItem<IV, ID> findPartial(ItemPath path) {
        if (path == null || path.isEmpty()) {
            // Incomplete path
            return null;
        }
        Object first = path.first();
        if (!ItemPath.isName(first)) {
            throw new IllegalArgumentException("Attempt to lookup item using a non-name path " + path + " in " + this);
        }
        ItemName subName = ItemPath.toName(first);
        ItemPath rest = path.rest();
        Item<?, ?> subItem = findItem(subName);
        if (subItem == null) {
            return null;
        }
        return subItem.findPartial(rest);
    }

    @Override
    public <X> PrismProperty<X> findProperty(ItemPath propertyPath) {
        return findItem(propertyPath, PrismProperty.class);
    }

    /**
     * Finds a specific property in the container by definition.
     * <p>
     * Returns null if nothing is found.
     *
     * @param propertyDefinition property definition to find.
     * @return found property or null
     */
    @Override
    public <X> PrismProperty<X> findProperty(PrismPropertyDefinition<X> propertyDefinition) {
        if (propertyDefinition == null) {
            throw new IllegalArgumentException("No property definition");
        }
        return findProperty(propertyDefinition.getItemName());
    }

    @Override
    public <X extends Containerable> PrismContainer<X> findContainer(QName containerName) {
        return findItem(ItemName.fromQName(containerName), PrismContainer.class);
    }

    @Override
    public PrismReference findReference(QName elementName) {
        return findItem(ItemName.fromQName(elementName), PrismReference.class);
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findItem(ItemPath itemPath, Class<I> type) {
        try {
            return findCreateItem(itemPath, type, null, false);
        } catch (SchemaException e) {
            // This should not happen
            throw new SystemException("Internal Error: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findCreateItem(QName itemName, Class<I> type, ID itemDefinition, boolean create) throws SchemaException {
        Item<IV, ID> item = findItemByQName(itemName);
        if (item != null) {
            if (type.isAssignableFrom(item.getClass())) {
                return (I) item;
            } else {
                if (create) {
                    throw new IllegalStateException("The " + type.getSimpleName() + " cannot be created because "
                            + item.getClass().getSimpleName() + " with the same name exists (" + item.getElementName() + ")");
                } else {
                    return null;
                }
            }
        }
        if (create) {   // todo treat unqualified names
            return createSubItem(itemName, type, itemDefinition);
        } else {
            return null;
        }
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findItem(ItemDefinition itemDefinition, Class<I> type) {
        if (itemDefinition == null) {
            throw new IllegalArgumentException("No item definition");
        }
        return findItem(itemDefinition.getItemName(), type);
    }

    @Override
    public boolean containsItem(ItemPath path, boolean acceptEmptyItem) throws SchemaException {
        if (!path.startsWithName()) {
            throw new IllegalArgumentException("Attempt to lookup item using a non-name path " + path + " in " + this);
        }
        QName subName = path.firstToName();
        ItemPath rest = path.rest();
        Item item = findItemByQName(subName);
        if (item != null) {
            if (rest.isEmpty()) {
                return acceptEmptyItem || !item.isEmpty();
            } else {
                // Go deeper
                if (item instanceof PrismContainer) {
                    return ((PrismContainer<?>) item).containsItem(rest, acceptEmptyItem);
                } else {
                    return acceptEmptyItem || !item.isEmpty();
                }
            }
        }

        return false;
    }

    // Expects that "self" path is NOT present in propPath
    @SuppressWarnings("unchecked")
    <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findCreateItem(ItemPath propPath, Class<I> type, ID itemDefinition, boolean create) throws SchemaException {
        Object first = propPath.first();
        if (!ItemPath.isItemOrInfraItem(first)) {
            throw new IllegalArgumentException("Attempt to lookup item using a non-name path '" + propPath + "' in " + this);
        }
        ItemName subName = ItemPath.toName(first);
        ItemPath rest = propPath.rest();
        I item = (I) findItemOrInfraItem(subName);
        if (item != null) {
            if (rest.isEmpty()) {
                if (type.isAssignableFrom(item.getClass())) {
                    return item;
                } else {
                    if (create) {
                        throw new SchemaException("The " + type.getSimpleName() + " cannot be created because "
                                + item.getClass().getSimpleName() + " with the same name exists (" + item.getElementName() + ")");
                    } else {
                        return null;
                    }
                }
            } else {
                // Go deeper
                if (item instanceof PrismContainer) {
                    return ((PrismContainer<?>) item).findCreateItem(rest, type, itemDefinition, create);
                } else {
                    if (create) {
                        throw new SchemaException("The " + type.getSimpleName() + " cannot be created because "
                                + item.getClass().getSimpleName() + " with the same name exists (" + item.getElementName() + ")");
                    } else {
                        // Return the item for non-container even if the path is non-empty
                        // FIXME: This is not the best solution but it is needed to be able to look inside properties
                        // such as PolyString

                        if (item instanceof PrismReference && rest.startsWithObjectReference()) {
                            var ref = ((PrismReference) item);
                            return ref.findReferencedItem(rest, type);
                        }
                        if (type.isAssignableFrom(item.getClass())) {
                            return item;
                        } else {
                            return null;
                        }
                    }
                }
            }
        }

        if (create) {       // todo treat unqualified names
            if (rest.isEmpty()) {
                return createSubItem(subName, type, itemDefinition);
            } else {
                // Go deeper
                PrismContainer<?> subItem = createSubItem(subName, PrismContainer.class, null);
                return subItem.findCreateItem(rest, type, itemDefinition, create);
            }
        } else {
            return null;
        }
    }

    private <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> findItemOrInfraItem(ItemName name) throws SchemaException {
        if (ItemPath.isIdentifier(name)) {
            // Normalize it to infra item name
            name = InfraItemName.ID;
        }
        if (name instanceof InfraItemName infra) {
            return findInfraItemName(infra);
        }
        return findItemByQName(name);
    }

    private <ID extends ItemDefinition<?>, IV extends PrismValue> Item<IV,ID> findInfraItemName(InfraItemName name) {
        if (InfraItemName.METADATA.equals(name)) {
            return (Item<IV,ID>) getValueMetadataAsContainer();
        }
        if (InfraItemName.ID.equals(name)) {
            return (Item<IV,ID>) idAsProperty();
        }
        return null;
    }

    private <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> findItemByQName(QName subName) throws SchemaException {
        // We assume that "unqualifiedItemNames" is empty most of the time. Hence, we do not want to spend time
        // calling .contains(..) method unnecessarily.
        if (QNameUtil.isUnqualified(subName) ||
                !unqualifiedItemNames.isEmpty() && unqualifiedItemNames.contains(subName.getLocalPart())) {
            // FIXME: PERFORMANCE: unqualifiedItemNames could be hash map (instead of hash set (which is actually hash map) - this would avoid full scan
            return findItemByQNameFullScan(subName);
        } else {
            //noinspection unchecked
            return (Item<IV, ID>) items.get(subName);
        }
    }

    private <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> findItemByQNameFullScan(QName subName) throws SchemaException {
//        LOGGER.warn("Full scan while finding {} in {}", subName, this);
        Item<IV, ID> matching = null;
        for (Item<?, ?> item : items.values()) {
            if (QNameUtil.match(subName, item.getElementName())) {
                if (matching != null) {
                    String containerName = getParent() != null ? DebugUtil.formatElementName(getParent().getElementName()) : "";
                    throw new SchemaException("More than one items matching " + subName + " in container " + containerName);
                } else {
                    matching = (Item<IV, ID>) item;
                }
            }
        }
        return matching;
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I createDetachedSubItem(QName name,
            Class<I> type, ID itemDefinition, boolean immutable) throws SchemaException, RemovedItemDefinitionException {
        I newItem = createDetachedNewItemInternal(name, type, itemDefinition, true);
        if (immutable) {
            newItem.freeze();
        }
        return newItem;
    }

    private <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I createSubItem(QName name, Class<I> type, ID itemDefinition) throws SchemaException {
        checkMutable();
        I newItem;
        try {
            newItem = createDetachedNewItemInternal(name, type, itemDefinition, false);
        } catch (RemovedItemDefinitionException e) {
            throw new SystemException("Unexpected exception: " + e.getMessage(), e);
        }
        add(newItem);
        return newItem;
    }

    private <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I createDetachedNewItemInternal(
            QName name, Class<I> type, ID itemDefinition, boolean treatRemovedDefinitions)
            throws SchemaException, RemovedItemDefinitionException {
        I newItem;
        if (itemDefinition == null) {
            ComplexTypeDefinition ctd = getComplexTypeDefinition();
            itemDefinition = determineItemDefinition(name, ctd);
            if (treatRemovedDefinitions && itemDefinition instanceof RemovedItemDefinition) {
                throw new RemovedItemDefinitionException();
            }
            if (ctd != null && itemDefinition == null || itemDefinition instanceof RemovedItemDefinition) {
                throw new SchemaException("No definition for item " + name + " in " + getParent());
            }
        }

        if (itemDefinition != null) {
            if (StringUtils.isNotBlank(name.getNamespaceURI())) {
                newItem = (I) itemDefinition.instantiate(name);
            } else {
                QName computed = new QName(itemDefinition.getItemName().getNamespaceURI(), name.getLocalPart());
                newItem = (I) itemDefinition.instantiate(computed);
            }
            if (newItem instanceof PrismObject) {
                throw new IllegalStateException("PrismObject instantiated as a subItem in " + this + " from definition " + itemDefinition);
            }
        } else {
            newItem = ItemImpl.createNewDefinitionlessItem(name, type);
            if (newItem instanceof PrismObject) {
                throw new IllegalStateException("PrismObject instantiated as a subItem in " + this + " as definitionless instance of class " + type);
            }
        }
        if (type.isAssignableFrom(newItem.getClass())) {
            return newItem;
        } else {
            throw new IllegalStateException(
                    "The new item '%s' of type %s could not be created because the provided definition produced an object of type %s; definition = %s"
                            .formatted(newItem.getElementName(), type.getSimpleName(), newItem.getClass().getSimpleName(), itemDefinition));
        }
    }

    @Override
    public <T extends Containerable> PrismContainer<T> findOrCreateContainer(QName containerName) throws SchemaException {
        return findCreateItem(containerName, PrismContainer.class, null, true);
    }

    @Override
    public PrismReference findOrCreateReference(QName referenceName) throws SchemaException {
        return findCreateItem(referenceName, PrismReference.class, null, true);
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> findOrCreateItem(QName containerName) throws SchemaException {
        return findCreateItem(containerName, Item.class, null, true);
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findOrCreateItem(QName containerName, Class<I> type) throws SchemaException {
        return findCreateItem(containerName, type, null, true);
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> I findOrCreateItem(ItemPath path, Class<I> type, ID definition) throws SchemaException {
        return findCreateItem(path, type, definition, true);
    }

    @Override
    public <X> PrismProperty<X> findOrCreateProperty(ItemPath propertyPath) throws SchemaException {
        return findOrCreateItem(propertyPath, PrismProperty.class, null);
    }

    @Override
    public <X> PrismProperty<X> findOrCreateProperty(PrismPropertyDefinition propertyDef) throws SchemaException {
        PrismProperty<X> property = findItem(propertyDef.getItemName(), PrismProperty.class);
        if (property != null) {
            return property;
        }
        return createProperty(propertyDef);
    }

    @Override
    public <X> PrismProperty<X> createProperty(QName propertyName) throws SchemaException {
        checkMutable();
        PrismPropertyDefinition<X> propertyDefinition = determineItemDefinition(propertyName, getComplexTypeDefinition());
        // Container has definition, but there is no property definition. This is either runtime schema or an error.
        if (propertyDefinition == null && getParent() != null && getDefinition() != null && !getDefinition().isRuntimeSchema()
                || propertyDefinition instanceof RemovedItemDefinition) {
            throw new IllegalArgumentException("No definition for property " + propertyName + " in " + complexTypeDefinition);
        }
        PrismProperty<X> property;
        if (propertyDefinition == null) {
            property = new PrismPropertyImpl<>(propertyName); // Definitionless
        } else {
            property = propertyDefinition.instantiate();
        }
        add(property, false);
        return property;
    }

    @Override
    public <X> PrismProperty<X> createProperty(PrismPropertyDefinition<X> propertyDefinition) throws SchemaException {
        PrismProperty<X> property = propertyDefinition.instantiate();
        add(property);
        return property;
    }

    @Override
    public void removeItem(@NotNull ItemPath path) {
        removeItem(path, Item.class);
    }

    @Override
    public void removeProperty(ItemPath path) {
        removeItem(path, PrismProperty.class);
    }

    @Override
    public void removeContainer(ItemPath path) {
        removeItem(path, PrismContainer.class);
    }

    @Override
    public void removeReference(ItemPath path) {
        removeItem(path, PrismReference.class);
    }

    // Expects that "self" path is NOT present in propPath
    <I extends Item<?, ?>> void removeItem(ItemPath itemPath, Class<I> itemType) {
        checkMutable();
        if (!itemPath.startsWithName()) {
            throw new IllegalArgumentException("Attempt to remove item using a non-name path " + itemPath + " in " + this);
        }
        QName subName = itemPath.firstToName();
        ItemPath rest = itemPath.rest();
        Iterator<Item<?, ?>> itemsIterator = items.values().iterator();
        while (itemsIterator.hasNext()) {
            Item<?, ?> item = itemsIterator.next();
            ItemName itemName = item.getElementName();
            if (subName.equals(itemName)) {
                if (!rest.isEmpty() && item instanceof PrismContainer) {
                    ((PrismContainer) item).removeItem(rest, itemType);
                    return;
                } else {
                    if (itemType.isAssignableFrom(item.getClass())) {
                        itemsIterator.remove(); // TODO we should also unset the respective parent
                        removeFromUnqualifiedIfNeeded(itemName);
                    } else {
                        throw new IllegalArgumentException("Attempt to remove item " + subName + " from " + this +
                                " of type " + itemType + " while the existing item is of incompatible type " + item.getClass());
                    }
                }
            }
        }
    }

    private void removeFromUnqualifiedIfNeeded(ItemName itemName) {
        if (QNameUtil.isUnqualified(itemName)) {
            removeUnqualifiedItemName(itemName);
        }
    }

    private void removeUnqualifiedItemName(ItemName itemName) {
        for (Item<?, ?> item : items.values()) {
            if (itemName.equals(item.getElementName())) {
                return;
            }
        }
        unqualifiedItemNames.remove(itemName.getLocalPart());
    }

    @Override
    public <T> void setPropertyRealValue(QName propertyName, T realValue) throws SchemaException {
        checkMutable();
        PrismProperty<T> property = findOrCreateProperty(ItemName.fromQName(propertyName));
        property.setRealValue(realValue);
    }

    @Override
    public <T> T getPropertyRealValue(QName propertyName, Class<T> type) {
        PrismProperty<T> property = findProperty(ItemName.fromQName(propertyName));
        if (property == null) {          // when using sql repo, even non-existing properties do not have 'null' here
            return null;
        }
        return property.getRealValue(type);
    }

    @Override
    public void recompute(PrismContext prismContext) {
        // Nothing to do. The subitems should be already recomputed as they are added to this container.
    }

    @Override
    public void accept(Visitor visitor) {
        super.accept(visitor);
        for (Item<?, ?> item : new ArrayList<>(items.values())) {     // to allow modifying item list via the acceptor
            item.accept(visitor);
        }
    }

    @Override
    public void accept(Visitor visitor, ItemPath path, boolean recursive) {
        if (path == null || path.isEmpty()) {
            // We are at the end of path, continue with regular visits all the way to the bottom
            if (recursive) {
                accept(visitor);
            } else {
                visitor.visit(this);
            }
        } else {
            Object first = path.first();
            if (!ItemPath.isName(first)) {
                throw new IllegalArgumentException("Attempt to lookup item using a non-name path " + path + " in " + this);
            }
            QName subName = ItemPath.toName(first);
            ItemPath rest = path.rest();
            for (Item<?, ?> item : items.values()) {            // todo unqualified names!
                if (subName.equals(item.getElementName())) {
                    item.accept(visitor, rest, recursive);
                }
            }
        }
    }

    @Override
    public void acceptParentVisitor(Visitor visitor) {
        visitor.visit(this);
        PrismContainerable<C> parent = getParent();
        if (parent != null) {
            parent.acceptParentVisitor(visitor);
        }
    }

    @Override
    public boolean hasCompleteDefinition() {
        for (Item<?, ?> item : getItems()) {
            if (!item.hasCompleteDefinition()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean representsSameValue(PrismValue other, EquivalenceStrategy strategy, boolean lax) {
        return other instanceof PrismContainerValue && representsSameValue((PrismContainerValue<C>) other, strategy, lax);
    }

    @SuppressWarnings("Duplicates")
    private boolean representsSameValue(PrismContainerValue<C> other, EquivalenceStrategy strategy, boolean lax) {
        if (lax && getParent() != null) {
            PrismContainerDefinition<?> definition = getDefinition();
            if (definition != null) {
                if (definition.isSingleValue()) {
                    // There is only one value, therefore it always represents the same thing
                    return true;
                }
            }
        }
        if (lax && other.getParent() != null) {
            PrismContainerDefinition<?> definition = other.getDefinition();
            if (definition != null) {
                if (definition.isSingleValue()) {
                    // There is only one value, therefore it always represents the same thing
                    return true;
                }
            }
        }

        if (strategy instanceof ParameterizedEquivalenceStrategy pes) {
            if (getId() != null && pes.isConsideringNaturalKeys() &&  equalNaturalKeys(other)) {
                // this is very specific situation, we have to have ID to be able to create child delta if
                // natural keys matches e.g. getId() != null avoids creating item paths with "null" ID segments,
                // or adding natural keys to item path and problems related with it

                return true;
            }
        }

        return this.getId() != null && this.getId().equals(other.getId());
    }



    private boolean equalNaturalKeys(PrismContainerValue<C> other) {
        Definition def = findDefinition(other);
        if (def == null) {
            return false;
        }

        NaturalKeyDefinition key = def.getNaturalKeyInstance();
        if (key != null && key.valuesMatch(this, other)) {
            return true;
        }

        if (def.getMergerIdentifier() != null) {
            ItemMerger merger = def.getMergerInstance(MergeStrategy.FULL, null);
            if (merger != null) {
                NaturalKeyDefinition k = merger.getNaturalKey();
                if (k != null && k.valuesMatch(this, other)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Definition findDefinition(PrismContainerValue<C> other) {
        PrismContainerDefinition def = getDefinition();
        if (def != null) {
            return def;
        }

        return other.getDefinition();
    }

    @Override
    public boolean diffMatchingRepresentation(PrismValue otherValue,
            Collection<? extends ItemDelta> deltas, ParameterizedEquivalenceStrategy strategy, boolean exitOnDiff) {
        if (otherValue instanceof PrismContainerValue) {
            return diffItems(this, (PrismContainerValue) otherValue, deltas, strategy, exitOnDiff);
        } else {
            throw new IllegalStateException("Comparing incompatible values " + this + " - " + otherValue);
        }
    }

    private void diffRepresentation(PrismContainerValue<C> otherValue,
            Collection<? extends ItemDelta> deltas, ParameterizedEquivalenceStrategy strategy) {
        diffItems(this, otherValue, deltas, strategy, false);
    }

    @Override
    public boolean isRaw() {
        return false;
    }

    @Override
    public boolean addRawElement(Object element) throws SchemaException {
        checkMutable();
        PrismContainerDefinition<C> definition = getDefinition();
        if (definition == null) {
            throw new UnsupportedOperationException("Definition-less containers are not supported any more.");
        } else {
            // We have definition here, we can parse it right now
            Item<?, ?> subitem = parseRawElement(element, definition);
            return merge(subitem);
        }
    }

    @Override
    public boolean deleteRawElement(Object element) throws SchemaException {
        checkMutable();
        PrismContainerDefinition<C> definition = getDefinition();
        if (definition == null) {
            throw new UnsupportedOperationException("Definition-less containers are not supported any more.");
        } else {
            // We have definition here, we can parse it right now
            Item<?, ?> subitem = parseRawElement(element, definition);
            return subtract(subitem);
        }
    }

    @Override
    public boolean removeRawElement(Object element) {
        checkMutable();
        throw new UnsupportedOperationException("Definition-less containers are not supported any more.");
    }

    private <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> parseRawElement(Object element, PrismContainerDefinition<C> definition) throws SchemaException {
        JaxbDomHack jaxbDomHack = ((PrismContextImpl) PrismContext.get()).getJaxbDomHack();
        return jaxbDomHack.parseRawElement(element, definition);
    }

    private boolean diffItems(PrismContainerValue<C> thisValue, PrismContainerValue<C> other,
            Collection<? extends ItemDelta> deltas, ParameterizedEquivalenceStrategy strategy, boolean exitOnDiff) {

        for (Item<?, ?> thisItem : thisValue.getItems()) {
            Item otherItem = other.findItem(thisItem.getElementName());
            if (!strategy.isConsideringOperationalData()) {
                ItemDefinition itemDef = thisItem.getDefinition();
                if (itemDef == null && other.getDefinition() != null) {
                    itemDef = other.getDefinition().findLocalItemDefinition(thisItem.getElementName());
                }
                if (shouldSkipForDiff(thisItem, itemDef)
                        && (otherItem == null || shouldSkipForDiff(otherItem, itemDef))) {
                    continue;
                }
            }
            // The "delete" delta will also result from the following diff
            boolean different = ((ItemImpl) thisItem).diffInternal(otherItem, deltas, false, strategy, exitOnDiff);
            if (different && exitOnDiff) {
                return true;
            }
        }

        for (Item otherItem : other.getItems()) {
            Item thisItem = thisValue.findItem(otherItem.getElementName());
            if (thisItem != null) {
                // Already processed in previous loop
                continue;
            }
            if (!strategy.isConsideringOperationalData()) {
                ItemDefinition itemDef = otherItem.getDefinition();
                if (itemDef == null && thisValue.getDefinition() != null) {
                    itemDef = thisValue.getDefinition().findLocalItemDefinition(otherItem.getElementName());
                }
                if (shouldSkipForDiff(otherItem, itemDef)) {
                    continue;
                }
            }

            // Other item has no values
            if (otherItem.hasNoValues()) {
                continue;
            }
            if (exitOnDiff) {
                return true;
            }

            ItemDelta itemDelta = otherItem.createDelta();

            itemDelta.addValuesToAdd(otherItem.getClonedValues());
            if (!itemDelta.isEmpty()) {
                ((Collection) deltas).add(itemDelta);
            }
        }
        return false;
    }

    /**
     * Returns true if the item is not operational or is not marked as alwaysUseForEquals.
     * It also checks sub-items.
     */
    private boolean shouldSkipForDiff(Item item, ItemDefinition itemDef) {
        // If the item is explicitly mentioned in schema to be used for equals
        // we do not consider it operational for diff purposes.
        if (getDefinition() != null && getDefinition().isAlwaysUseForEquals(item.getElementName())) {
            return false;
        }

        if (itemDef != null && itemDef.isOperational() && !itemDef.isAlwaysUseForEquals()) {
            return true;
        }
        if (item.isEmpty()) {
            return false;
        }
        if (!(item instanceof PrismContainer)) {
            return false;
        }
        PrismContainer<?> container = (PrismContainer) item;
        for (PrismContainerValue<?> cval : container.getValues()) {
            if (cval != null) {
                Collection<Item<?, ?>> subitems = cval.getItems();
                for (Item<?, ?> subitem : subitems) {
                    ItemDefinition subItemDef = subitem.getDefinition();
                    if (subItemDef == null && itemDef != null) {
                        subItemDef = ((PrismContainerDefinition) itemDef).findItemDefinition(subitem.getElementName());
                    }
                    if (subItemDef == null) {
                        return false;
                    }
                    if (!subItemDef.isOperational() || subItemDef.isAlwaysUseForEquals()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public PrismContainerDefinition<C> getDefinition() {
        return (PrismContainerDefinition<C>) super.getDefinition();
    }

    @Override
    public PrismContainerValue<C> applyDefinition(@NotNull ItemDefinition<?> definition) throws SchemaException {
        return applyDefinition(definition, true);
    }

    @Override
    public PrismContainerValue<C> applyDefinition(@NotNull ItemDefinition<?> definition, boolean force) throws SchemaException {
        checkMutable();
        if (!(definition instanceof PrismContainerDefinition)) {
            throw new IllegalArgumentException("Cannot apply " + definition + " to container " + this);
        }
        //noinspection unchecked
        return applyDefinition((PrismContainerDefinition<C>) definition, force);
    }

    @Override
    public PrismContainerValue<C> applyDefinition(@NotNull PrismContainerDefinition<C> containerDef, boolean force) throws SchemaException {
        checkMutable();
        ComplexTypeDefinition definitionToUse = containerDef.getComplexTypeDefinition();
        if (complexTypeDefinition != null) {
            if (!force) {
                return this; // there's a definition already
            }
            if (!complexTypeDefinition.getTypeName().equals(containerDef.getTypeName())) {
                // the second condition is a hack because e.g.
                // {http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/bundle/com.evolveum.icf.dummy/com.evolveum.icf.dummy.connector.DummyConnector}ConfigurationType
                // is clearly a runtime schema but it is _not_ marked as such (why?) -- see TestUcfDummy
                if (!definitionToUse.isRuntimeSchema() && definitionToUse.getCompileTimeClass() != null) {
                    // this is the case in which we are going to overwrite a specific definition
                    // (e.g. WfPrimaryChangeProcessorStateType) with a generic one (e.g. WfProcessorSpecificStateType)
                    // --> we should either skip this, or fetch the fresh definition from the prism context
                    ComplexTypeDefinition freshCtd = PrismContext.get().getSchemaRegistry()
                            .findComplexTypeDefinitionByType(complexTypeDefinition.getTypeName());
                    if (freshCtd != null) {
                        definitionToUse = freshCtd;
                    }
                } else {
                    // we are probably applying a dynamic definition over static one -- so, let's proceed
                }
            }
        }
        replaceComplexTypeDefinition(definitionToUse);

        // we need to continue even if CTD is null or 'any' - e.g. to resolve definitions within object extension
        applyDefinitionToItems(force);

        return definitionToUse.migrateIfNeeded(this);
    }

    private void applyDefinitionToItems(boolean force) throws SchemaException {
        // We change items during this operation, so we need to create a copy of them.
        ArrayList<Item<?, ?>> existingItems = new ArrayList<>(items.values());

        for (Item<?, ?> item : existingItems) {
            if (item.getDefinition() == null || force) {
                ItemDefinition<?> itemDefinition = determineItemDefinition(item.getElementName(), complexTypeDefinition);
                if (itemDefinition == null && item.getDefinition() != null && item.getDefinition().isDynamic()) {
                    // We will not apply the null definition here. The item has a dynamic definition that we don't
                    // want to destroy as it cannot be reconstructed later.
                } else if (item instanceof PrismProperty && itemDefinition instanceof PrismContainerDefinition) {
                    // Special case: we parsed something as (raw) property but later we found out it's in fact a container!
                    // (It could be also a reference but this will be implemented later.)
                    parseRawPropertyAsContainer((PrismProperty<?>) item, (PrismContainerDefinition<?>) itemDefinition);
                } else if (itemDefinition instanceof RemovedItemDefinition) {
                    // See MID-7939, this seems logical step - if definition was removed (e.g. for
                    // security reasons), let's remove the item too, so it's not available at all.
                    remove(item);
                } else if (itemDefinition != null) {
                    //noinspection unchecked,rawtypes
                    ((Item) item).applyDefinition(itemDefinition, force);
                }
            } else {
                // Item has a definition already, no need to apply it
            }
        }
    }

    private <C1 extends Containerable> void parseRawPropertyAsContainer(PrismProperty<?> property,
            PrismContainerDefinition<C1> containerDefinition) throws SchemaException {
        PrismContainer<C1> container = containerDefinition.instantiate();
        for (PrismPropertyValue<?> value : property.getValues()) {
            XNode rawElement = value.getRawElement();
            if (rawElement == null) {
                throw new IllegalStateException("Couldn't apply container definition (" + containerDefinition
                        + ") to a non-raw property value: " + value);
            } else {
                RootXNodeImpl rootXnode = new RootXNodeImpl(containerDefinition.getItemName(), rawElement);
                PrismValue parsedValue = PrismContext.get().parserFor(rootXnode).definition(containerDefinition).parseItemValue();
                if (parsedValue instanceof PrismContainerValue) {
                    //noinspection unchecked
                    container.add((PrismContainerValue<C1>) parsedValue);
                }
            }
        }
        remove(property);
        add(container);
    }

    /**
     * This method can both return null and throws exception. It returns null in case there is no definition
     * but it is OK (e.g. runtime schema). It throws exception if there is no definition and it is not OK.
     */
    @SuppressWarnings("unchecked")
    private <ID extends ItemDefinition> ID determineItemDefinition(QName itemName, @Nullable ComplexTypeDefinition ctd) throws SchemaException {
        ID itemDefinition = ctd != null ? ctd.findLocalItemDefinition(itemName) : null;
        if (itemDefinition != null) {
            return itemDefinition;
        }
        if (ctd == null || ctd.isXsdAnyMarker() || ctd.isRuntimeSchema()) {
            // Try to locate global definition. But even if that is not found it is still OK. This is runtime container.
            // We tolerate quite a lot here.
            PrismContext prismContext = PrismContext.get();
            if (prismContext != null) {
                return (ID) prismContext.getSchemaRegistry().resolveGlobalItemDefinition(itemName, ctd);
            } else {
                // Not initialized yet. Ignoring this.
                return null;
            }
        } else {
            if (ctd.isItemDefinitionRemoved(itemName)) {
                // This allows the caller to treat removed definition differently, if desired. See MID-7939.
                return (ID) new RemovedItemDefinition<>(itemName);
            }
            throw new SchemaException("No definition for item " + itemName + " in " + getParent() + "; type: " + ctd);
        }
    }

    @Override
    public void revive(PrismContext prismContext) {
        super.revive(prismContext);
        for (Item<?, ?> item : items.values()) {
            item.revive(prismContext);
        }
    }

    @Override
    public boolean isEmpty() {
        return id == null && hasNoItems();
    }

    @Override
    public boolean hasNoItems() {
        return items.isEmpty();
    }

    @Override
    public boolean isIdOnly() {
        return id != null && hasNoItems();
    }

    @Override
    public void normalize() {
//        checkMutable();
//        for (Item<?, ?> item : items.values()) {
//            item.normalize();
//        }
    }

    @Override
    public void checkConsistenceInternal(Itemable rootItem, boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope) {
        ItemPath myPath = getPath();
        if (requireDefinitions && getDefinition() == null) {
            throw new IllegalStateException("Definition-less container value " + this + " (" + myPath + " in " + rootItem + ")");
        }
        for (Item<?, ?> item : items.values()) {
            if (item == null) {
                throw new IllegalStateException(
                        "Null item in container value %s (%s in %s)".formatted(this, myPath, rootItem));
            }
            if (item.getParent() == null) {
                throw new IllegalStateException(
                        "No parent for item %s in container value %s (%s in %s)".formatted(item, this, myPath, rootItem));
            }
            if (item.getParent() != this) {
                throw new IllegalStateException(
                        "Wrong parent for item %s in container value %s (%s in %s), bad parent: %s".formatted(
                                item, this, myPath, rootItem, item.getParent()));
            }
            item.checkConsistenceInternal(rootItem, requireDefinitions, prohibitRaw, scope);
        }
    }

    @Override
    public void assertDefinitions(Supplier<String> sourceDescriptionSupplier) throws SchemaException {
        assertDefinitions(false, sourceDescriptionSupplier);
    }

    @Override
    public void assertDefinitions(boolean tolerateRaw, Supplier<String> sourceDescriptionSupplier)
            throws SchemaException {
        Supplier<String> itemSourceDescriptionSupplier = () -> "value(" + getId() + ") in " + sourceDescriptionSupplier.get();
        for (Item<?, ?> item : getItems()) {
            item.assertDefinitions(tolerateRaw, itemSourceDescriptionSupplier);
        }
    }

    @Override
    public PrismContainerValue<C> clone() {
        return cloneComplex(CloneStrategy.LITERAL_MUTABLE);
    }

    @Override
    public PrismContainerValue<C> createImmutableClone() {
        //noinspection unchecked
        return (PrismContainerValue<C>) super.createImmutableClone();
    }

    @Override
    public @NotNull PrismContainerValue<C> cloneComplex(@NotNull CloneStrategy strategy) {
        if (isImmutable() && !strategy.mutableCopy()) {
            return FlyweightClonedValue.from(this);
        }

        PrismContainerValueImpl<C> clone = new PrismContainerValueImpl<>(
                getOriginType(), getOriginObject(), getParent(), null, this.complexTypeDefinition);
        copyValues(strategy, clone);
        return clone;
    }

    protected void copyValues(CloneStrategy strategy, PrismContainerValueImpl<C> clone) {
        super.copyValues(strategy, clone);
        if (!strategy.ignoreContainerValueIds()) {
            clone.id = this.id;
        }
        for (Item<?, ?> item : this.items.values()) {
            Item<?, ?> clonedItem = item.cloneComplex(strategy);
            clonedItem.setParent(clone);
            clone.simpleAdd(clonedItem);
        }
    }

    void deepCloneDefinition(DeepCloneOperation operation, PrismContainerDefinition<C> clonedContainerDef) {
        // special treatment of CTD (we must not simply overwrite it with clonedPCD.CTD!)
        PrismContainerable<?> parent = getParent();
        if (parent != null && complexTypeDefinition != null) {
            if (complexTypeDefinition == parent.getComplexTypeDefinition()) {
                replaceComplexTypeDefinition(
                        clonedContainerDef.getComplexTypeDefinition());
            } else {
                replaceComplexTypeDefinition(
                        complexTypeDefinition.deepClone(operation));
            }
        }
        for (Item<?, ?> item : items.values()) {
            deepCloneDefinitionItem(operation, item, clonedContainerDef);
        }
    }

    private <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>>
    void deepCloneDefinitionItem(
            DeepCloneOperation operation,
            I item,
            PrismContainerDefinition<C> clonedContainerDef) {
        PrismContainerDefinition<C> oldContainerDef = getDefinition();
        ItemName itemName = item.getElementName();
        ID oldItemDefFromContainer = oldContainerDef.findLocalItemDefinition(itemName);
        ID oldItemDef = item.getDefinition();
        ID clonedItemDef;
        if (oldItemDef == null || oldItemDefFromContainer == oldItemDef) {
            clonedItemDef = clonedContainerDef.findItemDefinition(itemName);
        } else {
            //noinspection unchecked
            clonedItemDef = (ID) oldItemDef.deepClone(operation);
        }
        if (clonedItemDef != null) {
            // propagate to items in values
            //noinspection unchecked
            ((ItemImpl<?, ID>) item).propagateDeepCloneDefinition(operation, clonedItemDef);
            item.setDefinition(clonedItemDef); // sets CTD in values only if null!
        }
    }

    @Override
    public boolean equals(PrismValue other, @NotNull ParameterizedEquivalenceStrategy strategy) {
        return other instanceof PrismContainerValue<?> && equals((PrismContainerValue<?>) other, strategy);
    }

    private boolean equals(@NotNull PrismContainerValue<?> other, ParameterizedEquivalenceStrategy strategy) {
        if (strategy.isConsideringContainerIds()) {
            if (!Objects.equals(id, other.getId())) {
                return false;
            }
        } else if (strategy.isConsideringDifferentContainerIds()) {
            if (PrismValueUtil.differentIds(this, other)) {
                return false;
            }
        }
        // super.equals is called intentionally at the end, because it is quite expensive if metadata are present
        return equalsItems((PrismContainerValue<C>) other, strategy)
                && super.equals(other, strategy);
    }

    protected boolean equalsItems(PrismContainerValue<C> other, ParameterizedEquivalenceStrategy strategy) {
        Collection<? extends ItemDelta<?, ?>> deltas = FailOnAddList.INSTANCE;
        try {
            boolean different = diffItems(this, other, deltas, strategy, EARLY_EXIT);
            if (different) {
                return false;
            }
        } catch (ItemDifferentException e) {
            // Exception is only for fast escape
            return false;
        }
        return deltas.isEmpty();
    }

    @Override
    public boolean equivalent(PrismContainerValue<?> other) {
        return equals(other, EquivalenceStrategy.REAL_VALUE);
    }

    // TODO consider taking equivalence strategy into account
    @Override
    public int hashCode(@NotNull ParameterizedEquivalenceStrategy strategy) {
        final int prime = 31;
        int result = super.hashCode(strategy);
        // Do not include id. containers with non-null id and null id may still be considered equivalent
        // We also need to make sure that container valus that contain only metadata will produce zero hashcode
        // so it will not ruin hashcodes of parent containers
        int itemsHash;
        itemsHash = MiscUtil.unorderedCollectionHashcode(items.values(), item -> !item.isOperational());
        if (itemsHash != 0) {
            result = prime * result + itemsHash;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PCV(");
        sb.append(getId());
        sb.append("):");
        sb.append(getItems());
        if (isTransient()) {
            sb.append(", transient");
        }
        return sb.toString();
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        boolean wasIndent = false;
        if (DebugUtil.isDetailedDebugDump()) {
            DebugUtil.indentDebugDump(sb, indent);
            wasIndent = true;
            detailedDebugDumpStart(sb);
        }
        boolean multivalue = true;
        PrismContainerable<C> parent = getParent();
        if (parent != null && parent.getDefinition() != null) {
            multivalue = parent.getDefinition().isMultiValue();
        }
        wasIndent = dumpIdentifiers(sb, indent, wasIndent, multivalue);
        appendOriginDump(sb);
        wasIndent = appendExtraHeaderDump(sb, indent, wasIndent);
        Collection<Item<?, ?>> items = getItems();
        if (items.isEmpty()) {
            if (wasIndent) {
                sb.append("\n");
            }
            DebugUtil.indentDebugDump(sb, indent + 1);
            sb.append("(no items)");
        } else {
            Iterator<Item<?, ?>> i = getItems().iterator();
            if (wasIndent && i.hasNext()) {
                sb.append("\n");
            }
            while (i.hasNext()) {
                Item<?, ?> item = i.next();
                sb.append(item.debugDump(indent + 1));
                if (i.hasNext()) {
                    sb.append("\n");
                }
            }
        }
        var valueMetadata = getValueMetadata();
        if (!valueMetadata.isEmpty()) {
            sb.append("\n");
            DebugUtil.debugDumpWithLabel(sb, "value metadata", valueMetadata, indent + 1);
        }
        return sb.toString();
    }

    protected boolean appendExtraHeaderDump(StringBuilder sb, int indent, boolean wasIndent) {
        return wasIndent; // for extension in subclasses
    }

    // TODO fix this mess
    private boolean dumpIdentifiers(StringBuilder sb, int indent, boolean wasIndent, boolean multivalue) {
        if (this instanceof PrismObjectValue) {
            if (!wasIndent) {
                DebugUtil.indentDebugDump(sb, indent);
                wasIndent = true;
            }
            debugDumpIdentifiers(sb);
        } else {
            Long id = getId();
            if (multivalue || id != null || DebugUtil.isDetailedDebugDump()) {
                if (!wasIndent) {
                    DebugUtil.indentDebugDump(sb, indent);
                    wasIndent = true;
                }
                debugDumpIdentifiers(sb);
            }
        }
        return wasIndent;
    }

    protected void debugDumpIdentifiers(StringBuilder sb) {
        sb.append("id=").append(PrettyPrinter.prettyPrint(getId()));
    }

    protected void detailedDebugDumpStart(StringBuilder sb) {
        sb.append("PCV").append(": ");
    }

    @Override
    public String toHumanReadableString() {
        return "id=" + id + ": " + items.size() + " items";
    }

    @Override
    public QName getTypeName() {
        return getComplexTypeDefinition() != null ? getComplexTypeDefinition().getTypeName() : null;
    }

    @Override
    @Nullable
    public ComplexTypeDefinition getComplexTypeDefinition() {
        if (complexTypeDefinition == null) {
            complexTypeDefinition = determineComplexTypeDefinition();
        }
        return complexTypeDefinition;
    }

    // will correctly work only if argument is not null (otherwise the CTD will be determined on next call to getCTD)
    void replaceComplexTypeDefinition(ComplexTypeDefinition complexTypeDefinition) {
//        if (this.complexTypeDefinition != null && complexTypeDefinition != null && !this.complexTypeDefinition.getTypeName().equals(complexTypeDefinition.getTypeName())) {
//            System.out.println("Dangerous!");
//        }
        this.complexTypeDefinition = complexTypeDefinition;
    }

    private ComplexTypeDefinition determineComplexTypeDefinition() {
        PrismContainerable<C> parent = getParent();
        ComplexTypeDefinition parentCTD = parent != null && parent.getDefinition() != null ?
                parent.getDefinition().getComplexTypeDefinition() : null;
        if (containerable == null) {
            return parentCTD;
        }
        PrismContext prismContext = PrismContext.get();
        if (prismContext != null) {
            return prismContext.getSchemaRegistry()
                    .findComplexTypeDefinitionByCompileTimeClass(containerable.getClass()); // may be null at this place
        } else {
            // Ignoring this. We may be in midPoint initialization, called e.g. from a class initializer.
            return null;
        }
    }

    public static <T extends Containerable> List<PrismContainerValue<T>> toPcvList(List<T> beans) {
        List<PrismContainerValue<T>> rv = new ArrayList<>(beans.size());
        for (T bean : beans) {
            rv.add(bean.asPrismContainerValue());
        }
        return rv;
    }

    @Override
    public void performFreeze() {
        // Before freezing this PCV we should initialize it (if needed).
        // We assume that this object is NOT shared at this moment.
        if (getComplexTypeDefinition() != null && getComplexTypeDefinition().getCompileTimeClass() != null) {
            asContainerable();
        } else {
            // Calling asContainerable does not make sense anyway.
        }

        // And now let's freeze it; from the bottom up.
        for (Item<?, ?> item : items.values()) {
            item.freeze();
        }
        super.performFreeze();
    }

    @Override
    public Class<?> getRealClass() {
        if (containerable != null) {
            return containerable.getClass();
        }
        return resolveClass(null);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> T getRealValue() {
        return (T) asContainerable();
    }

    /**
     * Returns a single-valued container (with a single-valued definition) holding just this value.
     * If the value is immutable, the container will be immutable as well.
     * If the value is parent-less, it will be put right into the container.
     * Otherwise, the value will be cloned.
     *
     * *TODO* Consider moving this into some "util" class; it is not really in the spirit of prism.
     *
     * @param itemName Item name for newly-created container.
     */
    @Override
    public PrismContainer<C> asSingleValuedContainer(@NotNull QName itemName) throws SchemaException {
        ComplexTypeDefinition ctd = MiscUtil.stateNonNull(
                getComplexTypeDefinition(),
                "Cannot invoke 'asSingleValuedContainer' on a container value without CTD: %s", this);
        return PrismUtil.asSingleValuedContainer(itemName, this, ctd);
    }

    // EXPERIMENTAL. TODO write some tests
    // BEWARE, it expects that definitions for items are present. Otherwise definition-less single valued items will get overwritten.
    @Override
    @Experimental
    public void mergeContent(@NotNull PrismContainerValue<?> other, @NotNull List<QName> overwrite) throws SchemaException {
        List<ItemName> remainingToOverwrite = overwrite.stream().map(ItemName::fromQName).collect(Collectors.toList());
        for (Item<?, ?> otherItem : other.getItems()) {
            Item<?, ?> existingItem = findItem(otherItem.getElementName());
            if (QNameUtil.remove(remainingToOverwrite, otherItem.getElementName())
                    || existingItem != null && existingItem.isSingleValue()) {
                remove(existingItem);
            }
            merge(otherItem.clone());
        }
        remainingToOverwrite.forEach(name -> removeItem(name, Item.class));
    }

    @Override
    public @NotNull PrismContainerValue<?> getRootValue() {
        return (PrismContainerValue<?>) super.getRootValue();
    }

    public static <C extends Containerable> List<PrismContainerValue<C>> asPrismContainerValues(List<C> containerables) {
        //noinspection unchecked
        return containerables.stream()
                .map(c -> (PrismContainerValue<C>) c.asPrismContainerValue())
                .collect(Collectors.toList());
    }

    public static <C extends Containerable> List<C> asContainerables(List<PrismContainerValue<C>> pcvs) {
        return pcvs.stream().map(c -> c.asContainerable()).collect(Collectors.toList());
    }

    public static <C extends Containerable> Collection<C> asContainerables(Collection<PrismContainerValue<C>> pcvs) {
        return pcvs.stream().map(c -> c.asContainerable()).collect(Collectors.toList());
    }

    /**
     * Set origin type to all values and subvalues
     */
    @Override
    public void setOriginTypeRecursive(final OriginType originType) {
        accept((visitable) -> {
            if (visitable instanceof PrismValue) {
                ((PrismValue) visitable).setOriginType(originType);
            }
        });
    }

    // TODO optimize a bit + test thoroughly
    @Override
    public void keepPaths(List<? extends ItemPath> keep) throws SchemaException {
        Collection<QName> itemNames = getItemNames();
        for (QName itemName : itemNames) {
            Item<?, ?> item = findItemByQName(itemName);
            ItemPath itemPath = item.getPath().removeIds();
            if (!ItemPathCollectionsUtil.containsSuperpathOrEquivalent(keep, itemPath)
                    && !ItemPathCollectionsUtil.containsSubpathOrEquivalent(keep, itemPath)) {
                removeItem(ItemName.fromQName(itemName), Item.class);
            } else {
                if (item instanceof PrismContainer) {
                    for (PrismContainerValue<?> v : ((PrismContainer<?>) item).getValues()) {
                        v.keepPaths(keep);
                    }
                } else {
                    // TODO some additional checks here (e.g. when trying to keep 'name/xyz' - this is illegal)
                }
            }

        }
    }

    @Override
    public void removePaths(List<? extends ItemPath> pathsToRemove) throws SchemaException {
        walk((path, consumed) -> !consumed && ItemPathCollectionsUtil.containsSuperpath(pathsToRemove, path),
                path -> ItemPathCollectionsUtil.containsEquivalent(pathsToRemove, path),
                item -> {
                    if (item.getParent() != null) {
                        item.getParent().removeItem(ItemName.fromQName(item.getElementName()));
                    }
                });
    }

    @Override
    public void removeMetadataFromPaths(List<? extends ItemPath> pathsToRemoveMetadata)
            throws SchemaException {
        if (ItemPathCollectionsUtil.containsEquivalent(pathsToRemoveMetadata, this.getPath())) {
            this.deleteValueMetadata();
        }
        walk((path, consumed) -> ItemPathCollectionsUtil.containsSuperpath(pathsToRemoveMetadata, path),
                path -> ItemPathCollectionsUtil.containsEquivalent(pathsToRemoveMetadata, path),
                item -> item.getValues().forEach(PrismValue::deleteValueMetadata));
    }

    @Override
    public void walk(BiPredicate<? super ItemPath, Boolean> descendPredicate,
            Predicate<? super ItemPath> consumePredicate, Consumer<? super Item<?, ?>> itemConsumer)
            throws SchemaException {
        final Collection<QName> itemNames = getItemNames();
        for (QName itemName : itemNames) {
            final Item<?, ?> item = findItemByQName(itemName);
            final ItemPath itemPath = item.getPath().removeIds();
            final boolean consume = consumePredicate.test(itemPath);
            if (consume) {
                itemConsumer.accept(item);
            }
            if (descendPredicate.test(itemPath, consume)) {
                if (item instanceof PrismContainer<?> container) {
                    for (PrismContainerValue<?> v : container.getValues()) {
                        v.walk(descendPredicate, consumePredicate, itemConsumer);
                    }
                }
            }
        }
    }

    // Removes all unused definitions, in order to conserve heap. Assumes that the definition is not shared. Use with care!
    void trimItemsDefinitionsTrees(Collection<? extends ItemPath> alwaysKeep) {
        // to play safe, we won't touch PCV-specific complexTypeDefinition
        for (Item<?, ?> item : items.values()) {
            if (item instanceof PrismContainer) {
                Collection<ItemPath> alwaysKeepInSub = ItemPathCollectionsUtil.remainder(CollectionUtils.emptyIfNull(alwaysKeep),
                        item.getElementName(), false);
                ((PrismContainer<?>) item).trimDefinitionTree(alwaysKeepInSub);
            }
        }
    }

    @Override
    public @NotNull Collection<PrismValue> getAllValues(ItemPath path) {
        if (path.isEmpty()) {
            return List.of(this);
        }
        Object first = path.first();
        if (ItemPath.isIdentifier(first)) {
            return List.of(new PrismPropertyValueImpl<>(getIdentifier()));
        } else if (ItemPath.isName(first)) {
            //noinspection unchecked
            Item<?, ?> item = findItem(path.firstToName(), Item.class);
            if (item == null) {
                return List.of();
            } else {
                return item.getAllValues(path.rest());
            }
        } else {
            throw new IllegalArgumentException("Item path does not start with a name nor with '#': " + path);
        }
    }

    @Override
    public @NotNull Collection<Item<?, ?>> getAllItems(@NotNull ItemPath path) {
        assert !path.isEmpty();
        Object first = path.first();
        if (ItemPath.isName(first)) {
            //noinspection unchecked
            Item<?, ?> item = findItem(path.firstToName(), Item.class);
            if (item == null) {
                return List.of();
            } else {
                return item.getAllItems(path.rest());
            }
        } else {
            throw new IllegalArgumentException("Item path does not start with a name: " + path);
        }
    }

    /**
     * Returns the value of identifier corresponding to the '#' path: container id or object oid.
     */
    public Object getIdentifier() {
        return id;
    }

    // BEWARE!! Assumes the container has no parent! Otherwise item.getPath() provides wrong values.
    @Override
    public void removeItems(List<? extends ItemPath> itemsToRemove) {
        for (ItemPath itemToRemove : itemsToRemove) {
            Item<?, ?> item = findItem(itemToRemove); // reduce to "removeItem" after fixing that method implementation
            if (item != null) {
                removeItem(item.getPath(), Item.class);
            }
        }
    }

    @Override
    public void removeOperationalItems() {
        accept(visitable -> {
            if (visitable instanceof Item) {
                Item<?, ?> item = ((Item<?, ?>) visitable);
                if (item.getDefinition() != null && item.getDefinition().isOperational()) {
                    PrismContainerValue<?> parent = item.getParent();
                    if (parent != null) { // should be the case
                        parent.remove(item);
                    }
                }
            }
        });
    }

    @Override
    public void transformDefinition(ComplexTypeDefinition parentDef, ItemDefinition<?> itemDef,
            ItemDefinitionTransformer transformation) {

        ComplexTypeDefinition newDefinition = transformation.applyValue(parentDef, itemDef, complexTypeDefinition);
        if (newDefinition != null && newDefinition != complexTypeDefinition) {
            replaceComplexTypeDefinition(newDefinition);
        }

        for (Item<?, ?> item : items.values()) {
            if (item instanceof TransformableItem) {
                ((TransformableItem) item).transformDefinition(complexTypeDefinition, transformation);
            }
        }
    }

    private static class FailOnAddList extends AbstractList<ItemDelta<?, ?>> {

        public static final FailOnAddList INSTANCE = new FailOnAddList();

        @Override
        public ItemDelta<?, ?> get(int index) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean add(ItemDelta<?, ?> e) {
            throw DIFFERENT_ITEMS_EXCEPTION;
        }
    }

    private static class ItemDifferentException extends RuntimeException {

        private static final long serialVersionUID = 1L;

    }

    private static class RemovedItemDefinition<I extends Item<?, ?>> extends ItemDefinitionImpl<I> {

        private RemovedItemDefinition(@NotNull QName itemName) {
            super(itemName, DOMUtil.XSD_ANYTYPE);
        }

        @Override
        public @NotNull I instantiate() throws SchemaException {
            throw unsupported();
        }

        @Override
        public @NotNull I instantiate(QName name) throws SchemaException {
            throw unsupported();
        }

        @Override
        public @NotNull ItemDelta<?, ?> createEmptyDelta(ItemPath path) {
            throw unsupported();
        }

        @Override
        public ItemDefinitionMutator mutator() {
            throw unsupported();
        }

        @Override
        public Optional<ComplexTypeDefinition> structuredType() {
            throw unsupported();
        }

        @Override
        protected String getDebugDumpClassName() {
            throw unsupported();
        }

        @Override
        public String getDocClassName() {
            throw unsupported();
        }

        @Override
        public @NotNull ItemDefinition<I> clone() {
            throw unsupported();
        }

        @Override
        public @NotNull ItemDefinition<I> cloneWithNewName(@NotNull ItemName itemName) {
            throw unsupported();
        }

        private @NotNull UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("Unsupported method called on removed definition for " + itemName);
        }

        @Override
        public Class<?> getTypeClass() {
            throw unsupported();
        }
    }

    protected PrismProperty<?> idAsProperty() {
        var prop = getDefinition().findPropertyDefinition(PrismConstants.T_ID).instantiate();
        prop.setRealValue(getIdentifier());
        return prop;
    }
}
