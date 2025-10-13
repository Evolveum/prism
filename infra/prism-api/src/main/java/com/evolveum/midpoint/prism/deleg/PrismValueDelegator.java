/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.deleg;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.SchemaLookup;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Map;

public interface PrismValueDelegator extends PrismValue {

    PrismValue delegate();

    default Map<String, Object> getUserData() {
        return delegate().getUserData();
    }

    default Object getUserData(@NotNull String key) {
        return delegate().getUserData(key);
    }

    default void setUserData(@NotNull String key, Object value) {
        delegate().setUserData(key, value);
    }

    default Itemable getParent() {
        return delegate().getParent();
    }

    default void setParent(Itemable parent) {
        delegate().setParent(parent);
    }

    default @NotNull ValueMetadata getValueMetadata() {
        return delegate().getValueMetadata();
    }

    default @Nullable ValueMetadata getValueMetadataIfExists() {
        return delegate().getValueMetadataIfExists();
    }

    @Experimental
    default @NotNull <C extends Containerable> PrismContainer<C> getValueMetadataAsContainer() {
        return delegate().getValueMetadataAsContainer();
    }

    default boolean hasValueMetadata() {
        return delegate().hasValueMetadata();
    }

    @Experimental
    default void setValueMetadata(ValueMetadata valueMetadata) {
        delegate().setValueMetadata(valueMetadata);
    }

    @Experimental
    default void setValueMetadata(PrismContainer<?> valueMetadata) {
        delegate().setValueMetadata(valueMetadata);
    }

    @Experimental
    default void setValueMetadata(Containerable realValue) throws SchemaException {
        delegate().setValueMetadata(realValue);
    }

    default void deleteValueMetadata() {
        delegate().deleteValueMetadata();
    }

    default @NotNull ItemPath getPath() {
        return delegate().getPath();
    }

    default void clearParent() {
        delegate().clearParent();
    }

    default PrismValue applyDefinition(@NotNull ItemDefinition<?> definition) throws SchemaException {
        return delegate().applyDefinition(definition);
    }

    default void applyDefinitionLegacy(@NotNull ItemDefinition<?> definition) throws SchemaException {
        delegate().applyDefinitionLegacy(definition);
    }

    default PrismValue applyDefinition(@NotNull ItemDefinition<?> definition, boolean force) throws SchemaException {
        return delegate().applyDefinition(definition, force);
    }

    default void applyDefinitionLegacy(@NotNull ItemDefinition<?> definition, boolean force) throws SchemaException {
        delegate().applyDefinitionLegacy(definition, force);
    }

    default void recompute() {
        delegate().recompute();
    }

    default void recompute(PrismContext prismContext) {
        delegate().recompute(prismContext);
    }

    default void accept(Visitor visitor) {
        delegate().accept(visitor);
    }

    default void accept(Visitor visitor, ItemPath path, boolean recursive) {
        delegate().accept(visitor, path, recursive);
    }

    default void checkConsistenceInternal(Itemable rootItem, boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope) {
        delegate().checkConsistenceInternal(rootItem, requireDefinitions, prohibitRaw, scope);
    }

    default boolean representsSameValue(PrismValue other, EquivalenceStrategy strategy, boolean lax) {
        return delegate().representsSameValue(other, strategy, lax);
    }

    default void normalize() {
        delegate().normalize();
    }

    default PrismValue clone() {
        return delegate().clone();
    }

    default PrismValue createImmutableClone() {
        return delegate().createImmutableClone();
    }

    default PrismValue cloneComplex(@NotNull CloneStrategy strategy) {
        return delegate().cloneComplex(strategy);
    }

    default int hashCode(@NotNull EquivalenceStrategy equivalenceStrategy) {
        return delegate().hashCode(equivalenceStrategy);
    }

    default int hashCode(@NotNull ParameterizedEquivalenceStrategy equivalenceStrategy) {
        return delegate().hashCode(equivalenceStrategy);
    }

    default boolean equals(PrismValue otherValue, @NotNull EquivalenceStrategy strategy) {
        return delegate().equals(otherValue, strategy);
    }

    default boolean equals(PrismValue otherValue, @NotNull ParameterizedEquivalenceStrategy strategy) {
        return delegate().equals(otherValue, strategy);
    }

    default Collection<? extends ItemDelta> diff(PrismValue otherValue, ParameterizedEquivalenceStrategy strategy) {
        return delegate().diff(otherValue, strategy);
    }

    default @Nullable Class<?> getRealClass() {
        return delegate().getRealClass();
    }

    @Experimental
    default boolean hasRealClass() {
        return delegate().hasRealClass();
    }

    default <T> @Nullable T getRealValue() {
        return delegate().getRealValue();
    }

    @Experimental
    default @Nullable Object getRealValueOrRawType() {
        return delegate().getRealValueOrRawType();
    }

    @Experimental
    default @Nullable Object getRealValueIfExists() {
        return delegate().getRealValueIfExists();
    }

    default @NotNull PrismValue getRootValue() {
        return delegate().getRootValue();
    }

    default @Nullable Objectable getRootObjectable() {
        return delegate().getRootObjectable();
    }

    default <T> @Nullable T getNearestValueOfType(@NotNull Class<T> type) {
        return delegate().getNearestValueOfType(type);
    }

    default PrismContainerValue<?> getParentContainerValue() {
        return delegate().getParentContainerValue();
    }

    default QName getTypeName() {
        return delegate().getTypeName();
    }

    default @NotNull Collection<PrismValue> getAllValues(ItemPath path) {
        return delegate().getAllValues(path);
    }

    default @NotNull Collection<Item<?, ?>> getAllItems(@NotNull ItemPath path) {
        return delegate().getAllItems(path);
    }

    default boolean isRaw() {
        return delegate().isRaw();
    }

    default boolean isEmpty() {
        return delegate().isEmpty();
    }

    default String toHumanReadableString() {
        return delegate().toHumanReadableString();
    }

    default Object find(ItemPath path) {
        return delegate().find(path);
    }

    @Experimental
    default boolean isTransient() {
        return delegate().isTransient();
    }

    @Experimental
    default void setTransient(boolean value) {
        delegate().setTransient(value);
    }

    @Experimental
    default boolean isOfType(@NotNull QName expectedTypeName) {
        return delegate().isOfType(expectedTypeName);
    }

    default @NotNull PrismValue cloneIfImmutable() {
        return delegate().cloneIfImmutable();
    }

    default boolean isObjectable() {
        return delegate().isObjectable();
    }

    default SchemaContext getSchemaContext() {
        return delegate().getSchemaContext();
    }

    default SchemaLookup schemaLookup() {
        return delegate().schemaLookup();
    }

    default boolean acceptVisitor(PrismVisitor visitor) {
        return delegate().acceptVisitor(visitor);
    }

    default String debugDump() {
        return delegate().debugDump();
    }

    default String debugDump(int indent) {
        return delegate().debugDump(indent);
    }

    default Object debugDumpLazily() {
        return delegate().debugDumpLazily();
    }

    default Object debugDumpLazily(int indent) {
        return delegate().debugDumpLazily(indent);
    }

    default Objectable getOriginObject() {
        return delegate().getOriginObject();
    }

    default OriginType getOriginType() {
        return delegate().getOriginType();
    }

    default void setOriginType(OriginType type) {
        delegate().setOriginType(type);
    }

    default void setOriginObject(Objectable source) {
        delegate().setOriginObject(source);
    }

}

