/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.schemaContext;

import com.evolveum.midpoint.prism.path.ItemPath;

import java.io.Serializable;
import javax.xml.namespace.QName;

/**
 * Provides the content of `schemaContext` annotation.
 *
 * Just simple getters and setters, with some rudimentary parsing.
 *
 * See https://docs.evolveum.com/midpoint/devel/schema-context-annotations/.
 */
public interface SchemaContextDefinition extends Serializable {

    /**
     * Fixed name of the target object type. Must be qualified.
     */
    QName getType();

    /**
     * Fixed path of the target item within [fixed] target object type. {@link #getType()} must be non-null.
     */
    ItemPath getPath();

    /**
     * Path to a child element that will contain the target object type.
     */
    ItemPath getTypePath();

    /**
     * Name of the algorithm for determining the target item type.
     *
     * For example {@code ResourceObjectContextResolver}.
     */
    QName getAlgorithm();

    void setType(QName type);

    void setPath(ItemPath path);

    void setTypePath(ItemPath typePath);

    void setAlgorithm(QName algorithmName);
}
