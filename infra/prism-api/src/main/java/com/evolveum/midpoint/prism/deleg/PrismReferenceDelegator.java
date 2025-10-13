/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.deleg;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ReferenceDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.util.annotation.Experimental;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface PrismReferenceDelegator extends PrismReference, ItemDelegator<PrismReferenceValue,PrismReferenceDefinition> {

    @Override
    PrismReference delegate();

    @Override
    default Referencable getRealValue() {
        return delegate().getRealValue();
    }

    @Override
    default @NotNull Collection<Referencable> getRealValues() {
        return delegate().getRealValues();
    }

    @Override
    default boolean merge(PrismReferenceValue value) {
        return delegate().merge(value);
    }

    @Override
    default String getOid() {
        return delegate().getOid();
    }

    @Override
    default PolyString getTargetName() {
        return delegate().getTargetName();
    }

    @Override
    default PrismReferenceValue findValueByOid(String oid) {
        return delegate().findValueByOid(oid);
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>> PartiallyResolvedItem<IV, ID> findPartial(ItemPath path) {
        return delegate().findPartial(path);
    }

    @Override
    default ReferenceDelta createDelta() {
        return delegate().createDelta();
    }

    @Override
    default ReferenceDelta createDelta(ItemPath path) {
        return delegate().createDelta(path);
    }

    @Deprecated // use copy()
    @Override
    default PrismReference clone() {
        return delegate().clone();
    }

    @Override
    default PrismReference createImmutableClone() {
        return delegate().createImmutableClone();
    }

    @Override
    default @NotNull PrismReference cloneComplex(@NotNull CloneStrategy strategy) {
        return delegate().cloneComplex(strategy);
    }

    @Override
    default String debugDump(int indent) {
        return delegate().debugDump(indent);
    }

    @Experimental
    @Override
    default <I extends Item<?, ?>> I findReferencedItem(ItemPath path, Class<I> type) {
        return delegate().findReferencedItem(path, type);
    }

}
