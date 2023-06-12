/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.metadata.MidpointOriginMetadata;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.RawType;

import com.google.common.annotations.VisibleForTesting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author semancik
 *
 */
public interface PrismValue extends Visitable, PathVisitable, Serializable, DebugDumpable, Revivable, Freezable, PrismContextSensitive, MidpointOriginMetadata {      // todo ShortDumpable?

    @VisibleForTesting
    void setPrismContext(PrismContext prismContext);

    Map<String, Object> getUserData();

    Object getUserData(@NotNull String key);

    void setUserData(@NotNull String key, Object value);

    Itemable getParent();

    void setParent(Itemable parent);

    /**
     * To be reconsidered.
     */
    @Deprecated
    default Optional<ValueMetadata> valueMetadata() {
        return Optional.of(getValueMetadata());
    }

    /**
     * Maybe it is better to expect empty value metadata if these are absent.
     * Client code would be simpler. HIGHLY EXPERIMENTAL.
     */
    @Experimental
    @NotNull
    ValueMetadata getValueMetadata();

    /**
     * Returns value metadata as typed PrismContainer.
     * Useful for lazy clients because of the type matching.
     */
    @Experimental
    @NotNull
    default <C extends Containerable> PrismContainer<C> getValueMetadataAsContainer() {
        //noinspection unchecked
        return (PrismContainer<C>) getValueMetadata();
    }

    /**
     * @return True if this value has any metadata (i.e. the metadata container has any value).
     *
     * TODO Or should we accept only non-empty values? Probably not.
     */
    boolean hasValueMetadata();

    /**
     * Sets metadata for this value.
     */
    @Experimental
    void setValueMetadata(ValueMetadata valueMetadata);

    /**
     * Sets metadata for this value.
     */
    @Experimental
    void setValueMetadata(PrismContainer<?> valueMetadata);

    /**
     * Sets metadata from this value (from PCV). To be removed (most probably).
     */
    @Experimental
    void setValueMetadata(Containerable realValue) throws SchemaException;

    @NotNull
    ItemPath getPath();

    /**
     * Used when we are removing the value from the previous parent.
     * Or when we know that the previous parent will be discarded and we
     * want to avoid unnecessary cloning.
     */
    void clearParent();

    void applyDefinition(ItemDefinition definition) throws SchemaException;

    void applyDefinition(ItemDefinition definition, boolean force) throws SchemaException;

    /**
     * Recompute the value or otherwise "initialize" it before adding it to a prism tree.
     * This may as well do nothing if no recomputing or initialization is needed.
     */
    void recompute();

    void recompute(PrismContext prismContext);

    @Override
    void accept(Visitor visitor);

    @Override
    void accept(Visitor visitor, ItemPath path, boolean recursive);

    void checkConsistenceInternal(Itemable rootItem, boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope);

    /**
     * Returns true if this and other value represent the same value.
     * E.g. if they have the same IDs, OIDs or it is otherwise know
     * that they "belong together" without a deep examination of the
     * values.
     *
     * @param lax If we can reasonably assume that the two values belong together even if they don't have the same ID,
     *            e.g. if they both belong to single-valued parent items. This is useful e.g. when comparing
     *            multi-valued containers. But can cause problems when we want to be sure we are removing the correct
     *            value.
     */
    boolean representsSameValue(PrismValue other, boolean lax);

    /** Currently doing nothing. */
    void normalize();

    /**
     * Literal clone.
     */
    PrismValue clone();

    PrismValue createImmutableClone();

    /**
     * Complex clone with different cloning strategies.
     * @see CloneStrategy
     */
    PrismValue cloneComplex(CloneStrategy strategy);

    int hashCode(@NotNull EquivalenceStrategy equivalenceStrategy);

    int hashCode(@NotNull ParameterizedEquivalenceStrategy equivalenceStrategy);

    boolean equals(PrismValue otherValue, @NotNull EquivalenceStrategy strategy);

    boolean equals(PrismValue otherValue, @NotNull ParameterizedEquivalenceStrategy strategy);

    /**
     * Assumes matching representations. I.e. it assumes that both this and otherValue represent the same instance of item.
     * E.g. the container with the same ID.
     */
    Collection<? extends ItemDelta> diff(PrismValue otherValue, ParameterizedEquivalenceStrategy strategy);

    @Nullable
    Class<?> getRealClass();

    @Experimental // todo reconsider method name
    default boolean hasRealClass() {
        return getRealClass() != null;
    }

    /**
     * Returns the statically-typed "real value".
     *
     * TODO specify when exactly the returned value can be null.
     *
     * TODO decide for containers: they throw an exception if the value is not statically typed.
     */
    @Nullable
    <T> T getRealValue();

    @Nullable
    @Experimental // todo reconsider method name
    default Object getRealValueOrRawType(PrismContext prismContext) {
        if (hasRealClass() && getValueMetadata().isEmpty()) {
            return getRealValue();
        } else {
            return new RawType(this, getTypeName(), prismContext);
        }
    }

    @Nullable
    @Experimental
    default Object getRealValueIfExists() {
        if (hasRealClass()) {
            return getRealValue();
        } else {
            return null;
        }
    }

    // Returns a root of PrismValue tree. For example, if we have a AccessCertificationWorkItemType that has a parent (owner)
    // of AccessCertificationCaseType, which has a parent of AccessCertificationCampaignType, this method returns the PCV
    // of AccessCertificationCampaignType.
    //
    // Generally, this method returns either "this" (PrismValue) or a PrismContainerValue.
    @NotNull PrismValue getRootValue();

    PrismContainerValue<?> getParentContainerValue();

    QName getTypeName();

    /** See {@link Item#getAllValues(ItemPath)}. */
    @NotNull Collection<PrismValue> getAllValues(ItemPath path);

    /** See {@link Item#getAllItems(ItemPath)}. Here the path is never empty. */
    @NotNull Collection<Item<?, ?>> getAllItems(@NotNull ItemPath path);

    boolean isRaw();

    boolean isEmpty();

    String toHumanReadableString();

//    // todo hide from public
//    void diffMatchingRepresentation(PrismValue otherValue,
//            Collection<? extends ItemDelta> deltas, boolean ignoreMetadata, boolean isLiteral);

    Object find(ItemPath path);

    /**
     * @return True if the value is transient, so it won't be serialized if serialization
     * of transient value is disabled.
     */
    @Experimental
    boolean isTransient();

    @Experimental
    void setTransient(boolean value);

    /** Ignores untyped values (considers them non-matching). Supports non-static types. (May be slower.) */
    @Experimental
    default boolean isOfType(@NotNull QName expectedTypeName) {
        QName actualTypeName = Objects.requireNonNullElse(getTypeName(), DOMUtil.XSD_ANYTYPE);
        return PrismContext.get().getSchemaRegistry().isAssignableFromGeneral(expectedTypeName, actualTypeName);
    }

    default @NotNull PrismValue cloneIfImmutable() {
        return isImmutable() ? clone() : this;
    }

    /**
     * Returns `true` if this value represents a {@link PrismObject} value.
     *
     * Temporary implementation that uses real value to do the check, as {@link PrismObjectValue} can mask itself
     * as a {@link PrismContainerValue}, at least for now.
     */
    default boolean isObjectable() {
        return getRealValueIfExists() instanceof Objectable;
    }
}
