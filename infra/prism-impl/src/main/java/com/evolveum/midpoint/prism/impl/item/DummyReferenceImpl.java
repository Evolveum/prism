/*
 * Copyright (c) 2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.item;

import java.util.Collection;

import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ReferenceDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;

/**
 * @author semancik
 *
 */
public class DummyReferenceImpl extends DummyItem<PrismReferenceValue,PrismReferenceDefinition, PrismReference> implements PrismReference {

    private static final long serialVersionUID = 1L;

    public DummyReferenceImpl(PrismReference realReference, @NotNull ItemPath path) {
        super(realReference, path);
    }

    @Override
    public Referencable getRealValue() {
        return delegate().getRealValue();
    }

    @Override
    public <X> X getRealValue(Class<X> type) {
        return delegate().getRealValue(type);
    }

    @Override
    public <X> X[] getRealValuesArray(Class<X> type) {
        return delegate().getRealValuesArray(type);
    }

    @Override
    @NotNull
    public Collection<Referencable> getRealValues() {
        return delegate().getRealValues();
    }

    @Override
    public void addIgnoringEquivalents(@NotNull PrismReferenceValue newValue) throws SchemaException {
        delegate().addIgnoringEquivalents(newValue);
    }

    @Override
    public boolean merge(PrismReferenceValue value) {
        return delegate().merge(value);
    }

    @Override
    public String getOid() {
        return delegate().getOid();
    }

    @Override
    public PolyString getTargetName() {
        return delegate().getTargetName();
    }

    @Override
    public PrismReferenceValue findValueByOid(String oid) {
        return delegate().findValueByOid(oid);
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition> PartiallyResolvedItem<IV, ID> findPartial(
            ItemPath path) {
        return delegate().findPartial(path);
    }

    @Override
    public ReferenceDelta createDelta() {
        return delegate().createDelta();
    }

    @Override
    public ReferenceDelta createDelta(ItemPath path) {
        return delegate().createDelta(path);
    }

    @Override
    public PrismReference clone() {
        return delegate().clone();
    }

    @Override
    public PrismReference createImmutableClone() {
        return delegate().createImmutableClone();
    }

    @Override
    public PrismReference cloneComplex(CloneStrategy strategy) {
        return delegate().cloneComplex(strategy);
    }

    @Override
    public PrismReferenceValue getValue() {
        return delegate().getValue();
    }

    @Override
    public String getHelp() {
        return delegate().getHelp();
    }

    @Override
    public <I extends Item<?, ?>> I findReferencedItem(ItemPath dereferencePath, Class<I> type) {
        return delegate().findReferencedItem(dereferencePath, type);
    }

}
