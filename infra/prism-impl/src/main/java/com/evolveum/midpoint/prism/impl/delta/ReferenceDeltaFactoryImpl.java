/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.delta;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.DeltaFactory;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ReferenceDelta;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Collection;

public class ReferenceDeltaFactoryImpl implements DeltaFactory.Reference {

    @NotNull private final PrismContext prismContext;

    ReferenceDeltaFactoryImpl(@NotNull PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    @Override
    public ReferenceDelta create(ItemPath path, PrismReferenceDefinition definition) {
        return new ReferenceDeltaImpl(path, definition);
    }

    @Override
    public ReferenceDelta create(PrismReferenceDefinition itemDefinition) {
        return new ReferenceDeltaImpl(itemDefinition);
    }

    @Override
    public ReferenceDelta create(ItemPath parentPath, QName name, PrismReferenceDefinition itemDefinition) {
        return new ReferenceDeltaImpl(parentPath, name, itemDefinition);
    }

    @Override
    public ReferenceDelta createModificationReplace(ItemPath path, PrismObjectDefinition<?> objectDefinition, String oid) {
        return ReferenceDeltaImpl.createModificationReplace(path, objectDefinition, oid);
    }

    @Override
    public <O extends Objectable> ReferenceDelta createModificationReplace(ItemPath path, Class<O> type,
            String oid) {
        return ReferenceDeltaImpl.createModificationReplace(path, type, prismContext, oid);
    }

    @Override
    public ReferenceDelta createModificationReplace(ItemPath path, PrismObjectDefinition<?> objectDefinition,
            PrismReferenceValue refValue) {
        return ReferenceDeltaImpl.createModificationReplace(path, objectDefinition, refValue);
    }

    @Override
    public ReferenceDelta createModificationReplace(ItemPath path, PrismObjectDefinition<?> objectDefinition,
            Collection<PrismReferenceValue> refValues) {
        return ReferenceDeltaImpl.createModificationReplace(path, objectDefinition, refValues);
    }

    @Override
    public Collection<? extends ItemDelta<?, ?>> createModificationAddCollection(ItemName propertyName,
            PrismObjectDefinition<?> objectDefinition, PrismReferenceValue refValue) {
        return ReferenceDeltaImpl.createModificationAddCollection(propertyName, objectDefinition, refValue);
    }

    @Override
    public ReferenceDelta createModificationAdd(ItemPath path, PrismObjectDefinition<?> objectDefinition,
            String oid) {
        return ReferenceDeltaImpl.createModificationAdd(path, objectDefinition, oid);
    }

    @Override
    public ReferenceDelta createModificationAdd(ItemPath path, PrismObjectDefinition<?> objectDefinition,
            PrismReferenceValue refValue) {
        return ReferenceDeltaImpl.createModificationAdd(path, objectDefinition, refValue);
    }

    @Override
    public ReferenceDelta createModificationAdd(ItemPath path, PrismObjectDefinition<?> objectDefinition,
            Collection<PrismReferenceValue> refValues) {
        return ReferenceDeltaImpl.createModificationAdd(path, objectDefinition, refValues);
    }

    @Override
    public <T extends Objectable> ReferenceDelta createModificationAdd(Class<T> type, ItemName refName,
            PrismReferenceValue refValue) {
        return ReferenceDeltaImpl.createModificationAdd(type, refName, refValue);
    }

    @Override
    public <T extends Objectable> Collection<? extends ItemDelta<?, ?>> createModificationAddCollection(
            Class<T> type, ItemName refName, String targetOid) {
        return ReferenceDeltaImpl.createModificationAddCollection(type, refName, targetOid);
    }

    @Override
    public <T extends Objectable> Collection<? extends ItemDelta<?, ?>> createModificationAddCollection(
            Class<T> type, ItemName refName, PrismReferenceValue refValue) {
        return ReferenceDeltaImpl.createModificationAddCollection(type, refName, refValue);
    }

    @Override
    public <T extends Objectable> ReferenceDelta createModificationAdd(
            Class<T> type, ItemName refName, PrismObject<?> refTarget) {
        return ReferenceDeltaImpl.createModificationAdd(type, refName, refTarget);
    }

    @Override
    public <T extends Objectable> Collection<? extends ItemDelta<?, ?>> createModificationAddCollection(
            Class<T> type, ItemName refName, PrismObject<?> refTarget) {
        return ReferenceDeltaImpl.createModificationAddCollection(type, refName, refTarget);
    }

    @Override
    public Collection<? extends ItemDelta<?, ?>> createModificationDeleteCollection(
            QName propertyName, PrismObjectDefinition<?> objectDefinition, PrismReferenceValue refValue) {
        return ReferenceDeltaImpl.createModificationDeleteCollection(propertyName, objectDefinition, refValue);
    }

    @Override
    public ReferenceDelta createModificationDelete(ItemPath path, PrismObjectDefinition<?> objectDefinition,
            Collection<PrismReferenceValue> refValues) {
        return ReferenceDeltaImpl.createModificationDelete(path, objectDefinition, refValues);
    }

    @Override
    public ReferenceDelta createModificationDelete(QName refName, PrismObjectDefinition<?> objectDefinition,
            String oid) {
        return ReferenceDeltaImpl.createModificationDelete(refName, objectDefinition, oid);
    }

    @Override
    public ReferenceDelta createModificationDelete(QName refName, PrismObjectDefinition<?> objectDefinition,
            PrismObject<?> refTarget) {
        return ReferenceDeltaImpl.createModificationDelete(refName, objectDefinition, refTarget);
    }

    @Override
    public ReferenceDelta createModificationDelete(QName refName, PrismObjectDefinition<?> objectDefinition,
            PrismReferenceValue refValue) {
        return ReferenceDeltaImpl.createModificationDelete(refName, objectDefinition, refValue);
    }

    @Override
    public <T extends Objectable> ReferenceDelta createModificationDelete(Class<T> type, QName refName,
            PrismReferenceValue refValue) {
        return ReferenceDeltaImpl.createModificationDelete(type, refName, refValue);
    }

    @Override
    public <T extends Objectable> Collection<? extends ItemDelta<?, ?>> createModificationDeleteCollection(Class<T> type, QName refName,
            PrismReferenceValue refValue) {
        return ReferenceDeltaImpl.createModificationDeleteCollection(type, refName, refValue);
    }

    @Override
    public <T extends Objectable> ReferenceDelta createModificationDelete(Class<T> type, QName refName,
            PrismObject<?> refTarget) {
        return ReferenceDeltaImpl.createModificationDelete(type, refName, refTarget);
    }

    @Override
    public <T extends Objectable> Collection<? extends ItemDelta<?, ?>> createModificationDeleteCollection(Class<T> type, QName refName,
            PrismObject<?> refTarget) {
        return ReferenceDeltaImpl.createModificationDeleteCollection(type, refName, refTarget);
    }
}
