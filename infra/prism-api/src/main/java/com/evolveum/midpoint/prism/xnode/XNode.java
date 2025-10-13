/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.xnode;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.util.DebugDumpable;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 *
 */
public interface XNode extends DebugDumpable, Visitable<XNode>, Cloneable, Copyable<XNode>, Serializable, Freezable {

    boolean isEmpty();

    QName getTypeQName();

    RootXNode toRootXNode();

    boolean isExplicitTypeDeclaration();

    @NotNull
    XNode clone();

    @Override
    XNode copy();

    Integer getMaxOccurs();

    default boolean hasMetadata() {
        return this instanceof MetadataAware && !((MetadataAware) this).getMetadataNodes().isEmpty();
    }

    default PrismNamespaceContext namespaceContext() {
        return PrismNamespaceContext.EMPTY;
    }

    default XNode frozen() {
        freeze();
        return this;
    }

    void setDefinition(ItemDefinition<?> definition);
    ItemDefinition<?> getDefinition();
}
