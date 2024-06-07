/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.path;

import java.util.Collections;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import com.google.common.base.Strings;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.QNameUtil;

public class ItemName extends QName implements ItemPath {

    /**
     * Interner for Unprefixed Item Names (item names used in schema definitions, generated classes)
     *
     */
    private static final Interner<WithoutPrefix> WITHOUT_PREFIX = Interners.newStrongInterner();

    @Deprecated
    public ItemName(String namespaceURI, String localPart) {
        super(namespaceURI, localPart);
    }

    public ItemName(String namespaceURI, String localPart, String prefix) {
        super(namespaceURI, localPart, prefix);
    }

    public ItemName(String localPart) {
        super(localPart);
    }

    public ItemName(@NotNull QName name) {
        this(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix());
    }

    public static ItemName fromQName(QName name) {
        if (name == null) {
            return null;
        }
        if (name instanceof ItemName) {
            return (ItemName) name;
        }
        if (Strings.isNullOrEmpty(name.getPrefix())) {
            // FIXME: Should we use interned? would not this slow-down this construction?
            return new WithoutPrefix(name.getNamespaceURI(), name.getLocalPart());
        } else {
            return new ItemName(name);
        }
    }

    /**
     * Creates ItemName without prefix specified. Instance of Item Name is internalized
     * (deduplicated, ensured that only one such instance using this method exists in JVM)
     *
     * @param namespace
     * @param localPart
     * @return ItemName without prefix. Instance should be deduplicated inside JVM.
     */
    public static WithoutPrefix from(String namespace, String localPart) {
        return new WithoutPrefix(namespace, localPart);
    }

    public static WithoutPrefix interned(String namespace, String localPart) {
        return ItemName.from(namespace, localPart).intern();
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
        return this;
    }

    @Override
    public boolean isSingleName() {
        return true;
    }

    @Override
    public ItemName lastName() {
        return this;
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
        if (ItemPath.isObjectReference(this)) {
            return ObjectReferencePathSegment.SYMBOL;
        } else if (ItemPath.isIdentifier(this)) {
            return IdentifierPathSegment.SYMBOL;
        } else if (ItemPath.isParent(this)) {
            return ParentPathSegment.SYMBOL;
        } else {
            return DebugUtil.formatElementName(this);
        }
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
        return QNameUtil.match(this, other);
    }

    /**
     * ItemName without prefix specified, ideal item name for runtime data, constants, etc.
     * which could be internalized (one deduplicated instance for each combination of namespace, localPart)
     * per JVM - which simplifies equals.
     */
    public static class WithoutPrefix extends ItemName {
        protected WithoutPrefix(String namespaceUri, String localPart) {
            super(namespaceUri, localPart);
        }

        public WithoutPrefix intern() {
            return WITHOUT_PREFIX.intern(this);
        }

        @Override
        public WithoutPrefix withoutNamespace() {
            if (getNamespaceURI() == null) {
                return this;
            }
            return super.withoutNamespace();
        }
    }

    public WithoutPrefix withoutNamespace() {
        return new WithoutPrefix(null, getLocalPart());
    }

}
