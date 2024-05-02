/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.PrismContextImpl;
import com.evolveum.midpoint.prism.schema.SchemaBuilder;
import com.evolveum.midpoint.prism.schema.SchemaRegistryState;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.DomAnnotationParserFactory;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.List;

/**
 * Parser for DOM-represented XSD, creates midPoint Schema representation.
 *
 * It will parse schema in two steps:
 *
 * . DOM -> XSOM ({@link XSSchemaSet}) parsing is done by this class.
 * . XSOM -> Prism is delegated to {@link SchemaXsomParser}.
 *
 * @author lazyman
 * @author Radovan Semancik
 */
class SchemaDomParser {

    private static final Trace LOGGER = TraceManager.getTrace(SchemaDomParser.class);

    private static final Object SCHEMA_PARSING_SYNC_GUARD = new Object();

    private String shortDescription;

    /**
     * Parses single schema.
     */
    void parseSchema(
            @NotNull SchemaBuilder schemaBuilder,
            @NotNull Element xsdSchema,
            boolean isRuntime,
            boolean allowDelayedItemDefinitions,
            String shortDescription,
            SchemaRegistryState schemaRegistryState) throws SchemaException {
        this.shortDescription = shortDescription;
        schemaBuilder.setRuntime(isRuntime);
        XSSchemaSet xsSchemaSet = parseSchemaToXsom(xsdSchema, schemaRegistryState);
        if (xsSchemaSet != null) {
            new SchemaXsomParser(
                    xsSchemaSet,
                    List.of(schemaBuilder),
                    allowDelayedItemDefinitions,
                    shortDescription,
                    schemaRegistryState)
                    .parseSchema();
        }
    }

    /**
     * Parses several schemas, referenced by a wrapper schema.
     * Provided to allow circular references (e.g. common-3 -> scripting-3 -> common-3).
     */
    void parseSchemas(
            Collection<PrismSchemaImpl> schemas, Element wrapper, SchemaRegistryState schemaRegistryState) throws SchemaException {
        XSSchemaSet xsSchemaSet = parseSchemaToXsom(wrapper, schemaRegistryState);
        if (xsSchemaSet != null) {
            var builders = schemas.stream()
                    .map(sd -> sd.builder())
                    .toList();
            new SchemaXsomParser(
                    xsSchemaSet,
                    builders,
                    true,
                    "multiple sources",
                    schemaRegistryState)
                    .parseSchema();
        }
    }

    private XSSchemaSet parseSchemaToXsom(Element schema, SchemaRegistryState schemaRegistryState) throws SchemaException {
        // Synchronization here is a brutal workaround for MID-5648. We need to synchronize on parsing schemas globally, because
        // it looks like there are many fragments (referenced schemas) that get resolved during parsing.
        //
        // Unfortunately, this is not sufficient by itself -- there is a pre-processing that must be synchronized as well.
        synchronized (SCHEMA_PARSING_SYNC_GUARD) {
            // Make sure that the schema parser sees all the namespace declarations
            DOMUtil.fixNamespaceDeclarations(schema);
            try {
                TransformerFactory transfac = DOMUtil.setupTransformerFactory();
                Transformer trans = transfac.newTransformer();
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                trans.setOutputProperty(OutputKeys.INDENT, "yes");

                DOMSource source = new DOMSource(schema);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                StreamResult result = new StreamResult(out);

                trans.transform(source, result);

                XSOMParser parser = createSchemaParser(schemaRegistryState);
                InputSource inSource = new InputSource(new ByteArrayInputStream(out.toByteArray()));
                // XXX: hack: it's here to make entity resolver work...
                inSource.setSystemId("SystemId");
                // XXX: end hack
                inSource.setEncoding("utf-8");

                parser.parse(inSource);
                return parser.getResult();

            } catch (SAXException e) {
                throw new SchemaException(
                        "XML error during XSD schema parsing: %s (embedded exception %s) in %s".formatted(
                                e.getMessage(), e.getException(), shortDescription),
                        e);
            } catch (TransformerException e) {
                throw new SchemaException(
                        "XML transformer error during XSD schema parsing: %s (locator: %s, embedded exception:%s) in %s".formatted(
                                e.getMessage(), e.getLocator(), e.getException(), shortDescription),
                        e);
            } catch (RuntimeException e) {
                // This sometimes happens, e.g. NPEs in Saxon
                LOGGER.error("Unexpected error {} during parsing of schema:\n{}",
                        e.getMessage(), DOMUtil.serializeDOMToString(schema));
                throw new SchemaException(
                        "XML error during XSD schema parsing: " + e.getMessage() + " in " + shortDescription, e);
            }
        }
    }

    private XSOMParser createSchemaParser(SchemaRegistryState schemaRegistryState) {
        var saxParser = SAXParserFactory.newInstance();
        saxParser.setNamespaceAware(true);
        XSOMParser parser = new XSOMParser(saxParser);

        EntityResolver resolver;
        if (schemaRegistryState == null) {
            resolver = ((PrismContextImpl) PrismContext.get()).getEntityResolver();
        } else {
            resolver = new XmlEntityResolverImpl(
                    (SchemaRegistryImpl) PrismContext.get().getSchemaRegistry(),
                    (SchemaRegistryStateImpl) schemaRegistryState);
        }
        SchemaHandler schemaHandler = new SchemaHandler(resolver);
        parser.setErrorHandler(schemaHandler);
        parser.setAnnotationParser(new DomAnnotationParserFactory());
        parser.setEntityResolver(schemaHandler);

        return parser;
    }
}
