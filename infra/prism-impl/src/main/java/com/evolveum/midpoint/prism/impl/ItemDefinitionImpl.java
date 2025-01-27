/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import java.io.Serial;
import java.util.Objects;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ItemDefinition.ItemDefinitionLikeBuilder;
import com.evolveum.midpoint.prism.schema.SerializableItemDefinition;

import com.evolveum.midpoint.prism.delta.ItemMerger;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.ItemDefinition.ItemDefinitionMutator;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.QNameUtil;

import org.jetbrains.annotations.Nullable;

/**
 * Abstract item definition in the schema.
 *
 * This is supposed to be a superclass for all item definitions. Items are things
 * that can appear in property containers, which generally means only a property
 * and property container itself. Therefore this is in fact superclass for those
 * two definitions.
 *
 * The definitions represent data structures of the schema. Therefore instances
 * of Java objects from this class represent specific <em>definitions</em> from
 * the schema, not specific properties or objects. E.g the definitions does not
 * have any value.
 *
 * To transform definition to a real property or object use the explicit
 * instantiate() methods provided in the definition classes. E.g. the
 * instantiate() method will create instance of Property using appropriate
 * PropertyDefinition.
 *
 * The convenience methods in Schema are using this abstract class to find
 * appropriate definitions easily.
 *
 * @author Radovan Semancik
 *
 */
