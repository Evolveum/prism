/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.deleg;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.TypeDefinition;

public interface TypeDefinitionDelegator extends DefinitionDelegator, TypeDefinition {

    @Override
    TypeDefinition delegate();

    @Override
    default @NotNull QName getTypeName() {
        return delegate().getTypeName();
    }

    @Override
    default @Nullable Class<?> getCompileTimeClass() {
        return delegate().getCompileTimeClass();
    }

    @Override
    default @Nullable QName getSuperType() {
        return delegate().getSuperType();
    }

    @Override
    default @NotNull Collection<TypeDefinition> getStaticSubTypes() {
        return delegate().getStaticSubTypes();
    }

    @Override
    default Integer getInstantiationOrder() {
        return delegate().getInstantiationOrder();
    }

    @Override
    default boolean canRepresent(QName typeName) {
        return delegate().canRepresent(typeName);
    }

    @Override
    default String getHelp() {
        return delegate().getHelp();
    }
}
