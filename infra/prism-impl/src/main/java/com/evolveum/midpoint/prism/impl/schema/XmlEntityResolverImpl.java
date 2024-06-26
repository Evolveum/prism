/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.impl.XmlEntityResolver;
import com.evolveum.midpoint.prism.schema.SchemaDescription;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.Collection;

/**
 * @author semancik
 *
 * TODO refactor this a bit more
 */
public class XmlEntityResolverImpl implements XmlEntityResolver {

    private static final Trace LOGGER = TraceManager.getTrace(XmlEntityResolverImpl.class);

    private final SchemaRegistryImpl schemaRegistry;
    private final SchemaRegistryStateImpl schemaRegistryState;

    public XmlEntityResolverImpl(SchemaRegistryImpl schemaRegistry, SchemaRegistryStateImpl schemaRegistryState) {
        this.schemaRegistry = schemaRegistry;
        this.schemaRegistryState = schemaRegistryState;
    }

     /*
      *  Although not sure when resolveEntity and resolveResource is called, general schema is the following:
      *  1. For schemas that are imported via xsd:import, we use schemaLocation = namespaceURI, with some exceptions
       *      (we of course don't modify xsd:imports in standard schemas + for some historic reasons there is a difference
      *      for 'enc' and 'dsig' schemas: namespaceURI ends with '#', whereas schemaLocation does not)
      * 2. For schemas that are included via xsd:include (currently: fragments of common-3 schema), we use
      *    namespaceURI of the owning schema (e.g. .../common-3), whereas schemaLocation is URI derived from the
      *    namespace by including the fragment name (e.g. .../common-notifications-3).
      *
      * XSD parsers (the ones used by xjc and runtime parsing) seem to do the following:
      * 1. When encountering xsd:import, they look by publicId = namespaceURI, systemId = schemaLocation OR
      *    sometimes with publicId = null, systemId = schemaLocation (why?)
      * 2. When encountering xsd:include, they look by publicId = null, systemId = schemaLocation
      * 3. When encountering XML entity declaration that specifies publicId and systemId, look by them.
      *
      * See the respective methods.
      *
     */

