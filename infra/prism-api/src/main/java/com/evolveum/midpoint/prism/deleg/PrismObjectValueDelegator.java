/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.deleg;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

public interface PrismObjectValueDelegator<O extends Objectable> extends PrismContainerValueDelegator<O>, PrismObjectValue<O> {

    @Override
    PrismObjectValue<O> delegate();

    @Override
    default String getOid() {
        return delegate().getOid();
    }

    @Override
    default void setOid(String oid) {
        delegate().setOid(oid);
    }

    @Override
    default String getVersion() {
        return delegate().getVersion();
    }

    @Override
    default void setVersion(String version) {
        delegate().setVersion(version);
    }

    @Override
    default O asObjectable() {
        return delegate().asObjectable();
    }

    @Override
    default PrismObject<O> asPrismObject() {
        return delegate().asPrismObject();
    }

    @Override
    default PolyString getName() {
        return delegate().getName();
    }

    @Override
    default PrismContainer<?> getExtension() {
        return delegate().getExtension();
    }

    @Override
    default PrismObjectValue<O> clone() {
        return delegate().clone();
    }

    @Override
    default PrismObjectValue<O> cloneComplex(@NotNull CloneStrategy strategy) {
        return delegate().cloneComplex(strategy);
    }

    @Override
    default boolean equivalent(PrismContainerValue<?> other) {
        return delegate().equivalent(other);
    }


    @Override
    default String toHumanReadableString() {
        return delegate().toHumanReadableString();
    }

    @Override
    default PrismContainer<O> asSingleValuedContainer(@NotNull QName itemName) throws SchemaException {
        return delegate().asSingleValuedContainer(itemName);
    }

}
