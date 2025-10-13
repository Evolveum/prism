/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

import java.io.InputStream;
import javax.xml.transform.Source;

import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.Freezable;
import com.evolveum.midpoint.util.DebugDumpable;

/**
 * Schema (prism or non-prism) with additional information.
 */
public interface SchemaDescription extends DebugDumpable, Freezable {

    /**
     * @return Path to schema source data (e.g. XSD file) - if known.
     */
    String getPath();

    /**
     * @return Namespace for elements in this schema.
     */
    String getNamespace();

    /**
     * @return Prefix that is usually used for this schema/namespace (e.g. "c" for common-3).
     */
    String getUsualPrefix();

    /**
     * @return True if this prefix should be declared in XML files by default at the top of the file.
     */
    boolean isDeclaredByDefault();

    boolean isDefault();

    String getSourceDescription();

    boolean isPrismSchema();

    Package getCompileTimeClassesPackage();

    boolean canInputStream();

    InputStream openInputStream();

    Source getSource();

    Element getDomElement();
}