    /* (non-Javadoc)
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        LOGGER.trace("--- Resolving entity with publicID: {}, systemID: {}", publicId, systemId);
        InputSource inputSource = resolveResourceFromRegisteredSchemas(publicId, systemId);
        if (inputSource == null) {
            inputSource = resolveResourceUsingBuiltinResolver(null, null, publicId, systemId, null);
        }
        if (inputSource == null) {
            LOGGER.error("Unable to resolve entity with publicID: {}, systemID: {}", publicId, systemId);
            return null;
        }
        LOGGER.trace("==> Resolved entity with publicID: {}, systemID: {} : {}", publicId, systemId, inputSource);
        return inputSource;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.ls.LSResourceResolver#resolveResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
            String baseURI) {
        LOGGER.trace("--- Resolving resource of type {}, namespaceURI: {}, publicID: {}, systemID: {}, base URI: {}", type, namespaceURI, publicId, systemId, baseURI);
        InputSource inputSource = resolveResourceFromRegisteredSchemas(publicId, systemId);
        if (inputSource == null) {
            inputSource = resolveResourceUsingBuiltinResolver(type, namespaceURI, publicId, systemId, baseURI);
        }
        if (inputSource == null) {
            LOGGER.error("Unable to resolve resource of type {}, namespaceURI: {}, publicID: {}, systemID: {}, baseURI: {}", type, namespaceURI, publicId, systemId, baseURI);
            return null;
        }
        LOGGER.trace("==> Resolved resource of type {}, namespaceURI: {}, publicID: {}, systemID: {}, baseURI: {} : {}", type, namespaceURI, publicId, systemId, baseURI, inputSource);
        return new Input(publicId, systemId, inputSource.getByteStream());
    }

    // schema fragments (e.g. common-model-context-3) will be obviously not found by this method
    private InputSource resolveResourceFromRegisteredSchemas(String publicId, String systemId) {
        InputSource source = resolveResourceFromRegisteredSchemasByNamespace(publicId);
        if (source == null) {
            // give a chance to systemId - in cases of xsd:import namespaceURI=<ns>, schemaLocation=<ns> that
            // (for some weird reason) result in search with publicId=null, systemId=<ns>
            source = resolveResourceFromRegisteredSchemasByNamespace(systemId);
        }
        LOGGER.trace("...... Result of registered schema resolve for publicId: {}, systemId: {}: {}", publicId, systemId, source);
        return source;
    }

    private InputSource resolveResourceFromRegisteredSchemasByNamespace(String namespaceURI) {
        if (namespaceURI != null) {
            Collection<SchemaDescription> schemaDescriptions = schemaRegistryState.getParsedSchemas().get(namespaceURI);
            if (schemaDescriptions.size() == 1) {
                SchemaDescription schemaDescription = schemaDescriptions.iterator().next();
                InputStream inputStream;
                if (schemaDescription.canInputStream()) {
                    inputStream = schemaDescription.openInputStream();
                } else {
                    DOMUtil.fixNamespaceDeclarations(schemaDescription.getDomElement());
                    String xml = DOMUtil.serializeDOMToString(schemaDescription.getDomElement());
                    inputStream = new ByteArrayInputStream(xml.getBytes());
                }
                InputSource source = new InputSource();
                source.setByteStream(inputStream);
                //source.setSystemId(schemaDescription.getPath());
                // Make sure that both publicId and systemId are always set to schema namespace
                // this helps to avoid double processing of the schemas
                source.setSystemId(namespaceURI);
                source.setPublicId(namespaceURI);
                return source;
            } else {
                return null;            // none or ambiguous namespace
            }
        }
        return null;
    }

    public InputSource resolveResourceUsingBuiltinResolver(String type, String namespaceURI, String publicId, String systemId,
            String baseURI) {
        InputSource inputSource;
        try {
            // we first try to use traditional pair of publicId + systemId
            // the use of namespaceUri can be misleading in case of schema fragments:
            // e.g. when xsd:including common-model-context-3 the publicId=null, systemId=.../common-model-context-3 but nsUri=.../common-3
            inputSource = schemaRegistry.getBuiltinSchemaResolver().resolveEntity(publicId, systemId);
            LOGGER.trace("...... Result of using builtin resolver by publicId + systemId: {}", inputSource);
            // in some weird cases (e.g. when publicId=null, systemId=xml.xsd) we go with namespaceUri (e.g. http://www.w3.org/XML/1998/namespace)
            // it's a kind of unfortunate magic here
            if (inputSource == null && namespaceURI != null) {
                inputSource = schemaRegistry.getBuiltinSchemaResolver().resolveEntity(namespaceURI, systemId);
                LOGGER.trace("...... Result of using builtin resolver by namespaceURI + systemId: {}", inputSource);
            }
        } catch (SAXException e) {
            LOGGER.error("XML parser error resolving reference of type {}, namespaceURI: {}, publicID: {}, systemID: {}, baseURI: {}: {}", type, namespaceURI, publicId, systemId, baseURI, e.getMessage(), e);
            // TODO: better error handling
            return null;
        } catch (IOException e) {
            LOGGER.error("IO error resolving reference of type {}, namespaceURI: {}, publicID: {}, systemID: {}, baseURI: {}: {}", type, namespaceURI, publicId, systemId, baseURI, e.getMessage(), e);
            // TODO: better error handling
            return null;
        }
        return inputSource;
    }

    static class Input implements LSInput {

        private String publicId;
        private String systemId;
        private final BufferedInputStream inputStream;

        Input(String publicId, String sysId, InputStream input) {
            this.publicId = publicId;
            this.systemId = sysId;
            this.inputStream = new BufferedInputStream(input);
        }

        public String getPublicId() {
            return publicId;
        }

        public void setPublicId(String publicId) {
            this.publicId = publicId;
        }

        public String getBaseURI() {
            return null;
        }

        public InputStream getByteStream() {
            return null;
        }

        public boolean getCertifiedText() {
            return false;
        }

        public Reader getCharacterStream() {
            return null;
        }

        public String getEncoding() {
            return null;
        }

        public String getStringData() {
            synchronized (inputStream) {
                try {
                    byte[] input = new byte[inputStream.available()];
                    //noinspection ResultOfMethodCallIgnored
                    inputStream.read(input);
                    return new String(input);
                } catch (IOException e) {
                    LOGGER.error("IO error creating LSInput for publicID: {}, systemID: {}: {}", publicId, systemId, e.getMessage(), e);
                    // TODO: better error handling
                    return null;
                }
            }
        }

        public void setBaseURI(String baseURI) {
        }

        public void setByteStream(InputStream byteStream) {
        }

        public void setCertifiedText(boolean certifiedText) {
        }

        public void setCharacterStream(Reader characterStream) {
        }

        public void setEncoding(String encoding) {
        }

        public void setStringData(String stringData) {
        }

        public String getSystemId() {
            return systemId;
        }

        public void setSystemId(String systemId) {
            this.systemId = systemId;
        }

        public BufferedInputStream getInputStream() {
            return inputStream;
        }

    }

    //endregion


}
