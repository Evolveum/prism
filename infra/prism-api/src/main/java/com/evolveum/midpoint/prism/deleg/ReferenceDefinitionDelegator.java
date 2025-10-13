/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.deleg;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismReference;
import com.evolveum.midpoint.prism.PrismReferenceDefinition;

public interface ReferenceDefinitionDelegator extends ItemDefinitionDelegator<PrismReference>, PrismReferenceDefinition {

    @Override
    PrismReferenceDefinition delegate();

    @Override
    default QName getTargetTypeName() {
        return delegate().getTargetTypeName();
    }

    @Override
    default boolean isComposite() {
        return delegate().isComposite();
    }

    @Override
    default @NotNull PrismReference instantiate() {
        return delegate().instantiate();
    }

    @Override
    default @NotNull PrismReference instantiate(QName name) {
        return delegate().instantiate(name);
    }

}
