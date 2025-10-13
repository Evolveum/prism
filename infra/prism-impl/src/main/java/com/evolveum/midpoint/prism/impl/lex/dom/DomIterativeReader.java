/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.lex.dom;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;

import org.apache.commons.io.IOUtils;
import org.codehaus.staxmate.dom.DOMConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.ParserSource;
import com.evolveum.midpoint.prism.PrismNamespaceContext;
import com.evolveum.midpoint.prism.impl.lex.LexicalProcessor;
import com.evolveum.midpoint.prism.impl.xnode.RootXNodeImpl;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;

/**
 *
 */
class DomIterativeReader {

    private final ParserSource source;
    private final LexicalProcessor.RootXNodeHandler handler;
    private final SchemaRegistry schemaRegistry;

    DomIterativeReader(ParserSource source, LexicalProcessor.RootXNodeHandler handler, SchemaRegistry schemaRegistry) {
        this.source = source;
        this.handler = handler;
        this.schemaRegistry = schemaRegistry;
    }

    // code taken from Validator class
    public void readObjectsIteratively() throws SchemaException, IOException {
        InputStream is = source.getInputStream();
        XMLStreamReader stream = null;
        try {
            stream = getXMLInputFactory().createXMLStreamReader(is);

            int eventType = stream.nextTag();
            if (eventType != XMLStreamConstants.START_ELEMENT) {
                throw new SystemException("StAX Malfunction?");
            }
            DOMConverter domConverter = new DOMConverter(DOMUtil.createDocumentBuilder());
            Map<String, String> rootNamespaceDeclarations = new HashMap<>();

            QName objectsMarker = PrismContext.get().getObjectsElementName();
            if (objectsMarker != null && !QNameUtil.match(stream.getName(), objectsMarker)) {
                readSingleObjectIteratively(stream, rootNamespaceDeclarations, domConverter, handler);
            }
            for (int i = 0; i < stream.getNamespaceCount(); i++) {
                rootNamespaceDeclarations.put(stream.getNamespacePrefix(i), stream.getNamespaceURI(i));
            }
            while (stream.hasNext()) {
                eventType = stream.next();
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    if (!readSingleObjectIteratively(stream, rootNamespaceDeclarations, domConverter, handler)) {
                        return;
                    }
                }
            }
        } catch (XMLStreamException ex) {
            String lineInfo = stream != null
                    ? " on line " + stream.getLocation().getLineNumber()
                    : "";
            throw new SchemaException(
                    "Exception while parsing XML" + lineInfo + ": " + ex.getMessage(), ex);
        } finally {
            if (source.closeStreamAfterParsing()) {
                IOUtils.closeQuietly(is);
            }
        }

    }

    private boolean readSingleObjectIteratively(
            XMLStreamReader stream, Map<String, String> rootNamespaceDeclarations,
            DOMConverter domConverter, LexicalProcessor.RootXNodeHandler handler)
            throws XMLStreamException, SchemaException {
        Document objectDoc = domConverter.buildDocument(stream);
        Element objectElement = DOMUtil.getFirstChildElement(objectDoc);
        DOMUtil.setNamespaceDeclarations(objectElement, rootNamespaceDeclarations);
        RootXNodeImpl rootNode = new DomReader(objectElement, schemaRegistry, PrismNamespaceContext.EMPTY).read();
        return handler.handleData(rootNode);
    }

    private XMLInputFactory getXMLInputFactory() {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlInputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        // TODO: cache? static? prism context?
        return xmlInputFactory;
    }
}
