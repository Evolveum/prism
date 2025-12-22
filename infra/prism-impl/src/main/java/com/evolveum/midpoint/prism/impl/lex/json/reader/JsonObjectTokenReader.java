/*
 * Copyright (C) 2020-2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.lex.json.reader;

import static com.evolveum.midpoint.prism.impl.lex.json.JsonInfraItems.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.namespace.QName;

import com.evolveum.concepts.Argument;
import com.evolveum.concepts.SourceLocation;
import com.evolveum.concepts.TechnicalMessage;
import com.evolveum.concepts.ValidationLogType;
import com.evolveum.midpoint.prism.impl.lex.ValidatorUtil;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismNamespaceContext;
import com.evolveum.midpoint.prism.impl.lex.json.JsonInfraItems;
import com.evolveum.midpoint.prism.impl.xnode.*;
import com.evolveum.midpoint.prism.marshaller.XNodeProcessorEvaluationMode;
import com.evolveum.midpoint.prism.xnode.MapXNode;
import com.evolveum.midpoint.prism.xnode.MetadataAware;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * Reads JSON/YAML objects. This is the most complex part of the reading process.
 */
class JsonObjectTokenReader {

    private static final Trace LOGGER = TraceManager.getTrace(RootObjectReader.class);

    @NotNull private final JsonParser parser;
    @NotNull private final JsonReadingContext ctx;

    /**
     * Map corresponding to the current object.
     * It might or might not be used as a return value - depending on circumstances.
     */
    @NotNull private MapXNodeImpl map;

    private final PrismNamespaceContext parentContext;

    /**
     * Name of the type of this XNode.
     * Derived from YAML tag or from @type declaration.
     * Should be set only once.
     */
    private QName typeName;

    /**
     * Name of the element for this XNode. (From @element declaration.)
     * Should be set only once.
     */
    private QName elementName;

    /**
     * Wrapped value (@value declaration).
     * Should be set only once.
     */
    private XNodeImpl wrappedValue;

    /**
     * Metadata (@metadata).
     */
    private final List<MapXNode> metadata = new ArrayList<>();

    /**
     * Value of the "incomplete" flag (@incomplete).
     * Should be set only once.
     */
    private Boolean incomplete;

    private boolean namespaceSensitiveStarted = false;

    private @NotNull XNodeDefinition definition;

    private final XNodeDefinition parentDefinition;

    private XNodeImpl containerId;

    private static final Map<QName, ItemProcessor> PROCESSORS = ImmutableMap.<QName, ItemProcessor>builder()
            // Namespace definition processing
            .put(PROP_NAMESPACE_QNAME, JsonObjectTokenReader::processNamespaceDeclaration)
            .put(PROP_CONTEXT_QNAME, JsonObjectTokenReader::processContextDeclaration)

            .put(PROP_INCOMPLETE_QNAME, JsonObjectTokenReader::processIncompleteDeclaration)

            .put(PROP_TYPE_QNAME, JsonObjectTokenReader::processTypeDeclaration)
            .put(PROP_VALUE_QNAME, namespaceSensitive(JsonObjectTokenReader::processWrappedValue))

            .put(PROP_METADATA_QNAME, namespaceSensitive(JsonObjectTokenReader::processMetadataValue))
            .put(PROP_ELEMENT_QNAME, namespaceSensitive(JsonObjectTokenReader::processElementNameDeclaration))
            .put(PROP_ITEM_QNAME, namespaceSensitive(JsonObjectTokenReader::processElementNameDeclaration))

            .put(PROP_ID_QNAME, JsonObjectTokenReader::processId)
            .build();

    private static final ItemProcessor STANDARD_PROCESSOR = namespaceSensitive(JsonObjectTokenReader::processStandardFieldValue);

