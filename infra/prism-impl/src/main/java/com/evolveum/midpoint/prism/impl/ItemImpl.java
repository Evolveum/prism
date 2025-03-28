/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl;

import static com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy.DATA;
import static com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy.DEFAULT_FOR_EQUALS;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Supplier;
import javax.xml.namespace.QName;

import com.google.common.base.Strings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.ItemDefinitionTransformer.TransformableItem;
import com.evolveum.midpoint.prism.ItemDefinitionTransformer.TransformableValue;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.util.CloneUtil;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.Holder;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;

/**
 * Item is a common abstraction of Property and PropertyContainer.
 * <p>
 * This is supposed to be a superclass for all items. Items are things
 * that can appear in property containers, which generally means only a property
 * and property container itself. Therefore this is in fact superclass for those
 * two definitions.
 *
 * @author Radovan Semancik
 */
public abstract class ItemImpl<V extends PrismValue, D extends ItemDefinition<?>> extends AbstractFreezable implements Item<V, D>, TransformableItem {

    private static final long serialVersionUID = 510000191615288733L;

    // The object should basically work without definition and prismContext. This is the
    // usual case when it is constructed "out of the blue", e.g. as a new JAXB object
    // It may not work perfectly, but basic things should work
    protected ItemName elementName;
    protected PrismContainerValue<?> parent;
    protected D definition;
    // FIXME: THis should be Collection, not list, since list implementations does not allow hashing
    @NotNull protected final List<V> values = new ArrayList<>();
    private transient Map<String, Object> userData = new HashMap<>();

    protected boolean incomplete;

    /**
     * This is used for definition-less construction, e.g. in JAXB beans.
     *
     * The constructors should be used only occasionally (if used at all).
     * Use the factory methods in the ResourceObjectDefintion instead.
     */
    ItemImpl(QName elementName) {
        this.elementName = ItemName.fromQName(elementName);
    }

    /**
     * The constructors should be used only occasionally (if used at all).
     * Use the factory methods in the ResourceObjectDefinition instead.
     */
    ItemImpl(QName elementName, D definition) {
        super();
        this.elementName = ItemName.fromQName(elementName);
        this.definition = definition;
    }

    static <T extends Item> T createNewDefinitionlessItem(QName name, Class<T> type) {
        T item;
        try {
            //noinspection unchecked
            Constructor<T> constructor = toImplClass(type).getConstructor(QName.class);
            item = constructor.newInstance(name);
            item.revive(PrismContext.get());
        } catch (Exception e) {
            throw new SystemException("Error creating new definitionless " + type.getSimpleName() + ": " + e.getClass().getName() + " " + e.getMessage(), e);
        }
        return item;
    }

    private static <T> Class toImplClass(Class<T> type) {
        if (PrismProperty.class.equals(type)) {
            return PrismPropertyImpl.class;
        } else if (PrismReference.class.equals(type)) {
            return PrismReferenceImpl.class;
        } else if (PrismContainer.class.equals(type)) {
            return PrismContainerImpl.class;
        } else {
            return type;    // or throw an exception?
        }
    }

    @Override
    public D getDefinition() {
        return definition;
    }

    @Override
    public ItemName getElementName() {
        return elementName;
    }

    @Override
    public void setElementName(QName elementName) {
        checkMutable();
        this.elementName = ItemName.fromQName(elementName);
    }

    /**
     * Sets applicable property definition.
     *
     * @param definition the definition to set
     */
    @Override
    public void setDefinition(@NotNull D definition) {
        checkMutable();
        checkDefinition(definition);
        this.definition = definition;
    }

    @Override
    public boolean isIncomplete() {
        return incomplete;
    }

    @Override
    public void setIncomplete(boolean incomplete) {
        this.incomplete = incomplete;
    }

    @Override
    public PrismContainerValue<?> getParent() {
        return parent;
    }

    @Override
    public void setParent(PrismContainerValue<?> parentValue) {
        if (this.parent != null && parentValue != null && this.parent != parentValue) {
            throw new IllegalStateException("Attempt to reset parent of item " + this + " from " + this.parent + " to " + parentValue);
        }
        // Immutability check can be skipped, as setting the parent doesn't alter this object.
        // However, if existing parent itself is immutable, adding/removing its child item will cause the exception.
        this.parent = parentValue;
    }

