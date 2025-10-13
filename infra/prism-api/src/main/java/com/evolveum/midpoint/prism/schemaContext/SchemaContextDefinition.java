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
 * The interface provides Getter and Setter methods of possible attributes for definition the schema context annotation in xsd.
 *
 */
public interface SchemaContextDefinition extends Serializable {

    /**
     * Type object if directly defined.
     */
    QName getType();

    ItemPath getPath();

    /**
     * Path of type object.
     */
    QName getTypePath();

    /**
     * Name of resolver for find attributes which the object depends.
     *
     * For example {@link ResourceObjectContextResolver}
     */
    QName getAlgorithm();

    void setType(QName type);

    void setTypePath(QName typePath);

    void setPath(ItemPath path);

    void setAlgorithm(QName algorithmName);
}
