/*
 * Copyright (C) 2020-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.lex.json.reader;

import static com.evolveum.midpoint.prism.impl.lex.json.JsonInfraItems.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.namespace.QName;

import com.evolveum.concepts.SourceLocation;
import com.evolveum.concepts.ValidationMessageType;
import com.evolveum.midpoint.prism.impl.lex.ValidatorUtil;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
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
                warnOrThrow(msg);
                ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, msg, null, SourceLocation.unknown());
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
        String newFieldName = parser.getCurrentName();
        if (currentFieldName != null) {
            String msg = "Two field names in succession: " + currentFieldName.getName() + " and " + newFieldName;
            warnOrThrow(msg);
            SourceLocation sourceLocation = SourceLocation.from("XNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr());
            ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, msg, null, sourceLocation);
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
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, value, SourceLocation.from("XNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));
        value.setDefinition(name.itemDefinition());
        PROCESSORS.getOrDefault(name.getName(), STANDARD_PROCESSOR).apply(this, name.getName(), value);
    }

    private XNodeImpl readValue(XNodeDefinition fieldDef) throws IOException, SchemaException {
        return new JsonOtherTokenReader(ctx, namespaceContext().inherited(), fieldDef, definition).readValue();
    }

    private PrismNamespaceContext namespaceContext() {
        if (map != null) {
            ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, map, SourceLocation.from("mapXNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));
            return map.namespaceContext();
        }
        return parentContext.inherited();
    }

    private void processContextDeclaration(QName name, XNodeImpl value) throws SchemaException {
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, value, SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));

        if (value instanceof MapXNode) {
            Builder<String, String> nsCtx = ImmutableMap.<String, String>builder();
            for (Entry<QName, ? extends XNode> entry : ((MapXNode) value).toMap().entrySet()) {
                String key = entry.getKey().getLocalPart();
                String ns = getCurrentFieldStringValue(entry.getKey(), entry.getValue());
                nsCtx.put(key, ns);
            }
            this.map = new MapXNodeImpl(parentContext.childContext(nsCtx.build()));
            ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, map, SourceLocation.from("mapXNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));
            return;
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    private void processStandardFieldValue(QName name, @NotNull XNodeImpl currentFieldValue) {
        // MID-5326:
        //   If namespace is defined, fieldName is always qualified,
        //   If namespace is undefined, then we can not effectivelly distinguish between

        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, map, SourceLocation.from("mapXNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));
        map.put(name, currentFieldValue);
    }

    private void processIncompleteDeclaration(QName name, XNodeImpl currentFieldValue) throws SchemaException {
        SourceLocation sourceLocation = SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr());
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, currentFieldValue, sourceLocation);

        if (incomplete != null) {
            String msg = "Duplicate @incomplete marker found with the value: %s";
            warnOrThrow(String.format(msg, currentFieldValue));
            ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, String.format(msg, currentFieldValue.getElementName()), String.format(msg, currentFieldValue), sourceLocation);
        } else if (currentFieldValue instanceof PrimitiveXNodeImpl) {
            //noinspection unchecked
            Boolean realValue = ((PrimitiveXNodeImpl<Boolean>) currentFieldValue)
                    .getParsedValue(DOMUtil.XSD_BOOLEAN, Boolean.class, getEvaluationMode());
            incomplete = Boolean.TRUE.equals(realValue);
        } else {
            String msg = "@incomplete marker found with incompatible value: %s";
            warnOrThrow(String.format(msg, currentFieldValue));
            ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, String.format(msg, currentFieldValue.getElementName()), String.format(msg, currentFieldValue), sourceLocation);
        }
    }

    private void processWrappedValue(QName name, XNodeImpl currentFieldValue) throws SchemaException {
        SourceLocation sourceLocation = SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr());
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, currentFieldValue, sourceLocation);

        if (wrappedValue != null) {
            String msg = "Value (' %s ') defined more than once";
            warnOrThrow(String.format(msg, JsonInfraItems.PROP_VALUE));
            ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, String.format(msg, JsonInfraItems.PROP_VALUE), null, sourceLocation);

        }
        wrappedValue = currentFieldValue;
    }

    private void processMetadataValue(QName name, XNodeImpl currentFieldValue) throws SchemaException {

        SourceLocation sourceLocation = SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr());
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, currentFieldValue, sourceLocation);

        if (currentFieldValue instanceof MapXNode) {
            metadata.add((MapXNode) currentFieldValue);
        } else if (currentFieldValue instanceof ListXNodeImpl) {
            for (XNode metadataValue : (ListXNodeImpl) currentFieldValue) {
                if (metadataValue instanceof MapXNode) {
                    metadata.add((MapXNode) metadataValue);
                } else {
                    String msg = "Metadata is not a map XNode: %s";
                    warnOrThrow(String.format(msg, metadataValue.debugDump()));
                    ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, String.format(msg, metadataValue.getTypeQName().getLocalPart()), String.format(msg, metadataValue.debugDump()), sourceLocation);
                }
            }
        } else {
            String msg = "Metadata is not a map or list XNode: %s";
            warnOrThrow(String.format(msg, currentFieldValue.debugDump()));
            ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, String.format(msg, currentFieldValue.debugDump()), null, sourceLocation);
        }
    }

    private void processElementNameDeclaration(QName name, XNodeImpl value) throws SchemaException {

        SourceLocation sourceLocation = SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr());
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, value, sourceLocation);

        if (elementName != null) {
            String msg = "Element name defined more than once";
            warnOrThrow(msg);
            ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, msg, null, sourceLocation);
        }
        String nsName = getCurrentFieldStringValue(name, value);
        @NotNull
        XNodeDefinition maybeDefinition = parentDefinition.resolve(nsName, namespaceContext());
        elementName = resolveQName(nsName);
        replaceDefinition(maybeDefinition);
    }

    private void processId(QName name, XNodeImpl value) {
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, value, SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));

        if (value instanceof PrimitiveXNodeImpl<?>) {
            ((PrimitiveXNodeImpl) value).setAttribute(true);
        }
        containerId = value;
    }

    private void replaceDefinition(@NotNull XNodeDefinition maybeDefinition) {
        definition = definition.moreSpecific(maybeDefinition);
    }

    private void processTypeDeclaration(QName name, XNodeImpl value) throws SchemaException {
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, value, SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));

        if (typeName != null) {
            String msg = "Value type defined more than once";
            warnOrThrow(msg);
            ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, msg, null, SourceLocation.unknown());
        }
        String stringValue = getCurrentFieldStringValue(name, value);
        // TODO: Compat: We treat default prefixes as empty namespace, not default namespace
        typeName = XNodeDefinition.resolveQName(stringValue, namespaceContext());
        definition = definition.withType(typeName);
    }

    private void processNamespaceDeclaration(QName name, XNodeImpl value) throws SchemaException {
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, value, SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));

        if (namespaceSensitiveStarted) {
            String msg = "Namespace declared after other fields: %s";
            warnOrThrow(String.format(msg, ctx.getPositionSuffix()));
            ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, String.format(msg, ""), String.format(msg, ctx.getPositionSuffix()), SourceLocation.unknown());
        }
        if (map != null) {
            String msg = "Namespace defined more than once";
            warnOrThrow(msg);
            ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, msg, null, SourceLocation.unknown());
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
            warnOrThrow(String.format(msg, PROP_VALUE, PROP_INCOMPLETE));
            ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, msg, null, SourceLocation.unknown());
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

        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, ret, SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));

        return ret;
    }

    private void addIdTo(XNodeImpl ret) {
        if (containerId != null && ret instanceof MapXNodeImpl) {
            ((MapXNodeImpl) ret).put(XNodeImpl.KEY_CONTAINER_ID, containerId);
        }
    }

    private void addMetadataTo(XNodeImpl rv) throws SchemaException {
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, rv, SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));

        if (!metadata.isEmpty()) {
            if (rv instanceof MetadataAware) {
                ((MetadataAware) rv).setMetadataNodes(metadata);
            } else {
                String msg = "Couldn't apply metadata to non-metadata-aware node: %s";
                warnOrThrow(String.format(msg, rv.getClass()));
                ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, String.format(msg, rv.getClass().getSimpleName()), String.format(msg, rv.getClass()), SourceLocation.unknown());
            }
        }
    }

    private void addElementNameTo(XNodeImpl rv) throws SchemaException {
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, rv, SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));

        if (elementName != null) {
            if (wrappedValue != null && wrappedValue.getElementName() != null) {
                if (!wrappedValue.getElementName().equals(elementName)) {
                    String msg = "Conflicting element names for '%s' (%s) and regular content (%s; ) present";
                    warnOrThrow(String.format(msg, JsonInfraItems.PROP_VALUE, wrappedValue.getElementName(), elementName));
                    ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, String.format(msg, JsonInfraItems.PROP_VALUE, wrappedValue.getElementName().getLocalPart(), elementName.getLocalPart()), String.format(msg, JsonInfraItems.PROP_VALUE, wrappedValue.getElementName(), elementName), SourceLocation.unknown());
                }
            }
            rv.setElementName(elementName);
        }

    }

    private void addTypeNameTo(XNodeImpl rv) throws SchemaException {
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, rv, SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));

        if (typeName != null) {
            if (wrappedValue != null && wrappedValue.getTypeQName() != null && !wrappedValue.getTypeQName().equals(typeName)) {
                String msg = "Conflicting type names for '%s' (%s) and regular content (%s) present";
                warnOrThrow(String.format(msg, JsonInfraItems.PROP_VALUE, wrappedValue.getTypeQName(), typeName));
                ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, String.format(msg, JsonInfraItems.PROP_VALUE, wrappedValue.getTypeQName().getLocalPart(), typeName.getLocalPart()), String.format(msg, JsonInfraItems.PROP_VALUE, wrappedValue.getTypeQName(), typeName), SourceLocation.unknown());
            }
            rv.setTypeQName(typeName);
            rv.setExplicitTypeDeclaration(true);
        }
    }

    private String getCurrentFieldStringValue(QName name, XNode currentFieldValue) throws SchemaException {
        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, currentFieldValue, SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));

        if (currentFieldValue instanceof PrimitiveXNodeImpl) {
            return ((PrimitiveXNodeImpl<?>) currentFieldValue).getStringValue();
        } else {
            String msg = "Value of '%s' attribute must be a primitive one. It is %s instead";
            warnOrThrow(String.format(msg, name, currentFieldValue));
            ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, String.format(msg, name.getLocalPart(), currentFieldValue.getTypeQName().getLocalPart()), String.format(msg, name, currentFieldValue), SourceLocation.unknown());
            return "";
        }
    }

    private XNodeProcessorEvaluationMode getEvaluationMode() {
        return ctx.prismParsingContext.getEvaluationMode();
    }

    private void warnOrThrow(String format, Object... args) throws SchemaException {
        String message = Strings.lenientFormat(format, args);
        ctx.prismParsingContext.warnOrThrow(LOGGER, message + ". At " + ctx.getPositionSuffix());
        ValidatorUtil.registerRecord(ctx.prismParsingContext, ValidationMessageType.WARNING, message, null, SourceLocation.unknown());
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
            ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, map, SourceLocation.from("xNode", parser.currentLocation().getLineNr(), parser.currentLocation().getColumnNr()));
        }
    }

    @FunctionalInterface
    private interface ItemProcessor {

        void apply(JsonObjectTokenReader reader, QName itemName, XNodeImpl value) throws SchemaException;
    }
}