    protected Object getPathComponent() {
        ItemName elementName = getElementName();
        if (elementName != null) {
            return elementName;
        } else {
            throw new IllegalStateException("Unnamed item has no path");
        }
    }

    @Nullable
    @Override
    public Object getRealValue() {
        V value = getValue();
        return value != null ? value.getRealValue() : null;
    }

    /**
     * Type override, also for compatibility.
     */
    @Override
    public <X> X getRealValue(Class<X> type) {
        V singleValue = getValue();
        if (singleValue == null) {
            return null;
        }
        Object value = singleValue.getRealValue();
        if (value == null) {
            return null;
        }
        if (type.isAssignableFrom(value.getClass())) {
            //noinspection unchecked
            return (X) value;
        } else {
            throw new ClassCastException("Cannot cast value of item " + getElementName() + " which is of type " + value.getClass() + " to " + type);
        }
    }

    /**
     * Type override, also for compatibility.
     */
    @Override
    public <X> X[] getRealValuesArray(Class<X> type) {
        //noinspection unchecked
        X[] valuesArray = (X[]) Array.newInstance(type, getValues().size());
        for (int j = 0; j < getValues().size(); ++j) {
            Object value = getValues().get(j).getRealValue();
            Array.set(valuesArray, j, value);
        }
        return valuesArray;
    }

    @Override
    @NotNull
    public ItemPath getPath() {
        if (parent == null) {
            if (getElementName() != null) {
                return getElementName();
            } else {
                throw new IllegalStateException("Unnamed item has no path");
            }
        }
        /*
         * This quite ugly algorithm is here to eliminate the need to repeatedly call itemPath.append(..) method
         * that leads to creation of many little objects on the heap. Instead we simply collect path segments
         * and merge them to a single item path in one operation.
         *
         * TODO This is not very nice solution. Think again about it.
         */
        List<Object> names = new ArrayList<>();
        acceptParentVisitor(v -> {
            Object pathComponent;
            if (v instanceof Item) {
                if (v instanceof ItemImpl) {
                    pathComponent = ((ItemImpl) v).getPathComponent();
                } else {
                    throw new IllegalStateException("Expected ItemImpl but got " + v.getClass());
                }
            } else if (v instanceof PrismValue) {
                if (v instanceof PrismValueImpl) {
                    pathComponent = ((PrismValueImpl) v).getPathComponent();
                } else {
                    throw new IllegalStateException("Expected PrismValueImpl but got " + v.getClass());
                }
            } else if (v instanceof Itemable) {     // e.g. a delta
                pathComponent = ((Itemable) v).getPath();
            } else {
                throw new IllegalStateException("Expected Item or PrismValue but got " + v.getClass());
            }
            if (pathComponent != null) {
                names.add(pathComponent);
            }
        });
        return ItemPath.createReverse(names);
    }

    @Override
    public void acceptParentVisitor(@NotNull Visitor visitor) {
        visitor.visit(this);
        if (parent != null) {
            parent.acceptParentVisitor(visitor);
        }
    }

    @Override
    @NotNull
    public Map<String, Object> getUserData() {
        if (userData == null) {
            userData = new HashMap<>();
        }
        if (isImmutable()) {
            return Collections.unmodifiableMap(userData);            // TODO beware, objects in userData themselves are mutable
        } else {
            return userData;
        }
    }

    @Override
    public <T> T getUserData(String key) {
        // TODO make returned data immutable (?)
        return (T) getUserData().get(key);
    }

    @Override
    public void setUserData(String key, Object value) {
        checkMutable();
        getUserData().put(key, value);
    }

    @Override
    @NotNull
    public List<V> getValues() {
        return values;
    }

    @Override
    public V getValue() {
        if (values.isEmpty()) {
            return null;
        } else if (values.size() == 1) {
            return values.get(0);
        } else {
            throw new IllegalStateException("Attempt to get single value from item " + getElementName() + " with multiple values");
        }
    }