public abstract class ItemDefinitionImpl<I extends Item<?, ?>>
        extends DefinitionImpl
        implements ItemDefinition<I>,
        ItemDefinitionMutator,
        ItemDefinitionLikeBuilder,
        SerializableItemDefinition {
    @Serial private static final long serialVersionUID = -2643332934312107274L;

    /** Final because it's sometimes used as a key in maps; moreover, it forms an identity of the definition somehow. */
    @NotNull protected final ItemName itemName;

    private int minOccurs = 1;
    private int maxOccurs = 1;
    private boolean operational = false;

    private boolean alwaysUseForEquals;
    private boolean dynamic;
    private boolean canAdd = true;
    private boolean canRead = true;
    private boolean canModify = true;
    private boolean inherited;
    protected QName substitutionHead;
    protected boolean heterogeneousListItem;
    private PrismReferenceValue valueEnumerationRef;

    private Boolean indexed = null;
    private boolean indexOnly = false;

    private boolean isSearchable = false;

    private final transient SerializationProxy serializationProxy;

    protected ItemProcessing processing;

    // TODO: annotations

    /**
     * TODO review this doc
     *
     * The constructors should be used only occasionally (if used at all).
     * Use the factory methods in the ResourceObjectDefinition instead.
     *
     * @param itemName definition name (element Name)
     * @param typeName type name (XSD complex or simple type)
     */
    ItemDefinitionImpl(@NotNull QName itemName, @NotNull QName typeName) {
        this(itemName, typeName, null);
    }

    ItemDefinitionImpl(@NotNull QName itemName, @NotNull QName typeName, QName definedInType) {
        super(typeName);
        // We use from(ns, localPart) instead of QName, to make sure itemName is internalized and only one instance is always used.
        this.itemName = ItemName.from(itemName.getNamespaceURI(), itemName.getLocalPart()).intern();
        this.serializationProxy = definedInType != null ? SerializationProxy.forItemDef(definedInType, this.itemName) : null;
    }

    protected static boolean useSerializationProxy(boolean localEnabled) {
        return PrismStaticConfiguration.javaSerializationProxiesEnabled() && localEnabled;
    }

    /**
     * Returns name of the defined entity.
     *
     * The name is a name of the entity instance if it is fixed by the schema.
     * E.g. it may be a name of the property in the container that cannot be
     * changed.
     *
     * The name corresponds to the XML element name in the XML representation of
     * the schema. It does NOT correspond to a XSD type name.
     *
     * Name is optional. If name is not set the null value is returned. If name is
     * not set the type is "abstract", does not correspond to the element.
     *
     * @return the name name of the entity or null.
     */
    @Override
    @NotNull
    public ItemName getItemName() {
        return itemName;
    }

    @Override
    public int getMinOccurs() {
        return minOccurs;
    }

    @Override
    public void setMinOccurs(int minOccurs) {
        checkMutable();
        this.minOccurs = minOccurs;
    }

    @Override
    public int getMaxOccurs() {
        return maxOccurs;
    }

    @Override
    public void setMaxOccurs(int maxOccurs) {
        checkMutable();
        this.maxOccurs = maxOccurs;
    }

    @Override
    public boolean isOperational() {
        return operational;
    }

    @Override
    public void setOperational(boolean operational) {
        checkMutable();
        this.operational = operational;
    }

    @Override
    public boolean isAlwaysUseForEquals() {
        return alwaysUseForEquals;
    }

    @Override
    public void setAlwaysUseForEquals(boolean alwaysUseForEquals) {
        checkMutable();
        this.alwaysUseForEquals = alwaysUseForEquals;
    }

    @Override
    public boolean isDynamic() {
        return dynamic;
    }

    @Override
    public void setDynamic(boolean dynamic) {
        checkMutable();
        this.dynamic = dynamic;
    }

    /**
     * Returns true if the property can be read. I.e. if it is returned in objects
     * retrieved from "get", "search" and similar operations.
     */
    @Override
    public boolean canRead() {
        return canRead;
    }

    /**
     * Returns true if the item can be modified. I.e. if it can be changed
     * during a modification of existing object.
     */
    @Override
    public boolean canModify() {
        return canModify;
    }

    /**
     *
     */
    @Override
    public void setReadOnly() {
        checkMutable();
        canAdd = false;
        canRead = true;
        canModify = false;
    }

    @Override
    public void setCanRead(boolean read) {
        checkMutable();
        this.canRead = read;
    }

    @Override
    public void setCanModify(boolean modify) {
        checkMutable();
        this.canModify = modify;
    }

    @Override
    public void setCanAdd(boolean add) {
        checkMutable();
        this.canAdd = add;
    }

    /**
     * Returns true if the item can be added. I.e. if it can be present
     * in the object when a new object is created.
     */
    @Override
    public boolean canAdd() {
        return canAdd;
    }

    @Override
    public QName getSubstitutionHead() {
        return substitutionHead;
    }

    @Override
    public void setSubstitutionHead(QName substitutionHead) {
        checkMutable();
        this.substitutionHead = substitutionHead;
    }

    @Override
    public boolean isHeterogeneousListItem() {
        return heterogeneousListItem;
    }

    @Override
    public void setHeterogeneousListItem(boolean heterogeneousListItem) {
        checkMutable();
        this.heterogeneousListItem = heterogeneousListItem;
    }

    @Override
    public PrismReferenceValue getValueEnumerationRef() {
        return valueEnumerationRef;
    }

    @Override
    public void setValueEnumerationRef(PrismReferenceValue valueEnumerationRef) {
        checkMutable();
        this.valueEnumerationRef = valueEnumerationRef;
    }

    @Override
    public boolean isValidFor(
            @NotNull QName elementQName,
            @NotNull Class<? extends ItemDefinition<?>> clazz,
            boolean caseInsensitive) {
        return clazz.isAssignableFrom(this.getClass())
                && QNameUtil.match(elementQName, getItemName(), caseInsensitive);
    }

    @Override
    public <T extends ItemDefinition<?>> T findItemDefinition(@NotNull ItemPath path, @NotNull Class<T> clazz) {
        //noinspection unchecked
        return LivePrismItemDefinition.matchesThisDefinition(path, clazz, this) ? (T) this : null;
    }

    @NotNull
    @Override
    public abstract ItemDefinition<I> clone();

    protected void copyDefinitionDataFrom(ItemDefinition<I> source) {
        super.copyDefinitionDataFrom(source);
        this.minOccurs = source.getMinOccurs();
        this.maxOccurs = source.getMaxOccurs();
        this.dynamic = source.isDynamic();
        this.canAdd = source.canAdd();
        this.canRead = source.canRead();
        this.canModify = source.canModify();
        this.operational = source.isOperational();
        this.valueEnumerationRef = source.getValueEnumerationRef(); // clone?
        this.indexed = source.isIndexed();
        this.indexOnly = source.isIndexOnly();
    }

    /**
     * Make a deep clone, cloning all the sub-items and definitions.
     */
    @Override
    public ItemDefinition<I> deepClone(@NotNull DeepCloneOperation operation) {
        return clone();
    }

    @Override
    public void revive(PrismContext prismContext) {
        // NOOP
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ItemDefinitionImpl<?> that = (ItemDefinitionImpl<?>) o;
        return processing == that.processing
                && minOccurs == that.minOccurs
                && maxOccurs == that.maxOccurs
                && operational == that.operational
                && dynamic == that.dynamic
                && canAdd == that.canAdd
                && canRead == that.canRead
                && canModify == that.canModify
                && inherited == that.inherited
                && heterogeneousListItem == that.heterogeneousListItem
                && indexed == that.indexed
                && indexOnly == that.indexOnly
                && itemName.equals(that.itemName)
                && Objects.equals(substitutionHead, that.substitutionHead)
                && Objects.equals(valueEnumerationRef, that.valueEnumerationRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), itemName, minOccurs, maxOccurs, operational, dynamic, canAdd, canRead, canModify,
                inherited, substitutionHead, heterogeneousListItem, valueEnumerationRef, indexed, indexOnly);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDebugDumpClassName());
        sb.append(getMutabilityFlag());
        sb.append(":");
        sb.append(PrettyPrinter.prettyPrint(getItemName()));
        sb.append(" ");
        debugDumpShortToString(sb);
        return sb.toString();
    }

    /**
     * Used in debugDumping items. Does not need to have name in it as item already has it. Does not need
     * to have class as that is just too much info that is almost anytime pretty obvious anyway.
     */
    @Override
    public void debugDumpShortToString(StringBuilder sb) {
        sb.append(PrettyPrinter.prettyPrint(getTypeName()));
        debugMultiplicity(sb);
        debugFlags(sb);
    }

    private void debugMultiplicity(StringBuilder sb) {
        sb.append("[");
        sb.append(getMinOccurs());
        sb.append(",");
        sb.append(getMaxOccurs());
        sb.append("]");
    }

    public String debugDisplayOrder() {
        return getDisplayOrder() != null ? Integer.toString(getDisplayOrder()) : "";
    }

    public String debugMultiplicity() {
        StringBuilder sb = new StringBuilder();
        debugMultiplicity(sb);
        return sb.toString();
    }

    private void debugFlags(StringBuilder sb) {
        if (isIgnored()) {
            sb.append(",ignored");
        }
        if (isDynamic()) {
            sb.append(",dyn");
        }
        if (isElaborate()) {
            sb.append(",elaborate");
        }
        extendToString(sb);
    }

    public String debugFlags() {
        StringBuilder sb = new StringBuilder();
        debugFlags(sb);
        // This starts with a colon, we do not want it here
        if (!sb.isEmpty()) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    protected void extendToString(StringBuilder sb) {
        sb.append(",");
        if (canRead()) {
            sb.append("R");
        } else {
            sb.append("-");
        }
        if (canAdd()) {
            sb.append("A");
        } else {
            sb.append("-");
        }
        if (canModify()) {
            sb.append("M");
        } else {
            sb.append("-");
        }
        if (isRuntimeSchema()) {
            sb.append(",runtime");
        }
        if (isOperational()) {
            sb.append(",oper");
        }
    }

    @Override
    public boolean isInherited() {
        return inherited;
    }

    @Override
    public void setInherited(boolean inherited) {
        checkMutable();
        this.inherited = inherited;
    }

    @Override
    public Boolean isIndexed() {
        return indexed;
    }

    @Override
    public void setIndexed(Boolean indexed) {
        checkMutable();
        this.indexed = indexed;
    }

    @Override
    public boolean isIndexOnly() {
        return indexOnly;
    }

    @Override
    public void setIndexOnly(boolean indexOnly) {
        checkMutable();
        this.indexOnly = indexOnly;
        if (indexOnly) {
            setIndexed(true);
        }
    }

    @Override
    public boolean isSearchable() {
        return isSearchable;
    }

    @Override
    public void setSearchable(boolean searchable) {
        isSearchable = searchable;
    }

    protected Object writeReplace() {
        return useSerializationProxy(serializationProxy != null) ? serializationProxy : this;
    }

    public ItemProcessing getProcessing() {
        return processing;
    }

    public void setProcessing(ItemProcessing itemProcessing) {
        this.processing = itemProcessing;
    }

    public ItemDefinition<?> getObjectBuilt() {
        return this;
    }

    @Override
    public @Nullable ItemMerger getMergerInstance(@NotNull MergeStrategy strategy, @Nullable OriginMarker originMarker) {
        return PrismContext.get().itemMergerFactory().createMerger(this, strategy, originMarker);
    }
}
