/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.deleg;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.evolveum.prism.xml.ns._public.types_3.EvaluationTimeType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.ReferentialIntegrityType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

public interface PrismReferenceValueDelegator extends PrismReferenceValue, PrismValueDelegator {


    PrismReferenceValue delegate();

    @Override
    default String getOid() {
        return delegate().getOid();
    }

    @Override
    default void setOid(String oid) {
        delegate().setOid(oid);
    }

    @Override
    default <O extends Objectable> PrismObject<O> getObject() {
        return delegate().getObject();
    }

    @Override
    default Objectable getObjectable() {
        return delegate().getObjectable();
    }

    @Override
    default void setObject(PrismObject<?> object) {
        delegate().setObject(object);
    }

    @Override
    default QName getTargetType() {
        return delegate().getTargetType();
    }

    @Override
    default void setTargetType(QName targetType) {
        delegate().setTargetType(targetType);
    }

    @Override
    default void setTargetType(QName targetType, boolean allowEmptyNamespace) {
        delegate().setTargetType(targetType, allowEmptyNamespace);
    }

    @Override
    default @Nullable QName determineTargetTypeName() {
        return delegate().determineTargetTypeName();
    }

    @Override
    default PolyString getTargetName() {
        return delegate().getTargetName();
    }

    @Override
    default void setTargetName(PolyString name) {
        delegate().setTargetName(name);
    }

    @Override
    default void setTargetName(PolyStringType name) {
        delegate().setTargetName(name);
    }

    @Override
    default Class<Objectable> getTargetTypeCompileTimeClass() {
        return delegate().getTargetTypeCompileTimeClass();
    }

    @Override
    default QName getRelation() {
        return delegate().getRelation();
    }

    @Override
    default void setRelation(QName relation) {
        delegate().setRelation(relation);
    }

    @Override
    default PrismReferenceValue relation(QName relation) {
        return delegate().relation(relation);
    }

    @Override
    default String getDescription() {
        return delegate().getDescription();
    }

    @Override
    default void setDescription(String description) {
        delegate().setDescription(description);
    }

    @Override
    default SearchFilterType getFilter() {
        return delegate().getFilter();
    }

    @Override
    default void setFilter(SearchFilterType filter) {
        delegate().setFilter(filter);
    }

    @Override
    default EvaluationTimeType getResolutionTime() {
        return delegate().getResolutionTime();
    }

    @Override
    default @NotNull EvaluationTimeType getEffectiveResolutionTime() {
        return delegate().getEffectiveResolutionTime();
    }

    @Override
    default void setResolutionTime(EvaluationTimeType resolutionTime) {
        delegate().setResolutionTime(resolutionTime);
    }

    @Override
    default ReferentialIntegrityType getReferentialIntegrity() {
        return delegate().getReferentialIntegrity();
    }

    @Override
    default void setReferentialIntegrity(ReferentialIntegrityType referentialIntegrity) {
        delegate().setReferentialIntegrity(referentialIntegrity);
    }

    @Override
    default PrismReferenceDefinition getDefinition() {
        return delegate().getDefinition();
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> PartiallyResolvedItem<IV, ID> findPartial(ItemPath path) {
        return delegate().findPartial(path);
    }

    @Override
    default PrismReferenceValue applyDefinition(PrismReferenceDefinition definition, boolean force) throws SchemaException {
        return delegate().applyDefinition(definition, force);
    }

    @Override
    default PrismReferenceValue toCanonical() {
        return delegate().toCanonical();
    }

    @Override
    default Referencable asReferencable() {
        return delegate().asReferencable();
    }

    @Override
    default String debugDump(int indent, boolean expandObject) {
        return delegate().debugDump(indent, expandObject);
    }

    @Override
    default PrismReferenceValue clone() {
        return delegate().clone();
    }

    @Override
    default PrismReferenceValue createImmutableClone() {
        return delegate().createImmutableClone();
    }

    @Override
    default PrismReferenceValue cloneComplex(@NotNull CloneStrategy strategy) {
        return delegate().cloneComplex(strategy);
    }

    @Override
    default Class<?> getRealClass() {
        return delegate().getRealClass();
    }

    @Override
    default @Nullable Referencable getRealValue() {
        return delegate().getRealValue();
    }

    @Experimental
    @Override
    default <I extends Item<?, ?>> I findReferencedItem(ItemPath path, Class<I> type) {
        return delegate().findReferencedItem(path, type);
    }

    @Override
    default boolean acceptVisitor(PrismVisitor visitor) {
        return delegate().acceptVisitor(visitor);
    }

}