    JsonObjectTokenReader(@NotNull JsonReadingContext ctx, PrismNamespaceContext parentContext, @NotNull XNodeDefinition definition, @NotNull XNodeDefinition parentDefinition) {
        this.ctx = ctx;
        this.parser = ctx.parser;
        this.parentContext = parentContext;
        this.definition = definition;
        this.parentDefinition = parentDefinition;
    }

    /**
     * Normally returns a MapXNode. However, there are exceptions:
     * - JSON primitives/lists can be simulated by two-member object (@type + @value); in these cases we return respective XNode.
     * - Incomplete marker i.e. { "@incomplete" : "true" } should be interpreted as IncompleteMarkerXNode.
     */
    @NotNull XNodeImpl read() throws IOException, SchemaException {
        processYamlTag();
        processFields();
        return postProcess();
    }

    private void processYamlTag() throws IOException, SchemaException {
        Object typeId = parser.getTypeId();
        if (typeId != null) {
            typeName = ctx.yamlTagResolver.tagToTypeName(typeId, ctx);
            definition = definition.withType(typeName);
        }
    }

    private void processFields() throws IOException, SchemaException {
        XNodeDefinition currentField = null;
        while (!ctx.isAborted()) {
            JsonToken token = parser.nextToken();
            if (token == null) {
                String msg = "Unexpected end of data while parsing a map structure";
                ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                        null, new TechnicalMessage(msg),  msg);
                warnOrThrow(msg);
                ctx.setAborted();
                break;
            } else if (token == JsonToken.END_OBJECT) {
                break;
            } else if (token == JsonToken.FIELD_NAME) {
                currentField = processFieldName(currentField);
            } else {
                processFieldValue(currentField);
                currentField = null;
            }
        }
    }

    private @NotNull XNodeDefinition processFieldName(XNodeDefinition currentFieldName) throws IOException, SchemaException {
        String newFieldName = parser.currentName();
        if (currentFieldName != null) {
            String msg = "Two field names in succession: '%s' and '%s'";

            ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                    null, new TechnicalMessage(msg),  msg, ValidatorUtil.objectToString(currentFieldName), newFieldName);
            warnOrThrow(msg.formatted(currentFieldName.getName(), newFieldName));
        }

        // TODO set location for xNodeDefinition ???

        return definition.resolve(newFieldName, namespaceContext());
    }

    private @NotNull QName resolveQName(String name) throws SchemaException {
        return definition.unaware().resolve(name, namespaceContext()).getName();
    }

    private void processFieldValue(XNodeDefinition name) throws IOException, SchemaException {
        assert name != null;
        XNodeImpl value = readValue(name);

        value.setDefinition(name.itemDefinition());
        PROCESSORS.getOrDefault(name.getName(), STANDARD_PROCESSOR).apply(this, name.getName(), value);
    }

    private XNodeImpl readValue(XNodeDefinition fieldDef) throws IOException, SchemaException {
        return new JsonOtherTokenReader(ctx, namespaceContext().inherited(), fieldDef, definition).readValue();
    }

    private PrismNamespaceContext namespaceContext() {
        if (map != null) {
            return map.namespaceContext();
        }
        return parentContext.inherited();
    }

    private void processContextDeclaration(QName name, XNodeImpl value) throws SchemaException {
        if (value instanceof MapXNode) {
            Builder<String, String> nsCtx = ImmutableMap.<String, String>builder();
            for (Entry<QName, ? extends XNode> entry : ((MapXNode) value).toMap().entrySet()) {
                String key = entry.getKey().getLocalPart();
                String ns = getCurrentFieldStringValue(entry.getKey(), (XNodeImpl) entry.getValue());
                nsCtx.put(key, ns);
            }
            this.map = new MapXNodeImpl(parentContext.childContext(nsCtx.build()));
            return;
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    private void processStandardFieldValue(QName name, @NotNull XNodeImpl currentFieldValue) {
        // MID-5326:
        //   If namespace is defined, fieldName is always qualified,
        //   If namespace is undefined, then we can not effectivelly distinguish between
        map.put(name, currentFieldValue);
    }

    private void processIncompleteDeclaration(QName name, XNodeImpl currentFieldValue) throws SchemaException {
        if (incomplete != null) {
            String msg = "Duplicate '@incomplete' marker found with the value: '%s'";
            ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                    currentFieldValue.getSourceLocation(), new TechnicalMessage(msg, new Argument(currentFieldValue, Argument.ArgumentType.XNODE)),
                    "Duplicate '@incomplete' marker found");
            warnOrThrow(String.format(msg, currentFieldValue));
        } else if (currentFieldValue instanceof PrimitiveXNodeImpl) {
            //noinspection unchecked
            Boolean realValue = ((PrimitiveXNodeImpl<Boolean>) currentFieldValue)
                    .getParsedValue(DOMUtil.XSD_BOOLEAN, Boolean.class, getEvaluationMode());
            incomplete = Boolean.TRUE.equals(realValue);
        } else {
            String msg = "'@incomplete' marker found with incompatible value: '%s'";
            ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                    currentFieldValue.getSourceLocation(), new TechnicalMessage(msg, new Argument(currentFieldValue, Argument.ArgumentType.XNODE)),
                    "'@incomplete' marker found");
            warnOrThrow(String.format(msg, currentFieldValue));
        }
    }

    private void processWrappedValue(QName name, XNodeImpl currentFieldValue) throws SchemaException {
        if (wrappedValue != null) {
            String msg = "Value (' '%s' ') defined more than once";
            ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                    currentFieldValue.getSourceLocation(), new TechnicalMessage(msg),  msg, JsonInfraItems.PROP_VALUE);
            warnOrThrow(String.format(msg, JsonInfraItems.PROP_VALUE));

        }
        wrappedValue = currentFieldValue;
    }

    private void processMetadataValue(QName name, XNodeImpl currentFieldValue) throws SchemaException {
        if (currentFieldValue instanceof MapXNode) {
            metadata.add((MapXNode) currentFieldValue);
        } else if (currentFieldValue instanceof ListXNodeImpl) {
            for (XNodeImpl metadataValue : (ListXNodeImpl) currentFieldValue) {
                if (metadataValue instanceof MapXNode) {
                    metadata.add((MapXNode) metadataValue);
                } else {
                    String msg = "Metadata is not a map 'XNode': '%s'";
                    ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                            metadataValue.getSourceLocation(), new TechnicalMessage(msg, new Argument(metadataValue.debugDump(), Argument.ArgumentType.RAW)),
                            msg, ValidatorUtil.objectToString(metadataValue));
                    warnOrThrow(String.format(msg, metadataValue.debugDump()));
                }
            }
        } else {
            String msg = "Metadata is not a map or list 'XNode': '%s'";
            ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                    currentFieldValue.getSourceLocation(), new TechnicalMessage(msg, new Argument(currentFieldValue.debugDump(), Argument.ArgumentType.RAW)),
                    msg, ValidatorUtil.objectToString(currentFieldValue));
            warnOrThrow(String.format(msg, currentFieldValue.debugDump()));
        }
    }

    private void processElementNameDeclaration(QName name, XNodeImpl value) throws SchemaException {
        if (elementName != null) {
            String msg = "Element name defined more than once";
            ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                    value.getSourceLocation(), new TechnicalMessage(msg),  msg);
            warnOrThrow(msg);
        }
        String nsName = getCurrentFieldStringValue(name, value);
        @NotNull
        XNodeDefinition maybeDefinition = parentDefinition.resolve(nsName, namespaceContext());
        elementName = resolveQName(nsName);
        replaceDefinition(maybeDefinition);
    }

    private void processId(QName name, XNodeImpl value) {
        if (value instanceof PrimitiveXNodeImpl<?>) {
            ((PrimitiveXNodeImpl) value).setAttribute(true);
        }
        containerId = value;
    }

    private void replaceDefinition(@NotNull XNodeDefinition maybeDefinition) {
        definition = definition.moreSpecific(maybeDefinition);
    }

    private void processTypeDeclaration(QName name, XNodeImpl value) throws SchemaException {
        if (typeName != null) {
            String msg = "Value type defined more than once";
            ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                    value.getSourceLocation(), new TechnicalMessage(msg),  msg);
            warnOrThrow(msg);
        }
        String stringValue = getCurrentFieldStringValue(name, value);
        // TODO: Compat: We treat default prefixes as empty namespace, not default namespace
        typeName = XNodeDefinition.resolveQName(stringValue, namespaceContext());
        definition = definition.withType(typeName);
    }

    private void processNamespaceDeclaration(QName name, XNodeImpl value) throws SchemaException {
        if (namespaceSensitiveStarted) {
            String msg = "Namespace declared after other fields: '%s'";
            ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                    value.getSourceLocation(), new TechnicalMessage(msg, new Argument(ctx.getPositionSuffix(), Argument.ArgumentType.RAW)),
                    "Namespace declared after other fields");
            warnOrThrow(String.format(msg, ctx.getPositionSuffix()));
        }
        if (map != null) {
            String msg = "Namespace defined more than once";
            ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                    value.getSourceLocation(), new TechnicalMessage(msg), msg);
            warnOrThrow(msg);
        }
        var ns = getCurrentFieldStringValue(name, value);
        map = new MapXNodeImpl(parentContext.childContext(ImmutableMap.of("", ns)));
    }

    @NotNull
    private XNodeImpl postProcess() throws SchemaException {
        startNamespaceSensitive();
        // Return either map or primitive value (in case of @type/@value) or incomplete xnode
        int haveRegular = !map.isEmpty() ? 1 : 0;
        int haveWrapped = wrappedValue != null ? 1 : 0;
        int haveIncomplete = Boolean.TRUE.equals(incomplete) ? 1 : 0;

        XNodeImpl ret;

        if (haveRegular + haveWrapped + haveIncomplete > 1) {
            String msg = "More than one of '%s', '%s' and regular content present";
            ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                    map.getSourceLocation(), new TechnicalMessage(msg,
                            new Argument(PROP_VALUE, Argument.ArgumentType.STRING),
                            new Argument(PROP_INCOMPLETE, Argument.ArgumentType.STRING)),
                    msg, PROP_VALUE, PROP_INCOMPLETE);
            warnOrThrow(String.format(msg, PROP_VALUE, PROP_INCOMPLETE));
            ret = map;
        } else {
            if (haveIncomplete > 0) {
                ret = new IncompleteMarkerXNodeImpl();
            } else if (haveWrapped > 0) {
                ret = wrappedValue;
            } else {
                ret = map; // map can be empty here
            }
        }
        addIdTo(ret);
        addTypeNameTo(ret);
        addElementNameTo(ret);
        addMetadataTo(ret);
        if (ret instanceof PrimitiveXNodeImpl<?>) {
            ((PrimitiveXNodeImpl) ret).setAttribute(definition.isXmlAttribute());
        }

        return ret;
    }

    private void addIdTo(XNodeImpl ret) {
        if (containerId != null && ret instanceof MapXNodeImpl) {
            ((MapXNodeImpl) ret).put(XNodeImpl.KEY_CONTAINER_ID, containerId);
        }
    }

    private void addMetadataTo(XNodeImpl rv) throws SchemaException {
        if (!metadata.isEmpty()) {
            if (rv instanceof MetadataAware) {
                ((MetadataAware) rv).setMetadataNodes(metadata);
            } else {
                String msg = "Couldn't apply metadata to non-metadata-aware node: '%s'";
                ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                        rv.getSourceLocation(), new TechnicalMessage(msg, new Argument(rv, Argument.ArgumentType.XNODE)),
                        "Couldn't apply metadata to non-metadata-aware node");
                warnOrThrow(String.format(msg, rv.getClass()));
            }
        }
    }

    private void addElementNameTo(XNodeImpl rv) throws SchemaException {
        if (elementName != null) {
            if (wrappedValue != null && wrappedValue.getElementName() != null) {
                if (!wrappedValue.getElementName().equals(elementName)) {
                    String msg = "Conflicting element names for '%s' ('%s') and regular content ('%s'; ) present";
                    ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                            rv.getSourceLocation(), new TechnicalMessage(msg,
                                    new Argument(JsonInfraItems.PROP_VALUE, Argument.ArgumentType.STRING),
                                    new Argument(wrappedValue.getElementName(), Argument.ArgumentType.STRING),
                                    new Argument(elementName, Argument.ArgumentType.STRING)),
                            msg, JsonInfraItems.PROP_VALUE, "", elementName);
                    warnOrThrow(String.format(msg, JsonInfraItems.PROP_VALUE, wrappedValue.getElementName(), elementName));
                }
            }
            rv.setElementName(elementName);
        }
    }

    private void addTypeNameTo(XNodeImpl rv) throws SchemaException {
        if (typeName != null) {
            if (wrappedValue != null && wrappedValue.getTypeQName() != null && !wrappedValue.getTypeQName().equals(typeName)) {
                String msg = "Conflicting type names for '%s' ('%s') and regular content ('%s') present";
                ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                        rv.getSourceLocation(), new TechnicalMessage(msg,
                                new Argument(JsonInfraItems.PROP_VALUE, Argument.ArgumentType.STRING),
                                new Argument(wrappedValue.getTypeQName().getLocalPart(), Argument.ArgumentType.STRING),
                                new Argument(typeName, Argument.ArgumentType.QNAME)),
                        msg, JsonInfraItems.PROP_VALUE, "", typeName);
                warnOrThrow(String.format(msg, JsonInfraItems.PROP_VALUE, wrappedValue.getTypeQName(), typeName));
            }
            rv.setTypeQName(typeName);
            rv.setExplicitTypeDeclaration(true);
        }
    }

    private String getCurrentFieldStringValue(QName name, XNodeImpl currentFieldValue) throws SchemaException {
        if (currentFieldValue instanceof PrimitiveXNodeImpl) {
            return ((PrimitiveXNodeImpl<?>) currentFieldValue).getStringValue();
        } else {
            String msg = "Value of '%s' attribute must be a primitive one. It is '%s' instead";
            ctx.prismParsingContext.validationLogger(false, ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW,
                    currentFieldValue.getSourceLocation(), new TechnicalMessage(msg,
                            new Argument(name, Argument.ArgumentType.QNAME),
                            new Argument(currentFieldValue, Argument.ArgumentType.XNODE)),
                    msg, name, "");
            warnOrThrow(String.format(msg, name, currentFieldValue));
            return "";
        }
    }

    private XNodeProcessorEvaluationMode getEvaluationMode() {
        return ctx.prismParsingContext.getEvaluationMode();
    }

    private void warnOrThrow(String format, Object... args) throws SchemaException {
        String message = Strings.lenientFormat(format, args);
        ctx.prismParsingContext.warnOrThrow(LOGGER, message + ". At " + ctx.getPositionSuffix());
    }

    private static ItemProcessor namespaceSensitive(ItemProcessor processor) {
        return (reader, name, value) -> {
            reader.startNamespaceSensitive();
            processor.apply(reader, name, value);
        };
    }

    private void startNamespaceSensitive() {
        namespaceSensitiveStarted = true;
        if (map == null) {
            map = new MapXNodeImpl(parentContext);
        }
    }

    @FunctionalInterface
    private interface ItemProcessor {

        void apply(JsonObjectTokenReader reader, QName itemName, XNodeImpl value) throws SchemaException;
    }
}
