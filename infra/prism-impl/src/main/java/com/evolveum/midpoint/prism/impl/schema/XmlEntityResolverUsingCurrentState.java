/*
 * Copyright (c) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.schema.SchemaDescription;
import com.evolveum.midpoint.prism.schema.SchemaRegistryState;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.Collection;

/**
 * Resolver that use unfinished SchemaRegistryState for obtain SchemaDescription during of parsing schemas.
 */
public class XmlEntityResolverUsingCurrentState extends XmlEntityResolverImpl {

    private final SchemaRegistryStateImpl unfinishedState;

    public XmlEntityResolverUsingCurrentState(SchemaRegistryImpl schemaRegistry, SchemaRegistryStateImpl unfinishedState) {
        super(schemaRegistry);
        this.unfinishedState = unfinishedState;
    }

    protected Collection<SchemaDescription> getParsedSchemasForNamespace(String namespaceURI) {
        return unfinishedState.getParsedSchemas().get(namespaceURI);
    }
}
