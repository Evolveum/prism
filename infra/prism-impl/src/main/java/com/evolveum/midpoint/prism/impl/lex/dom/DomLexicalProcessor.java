/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.lex.dom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.impl.lex.ValidatorUtil;
import com.evolveum.midpoint.prism.marshaller.XNodeProcessorEvaluationMode;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.ParserElementSource;
import com.evolveum.midpoint.prism.impl.lex.LexicalProcessor;
import com.evolveum.midpoint.prism.impl.lex.LexicalUtils;
import com.evolveum.midpoint.prism.impl.xnode.*;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.xnode.RootXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

public class DomLexicalProcessor implements LexicalProcessor<String> {

    public static final Trace LOGGER = TraceManager.getTrace(DomLexicalProcessor.class);

    @NotNull private final SchemaRegistry schemaRegistry;

    public DomLexicalProcessor(@NotNull SchemaRegistry schemaRegistry) {
        this.schemaRegistry = schemaRegistry;
    }

    @NotNull
    @Override
    public RootXNodeImpl read(@NotNull ParserSource source, @NotNull ParsingContext parsingContext, ItemDefinition<?> definition) throws SchemaException, IOException {
        if (source instanceof ParserElementSource) {
            Element root;

            if (parsingContext.isValidation()) {
                try {
                    root = new StreamDomBuilder().parse(source.getInputStream()).getDocumentElement();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                root = ((ParserElementSource) source).getElement();
            }

            return new DomReader(root, schemaRegistry, PrismNamespaceContext.EMPTY, parsingContext).read();
        } else {
            InputStream is = source.getInputStream();
            try {
                Document document;

                if (parsingContext.isValidation()) {
                    try {
                        document = new StreamDomBuilder().parse(source.getInputStream());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    document = DOMUtil.parse(is);
                }

                return new DomReader(document, schemaRegistry, parsingContext).read();
            } finally {
                if (source.closeStreamAfterParsing()) {
                    IOUtils.closeQuietly(is);
                }
            }
        }
    }

    @NotNull
    @Override
    public List<RootXNodeImpl> readObjects(@NotNull ParserSource source, @NotNull ParsingContext parsingContext) throws SchemaException, IOException {
        InputStream is = source.getInputStream();
        try {
            Document document;

            if (parsingContext.isValidation()) {
                try {
                    document = new StreamDomBuilder().parse(source.getInputStream());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                document = DOMUtil.parse(is);
            }

            return new DomReader(document, schemaRegistry, parsingContext).readObjects();
        } finally {
            if (source.closeStreamAfterParsing()) {
                IOUtils.closeQuietly(is);
            }
        }
    }

    @Override
    public void readObjectsIteratively(@NotNull ParserSource source,
            @NotNull ParsingContext parsingContext, RootXNodeHandler handler)
            throws SchemaException, IOException {
        new DomIterativeReader(source, handler, schemaRegistry)
                .readObjectsIteratively(parsingContext);
    }


    @Override
    public boolean canRead(@NotNull File file) {
        return file.getName().endsWith(".xml");
    }

    private static final Pattern XML_DETECTION_PATTERN = Pattern.compile("\\A\\s*<\\w+");

    @Override
    public boolean canRead(@NotNull String dataString) {
        return dataString.charAt(0) == '<'
                || XML_DETECTION_PATTERN.matcher(dataString).find();
    }

    @NotNull
    @Override
    public String write(@NotNull XNode xnode, @NotNull QName rootElementName, SerializationContext serializationContext) throws SchemaException {
        RootXNodeImpl xroot = LexicalUtils.createRootXNode((XNodeImpl) xnode, rootElementName);
        Element element =
                new DomWriter(schemaRegistry, serializationContext)
                        .writeRoot(xroot);
        return DOMUtil.serializeDOMToString(element);
    }

    @NotNull
    @Override
    public String write(@NotNull RootXNode xnode, SerializationContext serializationContext) throws SchemaException {
        Element element =
                new DomWriter(schemaRegistry, serializationContext)
                        .writeRoot(xnode);
        return DOMUtil.serializeDOMToString(element);
    }

    @NotNull
    @Override
    public String write(@NotNull List<RootXNodeImpl> roots, @Nullable SerializationContext context) throws SchemaException {
        Element element = writeXRootListToElement(roots);
        return DOMUtil.serializeDOMToString(element);
    }

    @NotNull
    public Element writeXRootListToElement(@NotNull List<RootXNodeImpl> roots) throws SchemaException {
        return new DomWriter(schemaRegistry, null)
                .writeRoots(roots);
    }

    /**
     * Seems to be used in strange circumstances (called from various hacks).
     * To be reconsidered eventually. Avoid using in new code.
     */
    @Deprecated
    public Element writeXMapToElement(MapXNodeImpl xmap, QName elementName) throws SchemaException {
        return new DomWriter(schemaRegistry, null)
                .writeMap(xmap, elementName);
    }

    @NotNull
    public Element writeXRootToElement(@NotNull RootXNodeImpl xroot) throws SchemaException {
        return new DomWriter(schemaRegistry, null)
                .writeRoot(xroot);
    }

    // TODO move somewhere
    static <T> T processIllegalArgumentException(String value, QName typeName, IllegalArgumentException e,
            XNodeProcessorEvaluationMode mode) {
        if (mode == XNodeProcessorEvaluationMode.COMPAT) {
            LOGGER.warn("Value of '{}' couldn't be parsed as '{}' -- interpreting as null because of COMPAT mode set", value,
                    typeName, e);
            return null;
        } else {
            throw e;
        }
    }
}
