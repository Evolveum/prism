/*
 * Copyright (c) 2010-2015 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import java.util.Collection;

import com.evolveum.midpoint.prism.delta.ReferenceDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.util.CloneUtil;
import com.evolveum.midpoint.util.annotation.Experimental;

import org.jetbrains.annotations.NotNull;

/**
 * Object Reference is a property that describes reference to an object. It is
 * used to represent association between objects. For example reference from
 * User object to Account objects that belong to the user. The reference is a
 * simple uni-directional link using an OID as an identifier.
 *
 * This type should be used for all object references so the implementations can
 * detect them and automatically resolve them.
 *
 * @author semancik
 *
 */
public interface PrismReference extends Item<PrismReferenceValue,PrismReferenceDefinition> {
    @Override
    Referencable getRealValue();

    @NotNull
    @Override
    Collection<Referencable> getRealValues();

    boolean merge(PrismReferenceValue value);

    String getOid();

    PolyString getTargetName();

    PrismReferenceValue findValueByOid(String oid);

    @Override
    <IV extends PrismValue,ID extends ItemDefinition<?>> PartiallyResolvedItem<IV,ID> findPartial(ItemPath path);

    @Override
    ReferenceDelta createDelta();

    @Override
    ReferenceDelta createDelta(ItemPath path);

    @Override
    PrismReference clone();

    @Override
    PrismReference createImmutableClone();

    @Override
    @NotNull
    PrismReference cloneComplex(@NotNull CloneStrategy strategy);

    @Override
    default @NotNull PrismReference copy() {
        return cloneComplex(CloneStrategy.LITERAL_ANY);
    }

    default @NotNull PrismReference mutableCopy() {
        return cloneComplex(CloneStrategy.LITERAL_MUTABLE);
    }

    default @NotNull PrismReference immutableCopy() {
        return CloneUtil.immutableCopy(this);
    }

    @Override
    String toString();

    @Override
    String debugDump(int indent);

    /**
     * Tries to find referenced path (path starting with object derefence)
     * in-memory.
     *
     * This works only for single-value references, whose value also contains embedded object.
     * The search is performed on embedded object.
     *
     */
    @Experimental
    <I extends Item<?,?>> I findReferencedItem(ItemPath path, Class<I> type);
}
