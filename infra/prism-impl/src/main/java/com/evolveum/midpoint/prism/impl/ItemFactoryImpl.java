/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.item.DummyContainerImpl;
import com.evolveum.midpoint.prism.impl.item.DummyPropertyImpl;
import com.evolveum.midpoint.prism.impl.item.DummyReferenceImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

/**
 *
 */
public class ItemFactoryImpl implements ItemFactory {

    @Override
    public <T> PrismProperty<T> createProperty(QName itemName) {
        return new PrismPropertyImpl<>(itemName);
    }

    @Override
    public <T> PrismProperty<T> createProperty(QName itemName, PrismPropertyDefinition<T> definition) {
        return new PrismPropertyImpl<>(itemName, definition);
    }

    @Override
    public <T> PrismPropertyValue<T> createPropertyValue() {
        return new PrismPropertyValueImpl<>(null);
    }

    @Override
    public <T> PrismPropertyValue<T> createPropertyValue(T realValue) {
        return new PrismPropertyValueImpl<>(realValue);
    }

    @Override
    public <T> PrismPropertyValue<T> createPropertyValue(XNode rawValue) {
        PrismPropertyValueImpl<T> rv = new PrismPropertyValueImpl<>(null);
        rv.setRawElement(rawValue);
        return rv;
    }

    @Override
    public <T> PrismPropertyValue<T> createPropertyValue(T value, OriginType originType, Objectable originObject) {
        return new PrismPropertyValueImpl<>(value, originType, originObject, null);
    }

    @Override
    public PrismReference createReference(QName name) {
        return new PrismReferenceImpl(name, null);
    }

    @Override
    public PrismReference createReference(QName name, PrismReferenceDefinition definition) {
        return new PrismReferenceImpl(name, definition);
    }

    @Override
    public PrismReferenceValue createReferenceValue() {
        return new PrismReferenceValueImpl(null);
    }

    @Override
    public PrismReferenceValue createReferenceValue(PrismObject<?> target) {
        PrismReferenceValueImpl rv = new PrismReferenceValueImpl(target.getOid());
        rv.setObject(target);
        if (target.getDefinition() != null) {
            rv.setTargetType(target.getDefinition().getTypeName());
        }
        return rv;
    }

    @Override
    public PrismReferenceValue createReferenceValue(String targetOid) {
        return new PrismReferenceValueImpl(targetOid);
    }

    @Override
    public PrismReferenceValue createReferenceValue(String oid, OriginType originType, Objectable originObject) {
        return new PrismReferenceValueImpl(oid, originType, originObject);
    }

    @Override
    public PrismReferenceValue createReferenceValue(String oid, QName targetType) {
        return new PrismReferenceValueImpl(oid, targetType);
    }

    @Override
    public PrismValue createValue(Object realValue) {
        if (realValue instanceof Containerable) {
            return ((Containerable) realValue).asPrismContainerValue();
        } else if (realValue instanceof Referencable) {
            return ((Referencable) realValue).asReferenceValue();
        } else {
            return createPropertyValue(realValue);
        }
    }

    @Override
    public <C extends Containerable> PrismContainer<C> createContainer(QName name) {
        return new PrismContainerImpl<>(name);
    }

    @Override
    public <C extends Containerable> PrismContainer<C> createContainer(QName name, PrismContainerDefinition<C> definition) {
        return new PrismContainerImpl<>(name, definition);
    }

    @Override
    public <O extends Objectable> PrismObject<O> createObject(QName name, PrismObjectDefinition<O> definition) {
        return new PrismObjectImpl<>(name, definition);
    }

    @Override
    public <O extends Objectable> PrismObjectValue<O> createObjectValue(O objectable) {
        return new PrismObjectValueImpl<>(objectable);
    }

    @Override
    public <C extends Containerable> PrismContainerValue<C> createContainerValue(C containerable) {
        return new PrismContainerValueImpl<>(containerable);
    }

    @Override
    public <C extends Containerable> PrismContainerValue<C> createContainerValue() {
        return new PrismContainerValueImpl<>();
    }

    @Override
    public <V extends PrismValue,D extends ItemDefinition<?>> Item<V,D> createDummyItem(Item<V,D> itemOld, D definition, ItemPath path) throws SchemaException {
        Item<V,D> itemMid;
        if (itemOld == null) {
            //noinspection unchecked
            itemMid = (Item<V, D>) definition.instantiate();
        } else {
            itemMid = itemOld.clone();
        }
        if (itemMid instanceof PrismProperty<?> property) {
            //noinspection unchecked
            return (Item<V,D>) new DummyPropertyImpl<>(property, path);
        } else if (itemMid instanceof PrismReference reference) {
            //noinspection unchecked
            return (Item<V,D>) new DummyReferenceImpl(reference, path);
        } else if (itemMid instanceof PrismContainer<?> container) {
            //noinspection unchecked
            return (Item<V,D>) new DummyContainerImpl<>(container, path);
        } else {
            throw new IllegalStateException("Unknown type "+itemMid.getClass());
        }
    }
}
