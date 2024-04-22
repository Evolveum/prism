/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.path;

import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.PrismConstants;

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



    public InfraItemName(String namespaceURI, String localPart) {
        super(namespaceURI, localPart);
    }

    public static InfraItemName of(String namespace, String localPart) {
        return new InfraItemName(namespace, localPart);
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
    public Long firstToIdOrNull() {
        return null;
    }

    @NotNull
    @Override
    public ItemPath namedSegmentsOnly() {
        return this;
    }

    @NotNull
    @Override
    public ItemPath removeIds() {
        return this;
    }

    @Override
    public ItemName asSingleName() {
        return null;
    }

    @Override
    public boolean isSingleName() {
        return true;
    }

    @Override
    public ItemName lastName() {
        return null;
    }

    @Override
    public Object last() {
        return this;
    }

    @Override
    public ItemPath firstAsPath() {
        return this;
    }

    @NotNull
    @Override
    public ItemPath allExceptLast() {
        return EMPTY_PATH;
    }

    @Override
    public String toString() {
        return "@" + this.getLocalPart();
    }

    @Override
    public void shortDump(StringBuilder sb) {
        sb.append(this);
    }

    @Override
    public ItemPath subPath(int from, int to) {
        if (from > 0) {
            return EMPTY_PATH;
        } else if (to == 0) {
            return EMPTY_PATH;
        } else {
            return this;
        }
    }

    public boolean matches(QName other) {
        throw new UnsupportedOperationException();
    }
}
