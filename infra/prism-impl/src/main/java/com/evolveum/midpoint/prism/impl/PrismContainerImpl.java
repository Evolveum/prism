/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import java.io.Serial;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.deleg.PrismContainerValueDelegator;

import com.evolveum.midpoint.prism.lazy.FlyweightClonedItem;

import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ContainerDelta;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.impl.delta.ContainerDeltaImpl;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;

import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * Property container groups properties into logical blocks.The reason for
 * grouping may be as simple as better understandability of data structure. But
 * the group usually means different meaning, source or structure of the data.
 * For example, the property container is frequently used to hold properties
 * that are dynamic, not fixed by a static schema. Such grouping also naturally
 * translates to XML and helps to "quarantine" such properties to avoid Unique
 * Particle Attribute problems.
 * </p><p>
 * Property Container contains a set of (potentially multi-valued) properties or inner property containers.
 * The order of properties is not significant, regardless of the fact that it
 * may be fixed in the XML representation. In the XML representation, each
 * element inside Property Container must be either Property or a Property
 * Container.
 * </p><p>
 * Property Container is mutable.
 * </p>
 *
 * @author Radovan Semancik
 */
public class PrismContainerImpl<C extends Containerable>
        extends ItemImpl<PrismContainerValue<C>, PrismContainerDefinition<C>>
        implements PrismContainer<C> {

    @Serial private static final long serialVersionUID = 5206821250098051028L;

    protected Class<C> compileTimeClass;

    private static final int FAST_ID_THRESHOLD = 20;

    private transient boolean strictModifications = false;
    private transient Set<Long> presentIdentifiers;

    public PrismContainerImpl(QName name) {
        super(name);
    }

    /**
     * The `definition` parameter is here to preserve the definition if already present for a value (when instantiating from it).
     * Useful for dynamic definitions of midPoint shadows, for example.
     */
    public PrismContainerImpl(QName name, @NotNull Class<C> compileTimeClass, @Nullable PrismContainerDefinition<C> definition) {
        super(name);
        if (Modifier.isAbstract(compileTimeClass.getModifiers())) {
            throw new IllegalArgumentException(
                    "Can't use class '%s' as compile-time class for %s; the class is abstract.".formatted(
                            compileTimeClass.getSimpleName(), name));
        }
        this.compileTimeClass = compileTimeClass;
        if (definition == null) {
            try {
                // Here the container gets a definition.
                PrismContext.get().adopt(this);
            } catch (SchemaException e) {
                throw SystemException.unexpected(e, " while instantiating a PrismContainer with name %s and compile-time class %s"
                        .formatted(name, compileTimeClass));
            }
        }
    }

    public PrismContainerImpl(QName name, PrismContainerDefinition<C> definition) {
        super(name, definition);
    }

    @NotNull
    private ItemPath skipFirstId(@NotNull ItemPath path) {
        if (path.startsWithName()) {
            return path;
        } else if (path.startsWithId()) {
            return path.rest();
        } else {
            throw new IllegalArgumentException("Unexpected path segment in "+path);
        }
    }

    @Override
    public Class<C> getCompileTimeClass() {
        if (this.compileTimeClass != null) {
            return compileTimeClass;
        }
        if (getDefinition() != null) {
            return getDefinition().getCompileTimeClass();
        }
        return null;
    }

    @Override
    public boolean canRepresent(@NotNull Class<?> compileTimeClass) {
        Class<C> currentClass = getCompileTimeClass();
        return currentClass != null && compileTimeClass.isAssignableFrom(currentClass);
    }

    @Override
    public boolean canRepresent(@NotNull QName type) {
        PrismContainerDefinition<C> definition = getDefinition();
        if (definition == null) {
            throw new IllegalStateException("No definition in "+this+", cannot evaluate type equivalence");
        }
        return definition.canRepresent(type);
    }

    @NotNull
    @Override
    public Collection<C> getRealValues() {
        List<C> realValues = new ArrayList<>(getValues().size());
        for (PrismContainerValue<C> value : getValues()) {
            realValues.add(value.asContainerable());
        }
        return realValues;
    }

    @NotNull
    @Override
    public C getRealValue() {
        return getValue().getRealValue();
    }

    @Override
    public void setRealValue(C value) throws SchemaException {
        //noinspection unchecked
        setValue(value != null ? value.asPrismContainerValue() : null);
    }

    @Override
    @NotNull
    public PrismContainerValue<C> getValue() {
        if (getValues().size() == 1) {
            return getValues().get(0);
        }
        if (getValues().size() > 1) {
            throw new IllegalStateException("Attempt to get single value from a multivalued container "+ getElementName());
        }
        // We are not sure about multiplicity if there is no definition or the definition is dynamic
        // TODO why testing for isDynamic? consider Item.isSingleValue (we already removed this condition from there); see MID-3922
        if (getDefinition() != null && !getDefinition().isDynamic()) {
            if (getDefinition().isSingleValue()) {
                // Insert first empty value. This simulates empty single-valued container. It the container exists
                // it is clear that it has at least one value (and that value is empty).
                PrismContainerValue<C> pValue = new PrismContainerValueImpl<>(null, null, this, null, null);
                try {
                    add(pValue);
                } catch (SchemaException e) {
                    // This should not happen
                    throw new SystemException("Internal Error: "+e.getMessage(),e);
                }
                return pValue;
            } else {
                throw new IllegalStateException("Attempt to get single value from a multivalued container "+ getElementName());
            }
        } else {
            // Insert first empty value. This simulates empty single-valued container. It the container exists
            // it is clear that it has at least one value (and that value is empty).
            PrismContainerValue<C> pValue = new PrismContainerValueImpl<>(null, null, this, null, null);
            try {
                add(pValue);
            } catch (SchemaException e) {
                // This should not happen
                throw new SystemException("Internal Error: "+e.getMessage(),e);
            }
            return pValue;
        }
    }

    @Override
    public void setValue(@NotNull PrismContainerValue<C> value) throws SchemaException {
        checkMutable();
        if (getDefinition() != null) {
            if (getDefinition().isSingleValue()) {
                clear();
                addIgnoringEquivalents(value);
            } else {
                throw new IllegalStateException("Attempt to set single value to a multivalued container "+ getElementName());
            }
        } else {
            clear();
            addIgnoringEquivalents(value);
        }
    }

    @Override
    protected ItemModifyResult<PrismContainerValue<C>> addInternal(@NotNull PrismContainerValue newValue, boolean checkEquivalents, EquivalenceStrategy strategy) throws SchemaException {
        checkMutable();
        return super.addInternal(newValue, checkEquivalents, strategy);
    }

    @Override
    protected boolean addInternalExecution(@NotNull PrismContainerValue<C> newValue) {
        if (useIdentifierIndex()) {
            var id = newValue.getId();
            if (id != null) {
                if (presentIdentifiers.contains(id)) {
                    throw new IllegalStateException("Attempt to add a container value with an id that already exists: " + newValue.getId());
                }
                presentIdentifiers.add(id);
            } else {
                // ID is null, we can not asume strict modifications anymore
                stopStrictModifications();
            }
        }

        // We check strict modifications again, they could be turn off by previous block, if new value does not have id
        if (!strictModifications) {
            if (newValue.getId() != null) {
                for (PrismContainerValue existingValue : getValues()) {
                    if (existingValue.getId() != null && existingValue.getId().equals(newValue.getId())) {
                        throw new IllegalStateException("Attempt to add a container value with an id that already exists: " + newValue.getId());
                    }
                }
            }
        }
        return super.addInternalExecution(newValue);
    }

    private boolean canAssumeSingleValue() {
        if (getDefinition() != null) {
            return getDefinition().isSingleValue();
        } else {
            return getValues().size() <= 1;
        }
    }

    @Override
    public PrismContainerValue<C> getValue(Long id) {
        for (PrismContainerValue<C> pval: getValues()) {
            if ((id == null && pval.getId() == null) ||
                    id.equals(pval.getId())) {
                return pval;
            }
        }
        return null;
    }

    @Override
    public <T> void setPropertyRealValue(QName propertyName, T realValue) throws SchemaException {
        checkMutable();
        PrismProperty<T> property = findOrCreateProperty(ItemName.fromQName(propertyName));
        property.setRealValue(realValue);
    }

    @Override
    public <X extends Containerable> void setContainerRealValue(QName itemName, X realValue) throws SchemaException {
        checkMutable();
        if (realValue != null) {
            PrismContainer<Containerable> container = findOrCreateContainer(ItemName.fromQName(itemName));
            //noinspection unchecked
            container.setValue(realValue.asPrismContainerValue());
        } else {
            removeContainer(ItemName.fromQName(itemName));
        }
    }

    @Override
    public <T> void setPropertyRealValues(QName propertyName, T... realValues) throws SchemaException {
        checkMutable();
        PrismProperty<T> property = findOrCreateProperty(ItemName.fromQName(propertyName));
        property.setRealValues(realValues);
    }

    @Override
    public <T> T getPropertyRealValue(ItemPath propertyPath, Class<T> type) {
        PrismProperty<T> property = findProperty(propertyPath);
        if (property == null) {
            return null;
        }
        return property.getRealValue(type);
    }

    /**
     * Convenience method. Works only on single-valued containers.
     */
    @Override
    public void add(Item<?,?> item) throws SchemaException {
        checkMutable();
        this.getValue().add(item);
    }

    @Override
    public PrismContainerValue<C> createNewValue() {
        checkMutable();
        PrismContainerValue<C> pValue = createNewValueInternal();
        try {
            // No need to check uniqueness, we know that this value is new and therefore
            // it will change anyway and therefore the check is pointless.
            // However, the check is expensive (especially if there are many values).
            // So we are happy to avoid it.
            addIgnoringEquivalents(pValue);
        } catch (SchemaException e) {
            // This should not happen
            throw new SystemException("Internal Error: "+e.getMessage(),e);
        }
        return pValue;
    }

    /** Can be overridden in subclasses. */
    protected @NotNull PrismContainerValueImpl<C> createNewValueInternal() {
        return new PrismContainerValueImpl<>();
    }

    @Override
    public void mergeValues(PrismContainer<C> other) throws SchemaException {
        mergeValues(other.getValues());
    }

    @Override
    public void mergeValues(Collection<PrismContainerValue<C>> otherValues) throws SchemaException {
        for (PrismContainerValue<C> otherValue : otherValues) {
            mergeValue(otherValue);
        }
    }

    @Override
    public void mergeValue(PrismContainerValue<C> otherValue) throws SchemaException {
        checkMutable();
        Iterator<PrismContainerValue<C>> iterator = getValues().iterator();
        while (iterator.hasNext()) {
            PrismContainerValue<C> thisValue = iterator.next();
            if (thisValue.equals(otherValue)) {
                // Same values, nothing to merge
                return;
            }
            if (thisValue.getId() != null && thisValue.getId().equals(otherValue.getId())) {
                // Different value but same id. New value overwrites.
                iterator.remove();
            }
        }
        PrismContainerValue<C> clonedOtherValue = otherValue.clone();
        if (getDefinition() != null) {
            clonedOtherValue = clonedOtherValue.applyDefinition(getDefinition());
        }
        add(clonedOtherValue);
    }

    /**
     * Remove all empty values
     */
    @Override
    public void trim() {
        Iterator<PrismContainerValue<C>> iterator = getValues().iterator();
        while (iterator.hasNext()) {
            PrismContainerValue<C> pval = iterator.next();
            if (pval.isEmpty()) {
                checkMutable();
                iterator.remove();
            }
        }
    }

    /**
     * Returns applicable property container definition.
     * <p>
     * May return null if no definition is applicable or the definition is not
     * know.
     *
     * @return applicable property container definition
     */
    @Override
    public PrismContainerDefinition<C> getDefinition() {
        return definition;
    }

    /**
     * Sets applicable property container definition.
     *
     * @param definition the definition to set
     */
    @Override
    public void setDefinition(@NotNull PrismContainerDefinition<C> definition) {
        checkMutable();
        checkDefinition(definition);
        for (PrismContainerValue<C> value : getValues()) {
            // TODO reconsider this - sometimes we want to change CTDs, sometimes not
            boolean safeToOverwrite =
                    value.getComplexTypeDefinition() == null
                            || this.definition == null // TODO highly dangerous (the definition might be simply unknown)
                            || this.definition.getComplexTypeDefinition() == null
                            || this.definition.getComplexTypeDefinition().getTypeName().equals(value.getComplexTypeDefinition().getTypeName());
            if (safeToOverwrite) {
                //noinspection rawtypes
                ((PrismContainerValueImpl) value).replaceComplexTypeDefinition(definition.getComplexTypeDefinition());
            }
        }
        this.definition = definition;
    }

    // FIXME
    public void setDefinitionHack(@NotNull PrismContainerDefinition<C> definition) {
        checkMutable();
        checkDefinition(definition);
        this.definition = definition;
    }

    @Override
    public void applyDefinition(@NotNull PrismContainerDefinition<C> definition, boolean force) throws SchemaException {
        checkMutable();
        this.compileTimeClass = definition.getCompileTimeClass();
        super.applyDefinition(definition, force);
    }

    @Override
    public <IV extends PrismValue,ID extends ItemDefinition<?>,I extends Item<IV,ID>> I findItem(QName itemQName, Class<I> type) {
        try {
            return findCreateItem(itemQName, type, false);
        } catch (SchemaException e) {
            // This should not happen
            throw new SystemException("Internal Error: "+e.getMessage(),e);
        }
    }

    /**
     * Returns true if the object and all contained prisms have definitions.
     */
    @Override
    public boolean hasCompleteDefinition() {
        if (getDefinition() == null) {
            return false;
        }
        for (PrismContainerValue<C> cval: getValues()) {
            if (!cval.hasCompleteDefinition()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object find(ItemPath path) {
        if (ItemPath.isEmpty(path)) {
            return this;
        }

        Long id = path.firstToIdOrNull();
        PrismContainerValue<C> cval = findValue(id);
        if (cval == null) {
            return null;
        }
        // descent to the correct value
        ItemPath rest = path.startsWithId() ? path.rest() : path;
        return cval.find(rest);
    }

    @Override
    public <IV extends PrismValue,ID extends ItemDefinition<?>> PartiallyResolvedItem<IV,ID> findPartial(ItemPath path) {
        if (ItemPath.isEmpty(path)) {
            //noinspection unchecked
            return new PartiallyResolvedItem<>((Item<IV, ID>) this, null);
        }

        Long id = path.firstToIdOrNull();
        PrismContainerValue<C> cval = findValue(id);
        if (cval == null) {
            return null;
        }
        // descent to the correct value
        ItemPath rest = path.startsWithId() ? path.rest() : path;
        return cval.findPartial(rest);
    }

    @Override
    public <IV extends PrismValue,ID extends ItemDefinition<?>,I extends Item<IV,ID>> I findCreateItem(QName itemQName, Class<I> type, boolean create) throws SchemaException {
        return ((PrismContainerValueImpl<C>) this.getValue()).findCreateItem(itemQName, type, null, create);
    }

    @Override
    public <IV extends PrismValue,ID extends ItemDefinition<?>,I extends Item<IV,ID>> I findItem(ItemPath path, Class<I> type) {
        try {
            return findCreateItem(path, type, null, false);
        } catch (SchemaException e) {
            // This should not happen
            throw new SystemException("Internal Error:(path="+path+",type="+type+"): "+e.getMessage(),e);
        }
    }

    @Override
    public <IV extends PrismValue,ID extends ItemDefinition<?>> Item<IV,ID> findItem(ItemPath path) {
        try {
            return findCreateItem(path, Item.class, null, false);
        } catch (SchemaException e) {
            // This should not happen
            throw new SystemException("Internal Error:(path="+path+"): "+e.getMessage(),e);
        }
    }

    @Override
    public boolean containsItem(ItemPath itemPath, boolean acceptEmptyItem) throws SchemaException {
        if (itemPath == null || itemPath.isEmpty()) {
            throw new IllegalArgumentException("Empty path specified");
        }

        ItemPath rest = skipFirstId(itemPath);
        for (PrismContainerValue<C> value: getValues()) {
            if (value.containsItem(rest, acceptEmptyItem)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <IV extends PrismValue,ID extends ItemDefinition<?>,I extends Item<IV,ID>> I findCreateItem(ItemPath itemPath, Class<I> type, ID itemDefinition, boolean create) throws SchemaException {
        if (ItemPath.isEmpty(itemPath)) {
            throw new IllegalArgumentException("Empty path specified");
        }
        Long id = itemPath.firstToIdOrNull();
        PrismContainerValue<C> cval = findValue(id);
        if (cval == null) {
            return null;
        }
        // descent to the correct value
        ItemPath rest = itemPath.startsWithId() ? itemPath.rest() : itemPath;
        if (cval instanceof PrismContainerValueDelegator<C> delegator) {
            // FIXME: findCreateItem should probably be part of some API
            cval = delegator.delegate();
        }
        return ((PrismContainerValueImpl<C>) cval).findCreateItem(rest, type, itemDefinition, create);
    }

    @Override
    public PrismContainerValue<C> findValue(long id) {
        for (PrismContainerValue<C> pval : getValues()) {
            if (id == pval.getId()) {
                return pval;
            }
        }
        return null;
    }

    private PrismContainerValue<C> findValue(Long id) {
        if (id == null) {
            if (canAssumeSingleValue()) {
                return this.getValue();
            } else {
                throw new IllegalArgumentException("Attempt to get segment without an ID from a multi-valued container "+ getElementName());
            }
        } else {
            for (PrismContainerValue<C> pval : getValues()) {
                if (id.equals(pval.getId())) {
                    return pval;
                }
            }
            return null;
        }
    }

    @Override
    public <T extends Containerable> PrismContainer<T> findContainer(ItemPath path) {
        return findItem(path, PrismContainer.class);
    }

    public <T extends Containerable> PrismContainer<T> findContainer(QName containerName) {
        return findItem(containerName, PrismContainer.class);
    }

    @Override
    public <T> PrismProperty<T> findProperty(ItemPath path) {
        return findItem(path, PrismProperty.class);
    }

    @Override
    public PrismReference findReference(ItemPath path) {
        return findItem(path, PrismReference.class);
    }

    @Override
    public <IV extends PrismValue,ID extends ItemDefinition<?>,I extends Item<IV,ID>> I findOrCreateItem(
            ItemPath containerPath, Class<I> type) throws SchemaException {
        return findCreateItem(containerPath, type, null, true);
    }

    // The "definition" parameter provides definition of item to create, in case that the container does not have
    // the definition (e.g. in case of "extension" containers)
    @Override
    public <IV extends PrismValue,ID extends ItemDefinition<?>,I extends Item<IV,ID>> I findOrCreateItem(
            ItemPath containerPath, Class<I> type, ID definition) throws SchemaException {
        if (PrismObject.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("It makes no sense to find object in a container (class)");
        }
        if (definition instanceof PrismObjectDefinition<?>) {
            throw new IllegalArgumentException("It makes no sense to find object in a container (definition)");
        }
        return findCreateItem(containerPath, type, definition, true);
    }

    @Override
    public <T extends Containerable> PrismContainer<T> findOrCreateContainer(ItemPath containerPath) throws SchemaException {
        return findCreateItem(containerPath, PrismContainer.class, null, true);
    }

    @Override
    public <T> PrismProperty<T> findOrCreateProperty(ItemPath propertyPath) throws SchemaException {
        return findCreateItem(propertyPath, PrismProperty.class, null, true);
    }

    @Override
    public PrismReference findOrCreateReference(ItemPath propertyPath) throws SchemaException {
        return findCreateItem(propertyPath, PrismReference.class, null, true);
    }

    /**
     * Convenience method. Works only on single-valued containers.
     */
    @Override
    public void remove(Item<?,?> item) {
        this.getValue().remove(item);
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

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV,ID>>
    void removeItem(ItemPath path, Class<I> itemType) {
        Long id = ItemPath.firstToIdOrNull(path);
        PrismContainerValue<C> cval = findValue(id);
        if (cval == null) {
            return;
        }
        if (path == null) {
            throw new IllegalStateException("null path");       // todo
        }
        ((PrismContainerValueImpl<C>) cval).removeItem(skipFirstId(path), itemType);
    }

    // Expects that the "self" path segment is NOT included in the basePath
    // is this method used anywhere?
//    void addItemPathsToList(ItemPath basePath, Collection<ItemPath> list) {
//        boolean addIds = true;
//        if (getDefinition() != null) {
//            if (getDefinition().isSingleValue()) {
//                addIds = false;
//            }
//        }
//        for (PrismContainerValue<V> pval: getValues()) {
//            ItemPath subpath = null;
//            ItemPathSegment segment = null;
//            if (addIds) {
//                subpath = basePath.subPath(new IdItemPathSegment(pval.getId())).subPath(new NameItemPathSegment(getElementName()));
//            } else {
//                subpath = basePath.subPath(new NameItemPathSegment(getElementName()));
//            }
//            pval.addItemPathsToList(subpath, list);
//        }
//    }

    @Override
    public ContainerDelta<C> createDelta() {
        return new ContainerDeltaImpl<>(getPath(), getDefinition());
    }

    @Override
    public ContainerDelta<C> createDelta(ItemPath path) {
        return new ContainerDeltaImpl<>(path, getDefinition());
    }

    @Override
    public boolean isEmpty() {
        for (PrismContainerValue<C> pval : getValues()) {
            if (!pval.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void checkConsistenceInternal(Itemable rootItem, boolean requireDefinitions,
            boolean prohibitRaw, ConsistencyCheckScope scope) {
        checkIds();
        if (scope.isThorough()) {
            // Containers that are from run-time schema cannot have compile-time class.
            if (getDefinition() != null && !getDefinition().isRuntimeSchema()) {
                if (getCompileTimeClass() == null) {
                    throw new IllegalStateException("No compile-time class in "+this+" ("+getPath()+" in "+rootItem+")");
                }
            }
        }
        super.checkConsistenceInternal(rootItem, requireDefinitions, prohibitRaw, scope);
    }

    private void checkIds() {
        Set<Long> oidsUsed = new HashSet<>();
        for (PrismContainerValue value : getValues()) {
            Long id = value.getId();
            if (id != null) {
                if (oidsUsed.contains(id)) {
                    throw new IllegalArgumentException("There are more container values with the id of " + id + " in " + getElementName());
                } else {
                    oidsUsed.add(id);
                }
            }
        }
    }

    @Override
    public void assertDefinitions(boolean tolerateRawValues, Supplier<String> sourceDescriptionSupplier) throws SchemaException {
        super.assertDefinitions(tolerateRawValues, sourceDescriptionSupplier);
        Supplier<String> valueSourceDescriptionSupplier = () -> this + " in " + sourceDescriptionSupplier.get();
        for (PrismContainerValue<C> val: getValues()) {
            val.assertDefinitions(tolerateRawValues, valueSourceDescriptionSupplier);
        }
    }

    @Override
    public ContainerDelta<C> diff(PrismContainer<C> other) {
        return (ContainerDelta<C>) super.diff(other);
    }

    @Override
    public ContainerDelta<C> diff(PrismContainer<C> other, ParameterizedEquivalenceStrategy strategy) {
        return (ContainerDelta<C>) super.diff(other, strategy);
    }

    @Override
    public List<? extends ItemDelta> diffModifications(PrismContainer<C> other, ParameterizedEquivalenceStrategy strategy) {
        List<? extends ItemDelta> itemDeltas = new ArrayList<>();
        diffInternal(other, itemDeltas, false, strategy);
        return itemDeltas;
    }

    @Override
    public PrismContainer<C> clone() {
        return cloneComplex(CloneStrategy.LITERAL_MUTABLE);
    }

    @Override
    public PrismContainer<C> createImmutableClone() {
        return (PrismContainer<C>) super.createImmutableClone();
    }

    @Override
    public @NotNull PrismContainer<C> cloneComplex(@NotNull CloneStrategy strategy) {
        if (isImmutable() && !strategy.mutableCopy()) {
            return FlyweightClonedItem.from(this);
        }

        PrismContainerImpl<C> clone = new PrismContainerImpl<>(getElementName(), getDefinition());
        copyValues(strategy, clone);
        return clone;
    }

    protected void copyValues(@NotNull CloneStrategy strategy, PrismContainerImpl<C> clone) {
        super.copyValues(strategy, clone);
        clone.compileTimeClass = this.compileTimeClass;
        for (PrismContainerValue<C> pval : getValues()) {
            try {
                // No need to check for uniqueness here. If the value is unique in this object, it will also be unique in clone.
                // Not comparing values makes clones faster.
                clone.addInternal(pval.cloneComplex(strategy), false, null);
            } catch (SchemaException e) {
                // This should not happen
                throw new SystemException("Internal Error: "+e.getMessage(),e);
            }
        }
    }

    @Override
    public PrismContainerDefinition<C> deepCloneDefinition(@NotNull DeepCloneOperation operation) {
        PrismContainerDefinition<C> clonedDef = (PrismContainerDefinition<C>) getDefinition().deepClone(operation);
        propagateDeepCloneDefinition(operation, clonedDef);
        setDefinition(clonedDef);
        return clonedDef;
    }

    @Override
    protected void propagateDeepCloneDefinition(@NotNull DeepCloneOperation operation, PrismContainerDefinition<C> clonedDef) {
        for (PrismContainerValue<C> cval: getValues()) {
            ((PrismContainerValueImpl<C>) cval).deepCloneDefinition(operation, clonedDef);
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
            Long id = ItemPath.firstToIdOrNull(path);
            ItemPath rest = skipFirstId(path);
            if (id == null) {
                // visit all values
                for (PrismContainerValue<C> cval: getValues()) {
                    cval.accept(visitor, rest, recursive);
                }
            } else {
                PrismContainerValue<C> cval = findValue(id);
                if (cval != null) {
                    cval.accept(visitor, rest, recursive);
                }
            }
        }
    }

    /**
     * This method ignores some part of the object during comparison (e.g. source demarcation in values)
     * These methods compare the "meaningful" parts of the objects.
     */
    @Override
    public boolean equivalent(Object obj) {
        return equals(obj, ParameterizedEquivalenceStrategy.REAL_VALUE);
    }

    @Override
    public String toString() {
        return getDebugDumpClassName() + "(" + getElementName() + "):"
                + getValues();
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        if (DebugUtil.isDetailedDebugDump()) {
            sb.append(getDebugDumpClassName()).append(": ");
        }
        sb.append(DebugUtil.formatElementName(getElementName()));
        sb.append(": ");
        appendDebugDumpSuffix(sb);
        PrismContainerDefinition<C> def = getDefinition();
        if (DebugUtil.isDetailedDebugDump()) {
            if (def != null) {
                sb.append(" def(");
                sb.append(PrettyPrinter.prettyPrint(def.getTypeName()));
                if (def.isRuntimeSchema()) {
                    sb.append(",runtime");
                }
                if (def.isDynamic()) {
                    sb.append(",dyn");
                }
                if (def.isElaborate()) {
                    sb.append(",elaborate");
                }
                sb.append(")");
            }
        } else {
            if (def != null) {
                if (def.isElaborate()) {
                    sb.append(",elaborate");
                }
            }
        }
        Iterator<PrismContainerValue<C>> i = getValues().iterator();
        if (i.hasNext()) {
            sb.append("\n");
        }
        while (i.hasNext()) {
            PrismContainerValue<C> pval = i.next();
            sb.append(pval.debugDump(indent + 1));
            if (i.hasNext()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Return a human readable name of this class suitable for logs.
     */
    @Override
    protected String getDebugDumpClassName() {
        return "PC";
    }

    public static <V extends PrismContainerValue> void createParentIfNeeded(V value, ItemDefinition definition) throws SchemaException {
        if (value.getParent() != null) {
            return;
        }
        if (!(definition instanceof PrismContainerDefinition)) {
            throw new SchemaException("Missing or invalid definition for a PrismContainer: " + definition);
        }
        PrismContainerImpl<?> rv = (PrismContainerImpl) definition.instantiate();
        rv.add(value);
    }

    /**
     * Optimizes (trims) definition tree by removing any definitions not corresponding to items in this container.
     * Works recursively by sub-containers of this one.
     * USE WITH CARE. Make sure the definitions are not shared by other objects!
     */
    @Override
    public void trimDefinitionTree(Collection<? extends ItemPath> alwaysKeep) {
        PrismContainerDefinition<C> def = getDefinition();
        if (def == null || def.getComplexTypeDefinition() == null) {
            return;
        }
        Set<ItemPath> allPaths = getAllItemPaths(alwaysKeep);
        def.getComplexTypeDefinition().trimTo(allPaths);
        values.forEach(v -> ((PrismContainerValueImpl<C>) v).trimItemsDefinitionsTrees(alwaysKeep));
    }

    // TODO implement more efficiently
    private Set<ItemPath> getAllItemPaths(Collection<? extends ItemPath> alwaysKeep) {
        Set<ItemPath> paths = new HashSet<>(CollectionUtils.emptyIfNull(alwaysKeep));
        this.accept(v -> {
            if (v instanceof PrismValue) {
                paths.add(((PrismValue) v).getPath());
            }
        });
        return paths;
    }

    @Override
    public @NotNull Collection<PrismValue> getAllValues(ItemPath path) {
        if (path.isEmpty()) {
            return Collections.unmodifiableCollection(values);
        }
        if (values.isEmpty()) {
            return List.of();
        } else if (values.size() == 1) {
            return values.get(0).getAllValues(path);
        } else {
            List<PrismValue> rv = new ArrayList<>();
            for (PrismValue prismValue : values) {
                rv.addAll(
                        prismValue.getAllValues(path));
            }
            return rv;
        }
    }

    @Override
    public @NotNull Collection<Item<?, ?>> getAllItems(@NotNull ItemPath path) {
        if (path.isEmpty()) {
            return List.of(this);
        }
        if (values.isEmpty()) {
            return List.of();
        } else if (values.size() == 1) {
            return values.get(0).getAllItems(path);
        } else {
            List<Item<?, ?>> rv = new ArrayList<>();
            for (PrismValue prismValue : values) {
                rv.addAll(
                        prismValue.getAllItems(path));
            }
            return rv;
        }
    }

    public void startStrictModifications() {
        strictModifications = true;
    }

    public boolean useIdentifierIndex() {
        if (!strictModifications) {
            return false;
        }
        if (presentIdentifiers != null) {
            return true;
        }
        if (values.size() < FAST_ID_THRESHOLD) {
            return false;
        }
        presentIdentifiers = new HashSet<>();

        for (var value: values) {
            if (value.getId() == null) {
                // If we have value without identifier, we fallback to original behaviour
                stopStrictModifications();
                return false;
            }
            presentIdentifiers.add(value.getId());
        }
        return true;
    }

    public void stopStrictModifications() {
        strictModifications = false;
        presentIdentifiers = null;
    }
}