    @Override
    public boolean addAll(Collection<V> newValues, @NotNull EquivalenceStrategy strategy) throws SchemaException {
        return addAllInternal(newValues, true, strategy);
    }

    // The checkUniqueness parameter is redundant but let's keep it for robustness.
    @Contract("_, true, null -> fail; _, false, !null -> fail")
    private boolean addAllInternal(Collection<V> newValues, boolean checkUniqueness, EquivalenceStrategy strategy) throws SchemaException {
        checkMutable();
        boolean changed = false;
        for (V val : newValues) {
            if (addInternal(val, checkUniqueness, strategy).isChanged()) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean add(@NotNull V newValue, @NotNull EquivalenceStrategy equivalenceStrategy) throws SchemaException {
        return addInternal(newValue, true, equivalenceStrategy).isChanged();
    }

    public ItemModifyResult<V> addWithResult(@NotNull V newValue, @NotNull EquivalenceStrategy equivalenceStrategy) throws SchemaException {
        return addInternal(newValue, true, equivalenceStrategy);
    }

    @Override
    public ItemModifyResult<V> addIgnoringEquivalents(@NotNull V newValue) throws SchemaException {
        return addInternal(newValue, false, null);
    }

    // The checkUniqueness parameter is redundant but let's keep it for robustness.
    @Contract("_, true, null -> fail; _, false, !null -> fail")
    protected ItemModifyResult<V> addInternal(
            @NotNull V newValue, boolean checkEquivalents, EquivalenceStrategy equivalenceStrategy) throws SchemaException {

        if (checkEquivalents && equivalenceStrategy == null) {
            throw new IllegalArgumentException("Equivalence strategy must be present if checkEquivalents is true");
        }
        if (!checkEquivalents && equivalenceStrategy != null) {
            throw new IllegalArgumentException("Equivalence strategy must not be present if checkEquivalents is false");
        }

        checkMutable();
        resetIncompleteFlagOnAddIfPossible();

        // The parent is needed also for comparisons. So we set it here.
        Itemable originalParent = newValue.getParent();
        if (isParentForValues()) {
            newValue.setParent(this);
        }

        if (checkEquivalents) {
            V exactEquivalentFound = null;
            V somethingRemoved = null;
            Iterator<V> iterator = values.iterator();
            while (iterator.hasNext()) {
                V currentValue = iterator.next();
                if (equivalenceStrategy.equals(currentValue, newValue)) {
                    if (exactEquivalentFound == null &&
                            (DEFAULT_FOR_EQUALS.equals(equivalenceStrategy) || DEFAULT_FOR_EQUALS.equals(currentValue, newValue))) {
                        exactEquivalentFound = currentValue;
                    } else {
                        iterator.remove();
                        valueRemoved(currentValue);
                        if (isParentForValues()) {
                            currentValue.setParent(null);
                        }
                        somethingRemoved = currentValue;
                    }
                }
            }

            if (exactEquivalentFound != null && somethingRemoved == null) {
                if (isParentForValues()) {
                    newValue.setParent(originalParent);
                }
                return ItemModifyResult.unmodified(newValue);
            }
        }

        D definition = getDefinition();
        if (definition != null) {
            if (!values.isEmpty() && definition.isSingleValue()) {
                throw new SchemaException("Attempt to put more than one value to single-valued item " + this + "; newly added value: " + newValue);
            }
            if (!newValue.isImmutable()) {
                //noinspection unchecked
                newValue = (V) newValue.applyDefinition(definition, false);
            }
        }
        addInternalExecution(newValue);
        return ItemModifyResult.added(newValue, newValue);
    }

    /**
     * When adding a specific value to a single-valued incomplete item, we can assume that the item is no longer incomplete.
     * The reason is that it there is no other, unknown value, that would make the item incomplete.
     * (As single-valued items can have only one value.)
     *
     * This is true regardless of whether the value being added is the same as the (unknown) value.
     * If it's the same, the item is complete, as it will have one known value.
     * If it's different, then the item is also complete, as the new value would overwrite the old one.
     *
     * It is also true, if the item contains (by mistake) any existing value; although that state is inconsistent by nature.
     *
     * See also https://docs.evolveum.com/midpoint/devel/design/incomplete-items-4.9.1/.
     *
     * @see Item#isIncomplete()
     */
    void resetIncompleteFlagOnAddIfPossible() {
        if (incomplete && isSingleValueByDefinition()) {
            incomplete = false;
        }
    }

    protected void valueRemoved(V currentValue) {
        // NOOP
    }

    protected boolean addInternalExecution(@NotNull V newValue) {
        return values.add(newValue);
    }

    /**
     * Adds a given value with no checks, no definition application, and so on.
     * For internal use only.
     */
    @Experimental
    public void addForced(@NotNull V newValue) {
        values.add(newValue);
    }

    @Override
    public ItemModifyResult<V> addRespectingMetadataAndCloning(V value, @NotNull EquivalenceStrategy strategy,
            EquivalenceStrategy metadataEquivalenceStrategy) throws SchemaException {
        if (!value.hasValueMetadata()) {
            var cloned = CloneUtil.clone(value);
            return addWithResult(cloned, strategy);
        } else {
            V existingValue = findValue(value, strategy);
            if (existingValue == null) {
                var cloned = CloneUtil.clone(value);
                return addInternal(cloned, false, null);
            } else {
                addMetadataValues(existingValue, value.getValueMetadata(), metadataEquivalenceStrategy);
                return ItemModifyResult.modified(value, existingValue);
            }
        }
    }

    private void addMetadataValues(V existingValue, ValueMetadata newMetadata, EquivalenceStrategy metadataEquivalenceStrategy) throws SchemaException {
        ValueMetadata existingValueMetadata = existingValue.getValueMetadata();
        for (PrismContainerValue<Containerable> newMetadataValue : newMetadata.getValues()) {
            PrismContainerValue<Containerable> sameProvenance = existingValueMetadata.findValue(newMetadataValue, metadataEquivalenceStrategy);
            if (sameProvenance != null) {
                // REAL_VALUE is unsafe here (also) because some parts of metadata are mistakenly marked as operational.
                // Anyway, it is best to use DATA as "sameProvenance" is taken directly from existingValueMetadata.
                existingValueMetadata.remove(sameProvenance, DATA);
            }
            existingValueMetadata.add(newMetadataValue.clone());
        }
    }

    @Override
    public ItemModifyResult<V> removeRespectingMetadata(V value, @NotNull EquivalenceStrategy strategy,
            EquivalenceStrategy metadataEquivalenceStrategy) {
        if (!value.hasValueMetadata()) {
            return removeWithResult(value, strategy);

        } else {
            // We do not support the case when we are deleting by ID but only selected metadata.
            // I.e. if we want to delete metadata we must supply the correct (matching) value.
            V existingValue = findValue(value, strategy);
            if (existingValue != null) {
                return removeMetadataValues(value, existingValue, value.getValueMetadata(), metadataEquivalenceStrategy);
            }
        }
        return ItemModifyResult.unmodified(value);
    }

    private ItemModifyResult<V> removeMetadataValues(V requestValue, V existingValue, ValueMetadata metadataToRemove, EquivalenceStrategy metadataEquivalenceStrategy) {
        ValueMetadata existingValueMetadata = existingValue.getValueMetadata();
        existingValueMetadata.removeAll(metadataToRemove.getValues(), metadataEquivalenceStrategy);
        if (existingValueMetadata.hasNoValues()) {
            remove(existingValue, DATA.exceptForValueMetadata());
            return ItemModifyResult.removed(requestValue, existingValue);
        }
        return ItemModifyResult.modified(requestValue, existingValue);
    }

    @Override
    public boolean removeAll(Collection<V> newValues, @NotNull EquivalenceStrategy strategy) {
        checkMutable();
        boolean changed = false;
        for (V val : newValues) {
            if (remove(val, strategy)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean remove(V value, @NotNull EquivalenceStrategy strategy) {
        return !removeWithResult(value, strategy).isUnmodified();
    }

    public ItemModifyResult<V> removeWithResult(V value, @NotNull EquivalenceStrategy strategy) {
        checkMutable();
        V removedValue = null;
        Iterator<V> iterator = values.iterator();
        while (iterator.hasNext()) {
            V val = iterator.next();
            if (val.representsSameValue(value, strategy, false) || val.equals(value, strategy)) {
                iterator.remove();
                valueRemoved(val);
                if (isParentForValues()) {
                    val.setParent(null);
                }
                removedValue = val;
            }
        }
        if (removedValue != null) {
            return ItemModifyResult.removed(value, removedValue);
        }
        return ItemModifyResult.unmodified(value);
    }

    public V remove(int index) {
        checkMutable();
        V removed = values.remove(index);
        valueRemoved(removed);
        if (isParentForValues()) {
            removed.setParent(null);
        }
        return removed;
    }

    @Override
    public void replaceAll(Collection<V> newValues, @NotNull EquivalenceStrategy strategy) throws SchemaException {
        checkMutable();
        clear();
        addAll(newValues, strategy);
    }

    @Override
    public void replace(V newValue) throws SchemaException {
        checkMutable();
        clear();
        addIgnoringEquivalents(newValue);
    }

    @Override
    public void clear() {
        checkMutable();
        if (isParentForValues()) {
            for (V value : values) {
                value.setParent(null);
            }
        }
        values.clear();
    }

    /**
     * Does this object serve as a parent for its values? Usually it is so.
     *
     * An exception is {@link EmbeddedPrismObjectImpl} that denotes a (fake) {@link PrismObject}
     * implementation for object values that are embedded in a {@link PrismContainer} somewhere
     * in the enclosing (real) {@link PrismObject}.
     *
     * An example: identities/identity/data in midPoint focus object
     */
    boolean isParentForValues() {
        return true;
    }

    @Override
    public void normalize() {
//        checkMutable();
//        for (V value : values) {
//            value.normalize();
//        }
    }

    /**
     * Merge all the values of other item to this item.
     */
    @Override
    public void merge(Item<V, D> otherItem) throws SchemaException {
        for (V otherValue : otherItem.getValues()) {
            if (!contains(otherValue)) {
                add((V) otherValue.clone());
            }
        }
    }

    @Override
    public ItemDelta<V, D> diff(Item<V, D> other, @NotNull ParameterizedEquivalenceStrategy strategy) {
        List<ItemDelta<V, D>> itemDeltas = new ArrayList<>();
        diffInternal(other, itemDeltas, true, strategy);
        return MiscUtil.extractSingleton(itemDeltas);
    }

    void diffInternal(Item<V, D> other, Collection<? extends ItemDelta> deltas, boolean rootValuesOnly,
            ParameterizedEquivalenceStrategy strategy) {
        diffInternal(other, deltas, rootValuesOnly, strategy, false);
    }

    boolean diffInternal(Item<V, D> other, Collection<? extends ItemDelta> deltas, boolean rootValuesOnly,
            ParameterizedEquivalenceStrategy strategy, boolean exitOnDiff) {
        ItemDelta delta = exitOnDiff ? null : createDelta();
        if (other == null) {
            // Early exit for equals use case
            if (exitOnDiff) {
                return hasAnyValue();
            }
            // other doesn't exist, so delta means delete all values
            for (PrismValue value : getValues()) {
                PrismValue valueClone = value.clone();
                delta.addValueToDelete(valueClone);
            }
        } else {
            if (delta != null && delta.getDefinition() == null) {
                D otherDefinition = other.getDefinition();
                if (otherDefinition != null) {
                    delta.setDefinition(otherDefinition.clone()); // TODO why cloning?
                }
            }
            // the other exists, this means that we need to compare the values one by one
            Collection<PrismValue> outstandingOtherValues = new ArrayList<>(other.getValues());
            for (PrismValue thisValue : getValues()) {
                Iterator<PrismValue> iterator = outstandingOtherValues.iterator();
                boolean found = false;
                while (iterator.hasNext()) {
                    PrismValueImpl otherValue = (PrismValueImpl) iterator.next();
                    if (!rootValuesOnly && thisValue.representsSameValue(otherValue, strategy, true)) {
                        found = true;
                        // Matching IDs, look inside to figure out internal deltas
                        boolean different =
                                ((PrismValueImpl) thisValue).diffMatchingRepresentation(otherValue, deltas, strategy, exitOnDiff);
                        if (exitOnDiff && different) {
                            return true;
                        }
                        iterator.remove(); // No need to process this value again
                        break;
                    } else if (thisValue.equals(otherValue, strategy)) {
                        found = true;
                        iterator.remove(); // Same values. No delta. No need to process this value again.
                        break;
                    }
                }
                if (!found) {
                    if (exitOnDiff) {
                        return true;
                    } else {
                        assert delta != null;
                        // We have the value and the other does not, this is delete of the entire value
                        delta.addValueToDelete(thisValue.clone());
                    }
                }
            }
            // outstandingOtherValues are those values that the other has and we could not
            // match them to any of our values. These must be new values to add
            if (exitOnDiff) {
                if (!outstandingOtherValues.isEmpty()) {
                    return true;
                }
            } else {
                assert delta != null;
                for (PrismValue outstandingOtherValue : outstandingOtherValues) {
                    delta.addValueToAdd(outstandingOtherValue.clone());
                }
                // Some deltas may need to be polished a bit. E.g. transforming add/delete delta to a replace delta.
                delta = fixupDelta(delta, other);
            }
        }
        if (exitOnDiff || ItemDelta.isEmpty(delta)) {
            return false;
        } else {
            ((Collection) deltas).add(delta);
            return true;
        }
    }

    protected ItemDelta<V, D> fixupDelta(ItemDelta<V, D> delta, Item<V, D> other) {
        return delta;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
        for (PrismValue value : getValues()) {
            value.accept(visitor);
        }
    }

    @Override
    public void accept(Visitor visitor, ItemPath path, boolean recursive) {
        // This implementation is supposed to only work for non-hierarchical items, such as properties and references.
        // hierarchical items must override it.
        if (recursive) {
            accept(visitor);
        } else {
            visitor.visit(this);
        }
    }

    /**
     * Re-apply PolyString (and possible other) normalizations to the object.
     */
    @Override
    public void recomputeAllValues() {
        accept(visitable -> {
            if (visitable instanceof PrismPropertyValue<?>) {
                ((PrismPropertyValue<?>) visitable).recompute(PrismContext.get());
            }
        });
    }

    @Override
    public void applyDefinition(@NotNull D definition, boolean force) throws SchemaException {
        checkMutable(); // TODO consider if there is real change
        checkDefinition(definition);
        this.definition = definition;
        applyDefinitionToValues(definition, force);
    }

    protected void applyDefinitionToValues(@NotNull D definition, boolean force) throws SchemaException {
        List<V> newVals = new ArrayList<>();
        boolean changed = false;
        for (PrismValue pval : getValues()) {
            //noinspection unchecked
            V newVal = (V) pval.applyDefinition(definition, force);
            newVals.add(newVal);
            changed = true;
        }
        if (changed) {
            clear();
            addAllInternal(newVals, false, null);
        }
    }

    @Override
    public void revive(PrismContext prismContext) {
        // Is revive necessary if prism context is static?
        // it is necessary to do e.g. PolyString recomputation even if PrismContext is set
        if (definition != null) {
            definition.revive(prismContext);
        }
        for (V value : values) {
            value.revive(prismContext);
        }
    }

    protected void copyValues(CloneStrategy strategy, ItemImpl clone) {
        clone.elementName = this.elementName;
        clone.definition = this.definition;
        // Do not clone parent so the cloned item can be safely placed to another item
        clone.parent = null;
        clone.userData = MiscUtil.cloneMap(this.userData);
        clone.incomplete = this.incomplete;
        // Also do not copy 'immutable' flag so the clone is free to be modified
    }

    /** TODO description */
    protected void propagateDeepCloneDefinition(@NotNull DeepCloneOperation operation, D clonedDefinition) {
        // nothing to do by default
    }

    @Override
    public void checkConsistence(boolean requireDefinitions, ConsistencyCheckScope scope) {
        checkConsistenceInternal(this, requireDefinitions, false, scope);
    }

    @Override
    public void checkConsistence(boolean requireDefinitions, boolean prohibitRaw) {
        checkConsistenceInternal(this, requireDefinitions, prohibitRaw, ConsistencyCheckScope.THOROUGH);
    }

    @Override
    public void checkConsistence(boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope) {
        checkConsistenceInternal(this, requireDefinitions, prohibitRaw, scope);
    }

    @Override
    public void checkConsistence() {
        checkConsistenceInternal(this, false, false, ConsistencyCheckScope.THOROUGH);
    }

    @Override
    public void checkConsistence(ConsistencyCheckScope scope) {
        checkConsistenceInternal(this, false, false, scope);
    }

    @Override
    public void checkConsistenceInternal(Itemable rootItem, boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope) {
        ItemPath path = getPath();
        if (elementName == null) {
            throw new IllegalStateException(
                    "Item %s has no name (%s in %s)".formatted(this, path, rootItem));
        }

        if (definition != null) {
            checkDefinition(definition);
        } else if (requireDefinitions && !isRaw()) {
            throw new IllegalStateException(
                    "No definition in item %s (%s in %s)".formatted(this, path, rootItem));
        }
        for (V val : values) {
            if (prohibitRaw && val.isRaw()) {
                throw new IllegalStateException(
                        "Raw value %s in item %s (%s in %s)".formatted(val, this, path, rootItem));
            }
            if (val == null) {
                throw new IllegalStateException(
                        "Null value in item %s (%s in %s)".formatted(this, path, rootItem));
            }
            if (val.getParent() == null) {
                throw new IllegalStateException(
                        "Null parent for value %s in item %s (%s in %s)".formatted(val, this, path, rootItem));
            }
            if (val.getParent() != this) {
                throw new IllegalStateException(
                        "Wrong parent for value %s in item %s (%s in %s), bad parent: %s".formatted(
                                val, this, path, rootItem, val.getParent()));
            }
            val.checkConsistenceInternal(rootItem, requireDefinitions, prohibitRaw, scope);
        }
    }

    /**
     * This is a separate method, as it is used at various places, e.g.
     *
     * - in {@link #applyDefinition(ItemDefinition, boolean)}
     * - in {@link #setDefinition(ItemDefinition)}
     * - when checking the consistence
     */
    protected void checkDefinition(@NotNull D def) {
    }

    @Override
    public void assertDefinitions() throws SchemaException {
        assertDefinitions(() -> "");
    }

    @Override
    public void assertDefinitions(Supplier<String> sourceDescriptionSupplier) throws SchemaException {
        assertDefinitions(false, sourceDescriptionSupplier);
    }

    @Override
    public void assertDefinitions(boolean tolerateRawValues, Supplier<String> sourceDescriptionSupplier) throws SchemaException {
        if (tolerateRawValues && isRaw()) {
            return;
        }
        // Must be inlined because of the use of source description supplier
        if (definition == null) {
            throw new SchemaException(
                    Strings.lenientFormat("No definition in %s in %s",
                            this, sourceDescriptionSupplier.get()));
        }
    }

    @Override
    public int hashCode(@NotNull EquivalenceStrategy equivalenceStrategy) {
        return equivalenceStrategy.hashCode(this);
    }

    @Override
    public int hashCode(@NotNull ParameterizedEquivalenceStrategy equivalenceStrategy) {
        if (definition != null && definition.isRuntimeSchema() && !equivalenceStrategy.isHashRuntimeSchemaItems()) {
            //System.out.println("HashCode is 0 because of runtime: " + this);
            return 0;
        }
        int valuesHash = MiscUtil.unorderedCollectionHashcode(values, null);
        if (valuesHash == 0) {
            // empty or non-significant container. We do not want this to destroy hashcode of parent item
            //System.out.println("HashCode is 0 because values hashCode is 0: " + this);
            return 0;
        }
        final int prime = 31;
        int result = 1;
        if (equivalenceStrategy.isConsideringElementNames()) {
            String localElementName = elementName != null ? elementName.getLocalPart() : null;
            result = prime * result + ((localElementName == null) ? 0 : localElementName.hashCode());
        }
        result = prime * result + valuesHash;
        //System.out.println("HashCode is " + result + " for: " + this);
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode(DEFAULT_FOR_EQUALS);
    }

    @Override
    public boolean equals(Object obj, @NotNull EquivalenceStrategy strategy) {
        if (!(obj instanceof Item)) {
            return false;
        } else if (strategy instanceof ParameterizedEquivalenceStrategy) {
            // note that the counter is increased in the called equals(..) method - because that method can be called also
            // independently from this site
            return equals(obj, (ParameterizedEquivalenceStrategy) strategy);
        } else {
            incrementObjectCompareCounterIfNeeded(obj);
            return strategy.equals(this, (Item<?, ?>) obj);
        }
    }

    @Override
    public boolean equals(Object obj, @NotNull ParameterizedEquivalenceStrategy parameterizedEquivalenceStrategy) {
        incrementObjectCompareCounterIfNeeded(obj);
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Item)) {
            return false;
        }
        Item<?, ?> second = (Item<?, ?>) obj;
        @SuppressWarnings("unchecked")
        Collection<V> secondValues = (Collection<V>) second.getValues();
        return (!parameterizedEquivalenceStrategy.isConsideringDefinitions() || Objects.equals(definition, second.getDefinition())) &&
                (!parameterizedEquivalenceStrategy.isConsideringElementNames() || Objects.equals(elementName, second.getElementName())) &&
                incomplete == second.isIncomplete() &&
                MiscUtil.unorderedCollectionEquals(values, secondValues, parameterizedEquivalenceStrategy::equals);
        // Do not compare parents at all. They are not relevant.
    }

    private void incrementObjectCompareCounterIfNeeded(Object obj) {
        if (this instanceof PrismObject && PrismContext.get().getMonitor() != null) {
            PrismContext.get().getMonitor().recordPrismObjectCompareCount((PrismObject<? extends Objectable>) this, obj);
        }
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return equals(obj, DEFAULT_FOR_EQUALS);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + PrettyPrinter.prettyPrint(getElementName()) + ")";
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        if (DebugUtil.isDetailedDebugDump()) {
            sb.append(getDebugDumpClassName()).append(": ");
        }
        sb.append(DebugUtil.formatElementName(getElementName()));
        return sb.toString();
    }

    /**
     * Return a human readable name of this class suitable for logs.
     */
    protected String getDebugDumpClassName() {
        return "Item";
    }

    protected void appendDebugDumpSuffix(StringBuilder sb) {
        if (incomplete) {
            sb.append(" (incomplete)");
        }
    }

    @Override
    public void performFreeze() {
        for (V value : getValues()) {
            value.freeze();
        }
    }

    @Override
    public @NotNull Collection<PrismValue> getAllValues(ItemPath path) {
        if (path.isEmpty()) {
            return Collections.unmodifiableCollection(values);
        } else {
            return List.of();  // Overridden for containers
        }
    }

    @Override
    public @NotNull Collection<Item<?, ?>> getAllItems(@NotNull ItemPath path) {
        if (path.isEmpty()) {
            return List.of(this);
        } else {
            return List.of(); // Overridden for containers
        }
    }

    @Override
    public abstract Item<V, D> clone();

    @Override
    public Item<V, D> createImmutableClone() {
        Item<V, D> clone = clone();
        clone.freeze();
        return clone;
    }

    @Override
    public Long getHighestId() {
        Holder<Long> highest = new Holder<>();
        this.accept(visitable -> {
            if (visitable instanceof PrismContainerValue) {
                Long id = ((PrismContainerValue<?>) visitable).getId();
                if (id != null && (highest.isEmpty() || id > highest.getValue())) {
                    highest.setValue(id);
                }
            }
        });
        return highest.getValue();
    }

    @Override
    public void transformDefinition(ComplexTypeDefinition parent, ItemDefinitionTransformer transformation) {
        D newDefinition = transformation.transformItem(parent, definition);
        // Do not replace definition with null or run checks if definition is unmodified.
        if (newDefinition != null && newDefinition != definition) {
            setDefinition(newDefinition);
        }
        for (V pval : values) {
            if (pval instanceof TransformableValue) {
                ((TransformableValue) pval).transformDefinition(parent, definition, transformation);
            }
        }
    }
}
