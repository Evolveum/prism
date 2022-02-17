/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.util.DefinitionUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;

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
 * @author Radovan Semancik
 *
 */
public class PrismObjectDefinitionImpl<O extends Objectable> extends PrismContainerDefinitionImpl<O> implements
        MutablePrismObjectDefinition<O> {
    private static final long serialVersionUID = -8298581031956931008L;

    public PrismObjectDefinitionImpl(QName elementName, ComplexTypeDefinition complexTypeDefinition, Class<O> compileTimeClass) {
        // Object definition can only be top-level, hence SCHEMA ROOT parent
        super(elementName, complexTypeDefinition, compileTimeClass, PrismConstants.VIRTUAL_SCHEMA_ROOT);
    }

    @Override
    @NotNull
    public PrismObject<O> instantiate() throws SchemaException {
        if (isAbstract()) {
            throw new SchemaException("Cannot instantiate abstract definition "+this);
        }
        return new PrismObjectImpl<>(getItemName(), this, getPrismContext());
    }

    @NotNull
    @Override
    public PrismObject<O> instantiate(QName name) throws SchemaException {
        if (isAbstract()) {
            throw new SchemaException("Cannot instantiate abstract definition "+this);
        }
        name = DefinitionUtil.addNamespaceIfApplicable(name, this.itemName);
        return new PrismObjectImpl<>(name, this, getPrismContext());
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @NotNull
    @Override
    public PrismObjectDefinitionImpl<O> clone() {
        PrismObjectDefinitionImpl<O> clone = new PrismObjectDefinitionImpl<>(itemName, complexTypeDefinition, compileTimeClass);
        clone.copyDefinitionDataFrom(this);
        return clone;
    }

    @Override
    public PrismObjectDefinition<O> deepClone(@NotNull DeepCloneOperation operation) {
        return (PrismObjectDefinition<O>) super.deepClone(operation);
    }

    @Override
    @NotNull
    public PrismObjectDefinition<O> cloneWithReplacedDefinition(QName itemName, ItemDefinition<?> newDefinition) {
        return (PrismObjectDefinition<O>) super.cloneWithReplacedDefinition(itemName, newDefinition);
    }

    @Override
    public PrismContainerDefinition<?> getExtensionDefinition() {
        return findContainerDefinition(getExtensionQName());
    }

    @Override
    public PrismObjectValue<O> createValue() {
        return new PrismObjectValueImpl<>(getPrismContext());
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
    public MutablePrismObjectDefinition<O> toMutable() {
        checkMutableOnExposing();
        return this;
    }
}
