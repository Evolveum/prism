/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.schema.SchemaLookup;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.*;

public class DefinitionFactoryImpl implements DefinitionFactory {

    private final SchemaLookup lookup;

    public DefinitionFactoryImpl(SchemaLookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public ComplexTypeDefinitionImpl newComplexTypeDefinition(QName name) {
        return new ComplexTypeDefinitionImpl(name);
    }

    @Override
    public <T> PrismPropertyDefinitionImpl<T> newPropertyDefinition(QName name, QName typeName) {
        return new PrismPropertyDefinitionImpl<>(name, typeName);
    }

    public <T> PrismPropertyDefinitionImpl<T> newPropertyDefinition(QName name, QName typeName, QName definedInType) {
        return new PrismPropertyDefinitionImpl<>(name, typeName, definedInType);
    }

    @Override
    public PrismReferenceDefinition newReferenceDefinition(QName name, QName typeName) {
        return new PrismReferenceDefinitionImpl(name, typeName);
    }

    //region Containers and objects
    @Override
    public @NotNull PrismContainerDefinition<?> newContainerDefinitionWithoutTypeDefinition(
            @NotNull QName name, @NotNull QName typeName) {
        return  new PrismContainerDefinitionImpl<>(name, typeName, lookup);
    }

    @Override
    public <C extends Containerable> @NotNull PrismContainerDefinitionImpl<C> newContainerDefinition(
            @NotNull QName name, @NotNull ComplexTypeDefinition ctd) {
        return new PrismContainerDefinitionImpl<>(name, ctd);
    }

    public <C extends Containerable> @NotNull PrismContainerDefinitionImpl<C> newContainerDefinition(
            @NotNull QName name, @NotNull ComplexTypeDefinition ctd, @NotNull QName definedInType) {
        return new PrismContainerDefinitionImpl<>(name, ctd, definedInType);
    }

    public <O extends Objectable> @NotNull PrismObjectDefinitionImpl<O> newObjectDefinition(
            @NotNull QName name, @NotNull ComplexTypeDefinition ctd) {
        return new PrismObjectDefinitionImpl<>(name, ctd);
    }

    //endregion
}
