/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.*;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

/**
 * TODO document
 */
public class SimpleTypeDefinitionImpl extends TypeDefinitionImpl
        implements SimpleTypeDefinition, TypeDefinition.TypeDefinitionMutator, SimpleTypeDefinition.SimpleTypeDefinitionBuilder, PrismPresentationDefinition.Mutable {

    private final QName baseTypeName;
    private final DerivationMethod derivationMethod; // usually RESTRICTION

    public SimpleTypeDefinitionImpl(QName typeName, QName baseTypeName, DerivationMethod derivationMethod) {
        super(typeName);
        this.baseTypeName = baseTypeName;
        this.derivationMethod = derivationMethod;
    }

    @Override
    public void revive(PrismContext prismContext) {
    }

    @Override
    public String getDebugDumpClassName() {
        return "STD";
    }

    @Override
    public String getDocClassName() {
        return "simple type";
    }

    @Override
    public QName getBaseTypeName() {
        return baseTypeName;
    }

    @Override
    public DerivationMethod getDerivationMethod() {
        return derivationMethod;
    }

    @Override
    public Class<?> getTypeClass() {
        return PrismContext.get().getSchemaRegistry().determineClassForType(getTypeName());
    }

    @NotNull
    @Override
    public SimpleTypeDefinitionImpl clone() {
        SimpleTypeDefinitionImpl clone = new SimpleTypeDefinitionImpl(typeName, baseTypeName, derivationMethod);
        clone.copyDefinitionDataFrom(this);
        return clone;
    }

    @Override
    public TypeDefinitionMutator mutator() {
        checkMutableOnExposing();
        return this;
    }

    @Override
    public SimpleTypeDefinition getObjectBuilt() {
        return this;
    }
}
