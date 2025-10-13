/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.path;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.PrismConstants;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.QNameUtil;

/**
 *
 * Item Name of Infra Model Items.
 *
 * Infra Model Items are not part of data model, but rather meta-model or infra model which is behind data model.
 * They have namespace and local name, but they may be serialized differently according to language (using native features
 * of data serialization format and/or other models already available natively for particular language), or they may be computed
 * during runtime.
 *
 *
 * FIXME: Maybe this should not extend ItemName
 */
public class InfraItemName extends ItemName {

    private static final Interner<InfraItemName> INTERNER = Interners.newStrongInterner();


    public static final InfraItemName ID = InfraItemName.of(PrismConstants.NS_TYPES, "id").intern();

    public static final InfraItemName TYPE = InfraItemName.of(PrismConstants.NS_TYPES, "type").intern();

    public static final InfraItemName PATH = InfraItemName.of(PrismConstants.NS_TYPES, "path").intern();
    public static final InfraItemName METADATA = InfraItemName.of(PrismConstants.NS_METADATA,"metadata").intern();


    private static final BiMap<InfraItemName, QName> SERIALIZATION_MAPPING = ImmutableBiMap.<InfraItemName, QName>builder()
            .put(serializationEntry(ID))
            .put(serializationEntry(TYPE))
            .put(serializationEntry(PATH))
            .put(serializationEntry(METADATA))
            .build();

    private static final BiMap<QName, InfraItemName> DESERIALIZATION_MAPPING = SERIALIZATION_MAPPING.inverse();



    public InfraItemName(String namespaceURI, String localPart) {
        super(namespaceURI, localPart);
    }

    public static InfraItemName of(String namespace, String localPart) {
        return new InfraItemName(namespace, localPart);
    }

    public static InfraItemName fromQName(QName name) {
        if (name == null) {
            return null;
        } else if (name instanceof InfraItemName infra) {
            return infra;
        } else {
            return of(name.getNamespaceURI(), name.getLocalPart());
        }
    }

    public static boolean isSerializedForm(QName qname) {
        return DESERIALIZATION_MAPPING.containsKey(qname);
    }

    public static InfraItemName fromSerialized(QName qname) {
        return DESERIALIZATION_MAPPING.get(qname);
    }

    public InfraItemName intern() {
        return INTERNER.intern(this);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @NotNull
    @Override
    public List<?> getSegments() {
        return Collections.singletonList(this);
    }

    @Override
    public Object getSegment(int i) {
        if (i == 0) {
            return this;
        } else {
            throw new IndexOutOfBoundsException("Index: " + i + ", while accessing single-item path");
        }
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Object first() {
        return this;
    }

    @NotNull
    @Override
    public ItemPath rest() {
        return ItemPath.EMPTY_PATH;
    }

    @NotNull
    @Override
    public ItemPath rest(int n) {
        if (n == 0) {
            return this;
        } else {
            return EMPTY_PATH;
        }
    }

    @Override
    public String toString() {
        return "@" + this.getLocalPart();
    }


    public boolean matches(QName other) {
        throw new UnsupportedOperationException();
    }

    public QName asSerializationForm() {
        return SERIALIZATION_MAPPING.get(this);
    }

    private static Map.Entry<InfraItemName,QName> serializationEntry(InfraItemName infraItem) {
        return new AbstractMap.SimpleEntry<>(infraItem, new QName("@" + infraItem.getLocalPart()));
    }
}
