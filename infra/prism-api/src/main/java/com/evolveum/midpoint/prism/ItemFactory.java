/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 *  Factory for items (property, reference, container, object) and item values.
 *
 *  Eliminates the need of calls like "new PrismPropertyValue(...)" in midPoint 3.x.
 */
public interface ItemFactory {

    PrismValue createValue(Object realValue);

    <T> PrismProperty<T> createProperty(QName itemName);

    <T> PrismProperty<T> createProperty(QName itemName, PrismPropertyDefinition<T> definition);

    <T> PrismPropertyValue<T> createPropertyValue();

    default <T> Collection<PrismPropertyValue<T>> createPropertyValues(Collection<T> content) {
        return content.stream()
                .map(this::createPropertyValue)
                .toList();
    }

    <T> PrismPropertyValue<T> createPropertyValue(T content);

    <T> PrismPropertyValue<T> createPropertyValue(XNode rawContent);

    <T> PrismPropertyValue<T> createPropertyValue(T value, OriginType originType, Objectable originObject);

    PrismReference createReference(QName name);

    PrismReference createReference(QName name, PrismReferenceDefinition definition);

    PrismReferenceValue createReferenceValue();

    PrismReferenceValue createReferenceValue(PrismObject<?> target);

    PrismReferenceValue createReferenceValue(String targetOid);

    PrismReferenceValue createReferenceValue(String oid, OriginType originType, Objectable originObject);

    PrismReferenceValue createReferenceValue(String oid, QName targetType);

    default Collection<PrismReferenceValue> createReferenceValues(Collection<Referencable> content) {
        return content.stream()
                .map(Referencable::asReferenceValue)
                .toList();
    }

    <C extends Containerable> PrismContainer<C> createContainer(QName name);

    <C extends Containerable> PrismContainer<C> createContainer(QName name, PrismContainerDefinition<C> definition);

    <O extends Objectable> PrismObject<O> createObject(QName name, PrismObjectDefinition<O> definition);

    <C extends Containerable> PrismContainerValue<C> createContainerValue();

    /**
     * Creates a dummy container with a fixed path.
     * This container is good for storing values, e.g. in case of delta computations to get
     * preview of the new item.
     * But such container cannot be used to fit into any prism structure (cannot set parent).
     */
    @Experimental
    <V extends PrismValue,D extends ItemDefinition<?>> Item<V,D> createDummyItem(Item<V,D> itemOld, D definition, ItemPath path)
            throws SchemaException;
}
