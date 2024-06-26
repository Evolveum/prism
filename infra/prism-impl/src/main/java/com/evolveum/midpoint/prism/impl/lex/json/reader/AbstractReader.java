/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.lex.json.reader;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.ParserSource;
import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismNamespaceContext;
import com.evolveum.midpoint.prism.impl.ParsingContextImpl;
import com.evolveum.midpoint.prism.impl.lex.LexicalProcessor;
import com.evolveum.midpoint.prism.impl.xnode.*;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Takes care of reading JSON/YAML to XNode.
 */
public abstract class AbstractReader {

    private static final Trace LOGGER = TraceManager.getTrace(AbstractReader.class);

    @NotNull protected final SchemaRegistry schemaRegistry;

    private final PrismNamespaceContext namespaceContext;

    public static final ObjectMapper OBJECT_MAPPER;

    static {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule sm = new SimpleModule();
        sm.addDeserializer(QName.class, new QNameDeserializer());
        sm.addDeserializer(PolyString.class, new PolyStringDeserializer());

        mapper.registerModule(sm);
        OBJECT_MAPPER = mapper;
    }

    AbstractReader(@NotNull SchemaRegistry schemaRegistry) {
        this.schemaRegistry = schemaRegistry;
        // Parsing legacy namespace-less JSON with default namespace declared
        // would put any undefined element to default namespace (usually common)
        // which will break in places, where namespace change is expected
        // and final item name is not directly defined in parent.
        this.namespaceContext = schemaRegistry.staticNamespaceContext().withoutDefault();
    }

    @NotNull
    public RootXNodeImpl read(@NotNull ParserSource source, @NotNull ParsingContext parsingContext,
            @Nullable ItemDefinition<?> definition) throws SchemaException, IOException {
        List<RootXNodeImpl> nodes = readInternal(source, parsingContext, definition, false);
        if (nodes.isEmpty()) {
            throw new SchemaException("No data at input");
        } else if (nodes.size() > 1) {
            throw new SchemaException("More than one object found: " + nodes); // should not occur
        } else {
            return nodes.get(0);
        }
    }

    /**
     * Honors multi-document files and multiple objects in a single document (list-as-root mechanisms).
     */
    @NotNull
    public List<RootXNodeImpl> readObjects(@NotNull ParserSource source, @NotNull ParsingContext parsingContext,
            @Nullable ItemDefinition<?> definition) throws SchemaException, IOException {
        return readInternal(source, parsingContext, definition, true);
    }

    @NotNull
    private List<RootXNodeImpl> readInternal(@NotNull ParserSource source, @NotNull ParsingContext parsingContext,
            ItemDefinition<?> definition, boolean expectingMultipleObjects) throws SchemaException, IOException {
        InputStream is = source.getInputStream();
        try {
            JsonParser parser = createJacksonParser(is);
            List<RootXNodeImpl> rv = new ArrayList<>();
            readFromStart(parser, parsingContext, definition, rv::add, expectingMultipleObjects);
            return rv;
        } finally {
            if (source.closeStreamAfterParsing()) {
                closeQuietly(is);
            }
        }
    }

    private void closeQuietly(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                LoggingUtils.logExceptionAsWarning(LOGGER, "Couldn't close the input stream", e);
            }
        }
    }

    public void readObjectsIteratively(@NotNull ParserSource source, @NotNull ParsingContext parsingContext, ItemDefinition<?> definition,
            LexicalProcessor.RootXNodeHandler handler) throws SchemaException, IOException {
        InputStream is = source.getInputStream();
        try {
            JsonParser parser = createJacksonParser(is);
            readFromStart(parser, parsingContext, definition, handler, true);
        } finally {
            if (source.closeStreamAfterParsing()) {
                closeQuietly(is);
            }
        }
    }

    protected abstract JsonParser createJacksonParser(InputStream stream) throws SchemaException, IOException;

    @FunctionalInterface
    interface YamlTagResolver {
        QName tagToTypeName(Object tid, JsonReadingContext ctx) throws IOException, SchemaException;
    }

    private void readFromStart(JsonParser unconfiguredParser, ParsingContext parsingContext, ItemDefinition<?> definition,
            LexicalProcessor.RootXNodeHandler handler, boolean expectingMultipleObjects) throws SchemaException, IOException {
        JsonParser configuredParser = configureParser(unconfiguredParser);
        JsonReadingContext ctx = new JsonReadingContext(configuredParser, (ParsingContextImpl) parsingContext,
                handler, this::tagToTypeName, schemaRegistry, definition);
        readTreatingExceptions(expectingMultipleObjects, configuredParser, ctx);
    }

    private void readTreatingExceptions(boolean expectingMultipleObjects, JsonParser configuredParser, JsonReadingContext ctx)
            throws SchemaException, IOException {
        try {
            readFirstTokenAndCheckEmptyInput(configuredParser);
            if (supportsMultipleDocuments()) {
                new MultiDocumentReader(ctx, globalNamespaceContext()).read(expectingMultipleObjects);
            } else {
                new DocumentReader(ctx, globalNamespaceContext()).read(expectingMultipleObjects);
            }
        } catch (SchemaException e) {
            throw e;
        } catch (JsonParseException e) {
            throw new SchemaException("Couldn't parse JSON/YAML object: " + e.getMessage() + ctx.getPositionSuffixIfPresent(), e);
        } catch (IOException e) {
            throw new IOException("Couldn't parse JSON/YAML object: " + e.getMessage() + ctx.getPositionSuffixIfPresent(), e);
        } catch (Throwable t) {
            throw new SystemException("Couldn't parse JSON/YAML object: " + t.getMessage() + ctx.getPositionSuffixIfPresent(), t);
        }
    }

    private PrismNamespaceContext globalNamespaceContext() {
        return namespaceContext;
    }

    abstract boolean supportsMultipleDocuments();

    private void readFirstTokenAndCheckEmptyInput(JsonParser configuredParser) throws IOException, SchemaException {
        configuredParser.nextToken();
        if (configuredParser.currentToken() == null) {
            throw new SchemaException("Nothing to parse: the input is empty.");
        }
    }

    private JsonParser configureParser(JsonParser parser) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule sm = new SimpleModule();
        sm.addDeserializer(QName.class, new QNameDeserializer());
        sm.addDeserializer(PolyString.class, new PolyStringDeserializer());

        mapper.registerModule(sm);
        parser.setCodec(mapper);
        return parser;
    }

    protected abstract QName tagToTypeName(Object tid, JsonReadingContext ctx) throws IOException, SchemaException;

    public abstract boolean canRead(@NotNull File file) throws IOException;

    public abstract boolean canRead(@NotNull String dataString);
}
