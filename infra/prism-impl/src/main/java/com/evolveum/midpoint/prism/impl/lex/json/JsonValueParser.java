/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.lex.json;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.PrismNamespaceContext;
import com.evolveum.midpoint.prism.impl.lex.json.reader.AbstractReader;
import com.evolveum.midpoint.prism.impl.marshaller.ItemPathHolder;
import com.evolveum.midpoint.prism.impl.xnode.XNodeDefinition;
import com.evolveum.midpoint.prism.marshaller.XNodeProcessorEvaluationMode;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.prism.xnode.ValueParser;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ValueNode;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * TODO what about thread safety?
 */
public class JsonValueParser<T> implements ValueParser<T> , Serializable {


    private static final long serialVersionUID = -5646889977104413611L;

    private final ValueNode node;
    private final PrismNamespaceContext context;

    public JsonValueParser(@NotNull JsonParser parser, ValueNode node, PrismNamespaceContext context) {
        this.node = node;
        this.context = context;
    }

    @Override
    public T parse(QName typeName, XNodeProcessorEvaluationMode mode) throws SchemaException {
        Class<?> clazz = XsdTypeMapper.toJavaTypeIfKnown(typeName);

        if(clazz == null) {
            throw new SchemaException("No mapping", typeName);
        }

        if (ItemPathType.class.isAssignableFrom(clazz)) {
            return (T) new ItemPathType(parseItemPath());
        } else if(ItemPath.class.isAssignableFrom(clazz)) {
            return (T) parseItemPath();
        } if(QName.class.isAssignableFrom(clazz)) {
            return (T) XNodeDefinition.resolveQName(getStringValue(), context);
        } if(XMLGregorianCalendar.class.isAssignableFrom(clazz)) {
            return (T) XmlTypeConverter.createXMLGregorianCalendar(getStringValue());
        }

        ObjectReader r = AbstractReader.OBJECT_MAPPER.readerFor(clazz);

        try {
            return r.readValue(node);
            // TODO implement COMPAT mode
        } catch (IOException e) {
            throw new SchemaException("Cannot parse value: " + e.getMessage(), e);
        }
    }

    private ItemPath parseItemPath() {
        return ItemPathHolder.parseFromString(getStringValue(), context.allPrefixes());
    }

    @Override
    public boolean canParseAs(QName typeName) {
        return XsdTypeMapper.toJavaTypeIfKnown(typeName) != null; // TODO do we have a reader for any relevant class?
    }

    @Override
    public boolean isEmpty() {
        return node == null || StringUtils.isBlank(node.asText());            // to be consistent with PrimitiveXNode.isEmpty for parsed values
    }

    @Override
    public String getStringValue() {
        if (node == null) {
            return null;
        }
        return node.asText();
    }

    @Override
    public String toString() {
        return "JsonValueParser(JSON value: "+node+")";
    }

    @Override
    public Map<String, String> getPotentiallyRelevantNamespaces() {
        return context.allPrefixes();
    }

    @Override
    public ValueParser<T> freeze() {
        // Value parser is effectivelly immutable
        return this;
    }

    public Element asDomElement() throws IOException {
        ObjectReader r = AbstractReader.OBJECT_MAPPER.readerFor(Document.class);
        return ((Document) r.readValue(node)).getDocumentElement();
    }
}
