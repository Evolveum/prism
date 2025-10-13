/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.util.DefinitionUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

/**
 * MidPoint Object Definition.
 *
 * Objects are storable entities in midPoint.
 *
 * This is mostly just a marker class to identify object boundaries in schema.
 *
 * This class represents schema definition for objects. See {@link Definition}
 * for more details.
 *
 * "Instance" class of this class is MidPointObject, not Object - to avoid
 * confusion with java.lang.Object.
 *
 * *Do not call constructors on this object.* Use {@link DefinitionFactory} instead.
 *
 * @author Radovan Semancik
 */
public class PrismObjectDefinitionImpl<O extends Objectable>
        extends PrismContainerDefinitionImpl<O>
        implements PrismObjectDefinition<O>, PrismObjectDefinition.PrismObjectDefinitionMutator<O> {

    @Serial private static final long serialVersionUID = -8298581031956931008L;

    PrismObjectDefinitionImpl(@NotNull QName itemName, @NotNull ComplexTypeDefinition ctd) {
        //noinspection unchecked
        this(itemName, ctd.getTypeName(), ctd, (Class<O>) ctd.getCompileTimeClass());
    }

    private PrismObjectDefinitionImpl(QName itemName, QName typeName, ComplexTypeDefinition ctd, Class<O> compileTimeClass) {
        // Object definition can only be top-level, hence SCHEMA ROOT parent
        super(itemName, typeName, ctd, compileTimeClass, PrismConstants.VIRTUAL_SCHEMA_ROOT, ctd.schemaLookup());
    }

    @Override
    @NotNull
    public PrismObject<O> instantiate() throws SchemaException {
        if (isAbstract()) {
            throw new SchemaException("Cannot instantiate abstract definition "+this);
        }
        return new PrismObjectImpl<>(getItemName(), this);
    }

    @NotNull
    @Override
    public PrismObject<O> instantiate(QName name) throws SchemaException {
        if (isAbstract()) {
            throw new SchemaException("Cannot instantiate abstract definition "+this);
        }
        name = DefinitionUtil.addNamespaceIfApplicable(name, this.itemName);
        return new PrismObjectImpl<>(name, this);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public @NotNull PrismObjectDefinitionImpl<O> clone() {
        var clone = new PrismObjectDefinitionImpl<>(itemName, typeName, complexTypeDefinition, compileTimeClass);
        clone.copyDefinitionDataFrom(this);
        return clone;
    }

    @Override
    public @NotNull PrismObjectDefinition<?> cloneWithNewType(
            @NotNull QName newTypeName, @NotNull ComplexTypeDefinition newCtd) {
        throw new UnsupportedOperationException("Implement if needed");
    }

    @Override
    public @NotNull PrismObjectDefinition<O> cloneWithNewName(@NotNull ItemName newItemName) {
        throw new UnsupportedOperationException("Implement if needed");
    }

    @Override
    public PrismObjectDefinition<O> deepClone(@NotNull DeepCloneOperation operation) {
        return (PrismObjectDefinition<O>) super.deepClone(operation);
    }

    @Override
    @NotNull
    public PrismObjectDefinition<O> cloneWithNewDefinition(QName newItemName, ItemDefinition<?> newDefinition) {
        return (PrismObjectDefinition<O>) super.cloneWithNewDefinition(newItemName, newDefinition);
    }

    @Override
    public PrismContainerDefinition<?> getExtensionDefinition() {
        return findContainerDefinition(getExtensionQName());
    }

    @Override
    public PrismObjectValue<O> createValue() {
        return new PrismObjectValueImpl<>();
    }


    private ItemName getExtensionQName() {
        String namespace = getItemName().getNamespaceURI();
        return new ItemName(namespace, PrismConstants.EXTENSION_LOCAL_NAME);
    }

    @Override
    public String getDebugDumpClassName() {
        return "POD";
    }

    @Override
    public String getDocClassName() {
        return "object";
    }

    @Override
    public PrismObjectDefinitionMutator<O> mutator() {
        checkMutableOnExposing();
        return this;
    }
}
