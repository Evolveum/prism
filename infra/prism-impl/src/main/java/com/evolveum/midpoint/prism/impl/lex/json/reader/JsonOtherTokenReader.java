/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.lex.json.reader;

import java.io.IOException;
import java.util.Objects;
import javax.xml.namespace.QName;

import com.evolveum.concepts.SourceLocation;
import com.evolveum.concepts.TechnicalMessage;
import com.evolveum.concepts.ValidationLogType;
import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.impl.lex.ValidatorUtil;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.node.ValueNode;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismNamespaceContext;
import com.evolveum.midpoint.prism.impl.lex.json.JsonNullValueParser;
import com.evolveum.midpoint.prism.impl.lex.json.JsonValueParser;
import com.evolveum.midpoint.prism.impl.xnode.ListXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.PrimitiveXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeDefinition;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import com.evolveum.midpoint.prism.xnode.ValueParser;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * TODO
 */
class JsonOtherTokenReader {

    private static final Trace LOGGER = TraceManager.getTrace(JsonOtherTokenReader.class);

    @NotNull private final JsonReadingContext ctx;
    @NotNull private final JsonParser parser;

    private final PrismNamespaceContext parentContext;

    private final XNodeDefinition def;

    private @NotNull XNodeDefinition parentDef;

    JsonOtherTokenReader(JsonReadingContext ctx, PrismNamespaceContext context, XNodeDefinition def, @NotNull XNodeDefinition parentDef) {
        this.ctx = ctx;
        this.parser = ctx.parser;
        this.parentContext = context;
        this.def = def;
        this.parentDef = parentDef;
    }

    @NotNull XNodeImpl readValue() throws IOException, SchemaException {
        JsonToken currentToken = Objects.requireNonNull(parser.currentToken(), "currentToken");
        SourceLocation sourceLocation = SourceLocation.from(null,
                parser.currentLocation().getLineNr(),
                parser.currentLocation().getColumnNr()
        );

        @NotNull XNodeImpl xNode;

        switch (currentToken) {
            case START_OBJECT:
                xNode = new JsonObjectTokenReader(ctx, parentContext, def, parentDef).read();
                break;
            case START_ARRAY:
                xNode = parseToList();
                break;
            case VALUE_STRING:
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
            case VALUE_EMBEDDED_OBJECT:             // assuming it's a scalar value e.g. !!binary (TODO)
                xNode = parseToPrimitive();
                break;
            case VALUE_NULL:
                xNode = parseToEmptyPrimitive();
                break;
            default:
                throw new SchemaException("Unexpected current token: " + currentToken + ". At: " + ctx.getPositionSuffix());
        }

        ValidatorUtil.setPositionToXNode(ctx.prismParsingContext, xNode, sourceLocation);

        return xNode;
    }

    private ListXNodeImpl parseToList() throws SchemaException, IOException {
        Validate.notNull(parser.currentToken());

        ListXNodeImpl list = new ListXNodeImpl();

        Object tid = parser.getTypeId();
        if (tid != null) {
            list.setTypeQName(ctx.yamlTagResolver.tagToTypeName(tid, ctx));
        }

        for (;;) {
            JsonToken token = parser.nextToken();
            if (token == null) {
                String msg = "Unexpected end of data while parsing a list structure at ";
                ctx.prismParsingContext.validationLogger(false, ValidationLogType.WARNING,
                        list.getSourceLocation(), new TechnicalMessage(msg),  msg);
                ctx.prismParsingContext.warnOrThrow(LOGGER, msg + ctx.getPositionSuffix());
                return list;
            } else if (token == JsonToken.END_ARRAY) {
                return list;
            } else {
                list.add(readValue());
            }
        }
    }

    private <T> PrimitiveXNodeImpl<T> parseToPrimitive() throws IOException, SchemaException {
        PrimitiveXNodeImpl<T> primitive = new PrimitiveXNodeImpl<>(this.parentContext);

        Object tid = parser.getTypeId();
        if (tid != null) {
            QName typeName = ctx.yamlTagResolver.tagToTypeName(tid, ctx);
            primitive.setTypeQName(typeName);
            primitive.setExplicitTypeDeclaration(true);
        } else {
            // We don't try to determine XNode type from the implicit JSON/YAML type (integer, number, ...),
            // because XNode type prescribes interpretation in midPoint. E.g. YAML string type would be interpreted
            // as xsd:string, even if the schema would expect e.g. timestamp.
        }

        ValueNode jn = parser.readValueAs(ValueNode.class);
        ValueParser<T> vp = new JsonValueParser<>(parser, jn, parentContext);
        primitive.setValueParser(vp);
        primitive.setAttribute(def.isXmlAttribute());
        // FIXME: Materialize when possible

        return primitive;
    }

    private <T> PrimitiveXNodeImpl<T> parseToEmptyPrimitive() {
        PrimitiveXNodeImpl<T> primitive = new PrimitiveXNodeImpl<>();
        primitive.setValueParser(new JsonNullValueParser<>());
        return primitive;
    }

}
