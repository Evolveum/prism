/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.ItemProcessing;
import com.evolveum.midpoint.prism.MutableItemDefinition;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

import com.sun.xml.xsom.XSAnnotation;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import static com.evolveum.midpoint.prism.PrismConstants.*;

/**
 * TODO DOC
 */
public enum Annotation {

    IGNORE(A_IGNORE, Boolean.class, true),

    PROCESSING(A_PROCESSING, ItemProcessing.class, new ItemProcessingConverter(), null),

    DEPRECATED(A_DEPRECATED, Boolean.class, true),

    DEPRECATED_SINCE(A_DEPRECATED_SINCE, String.class, null),

    REMOVED(A_REMOVED, Boolean.class, true),

    REMOVED_SINCE(A_REMOVED_SINCE, String.class, null),

    EXPERIMENTAL(A_EXPERIMENTAL, Boolean.class, true),

    PLANNED_REMOVAL(A_PLANNED_REMOVAL, Boolean.class, true),

    ELABORATE(A_ELABORATE, Boolean.class, true),

    OPERATIONAL(A_OPERATIONAL, Boolean.class, true),

    DISPLAY_NAME(A_DISPLAY_NAME, String.class, null),

    DISPLAY_ORDER(A_DISPLAY_ORDER, Integer.class, null),

    HELP(A_HELP, String.class, null),

    EMPHASIZED(A_EMPHASIZED, Boolean.class, true),

    SEARCHABLE(A_SEARCHABLE, Boolean.class, true),

    DOCUMENTATION(DOMUtil.XSD_DOCUMENTATION_ELEMENT, String.class, (a) -> DOMUtil.serializeElementContent(a), null),

    HETEROGENEOUS_LIST_ITEM(A_HETEROGENEOUS_LIST_ITEM, Boolean.class, true),

    // todo schema migration

    // todo diagrams?

    OBJECT_REFERENCE_TARGET_TYPE(A_OBJECT_REFERENCE_TARGET_TYPE, QName.class, a -> DOMUtil.getQNameValue(a), null);

    // todo others?

    final QName name;

    final Class<?> type;

    final AnnotationConverter<?> converter;

    final Object defaultValue;

    Annotation(QName name, Class<?> type, Object defaultValue) {
        this(name, type, null, defaultValue);
    }

    Annotation(QName name, Class<?> type, AnnotationConverter<?> converter, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.converter = converter;
        this.defaultValue = defaultValue;
    }

    public static void processAnnotations(MutableItemDefinition<?> itemDef, XSAnnotation annotation) throws SchemaException {
        for (Annotation a : Annotation.values()) {
            processAnnotation(itemDef, annotation, a);
        }
    }

    public static void processAnnotation(MutableItemDefinition<?> itemDef, XSAnnotation annotation, Annotation toProcess) throws SchemaException {
        Element element = SchemaProcessorUtil.getAnnotationElement(annotation, toProcess.name);
        if (element == null) {
            return;
        }

        Object value;
        AnnotationConverter<?> converter = toProcess.converter;
        if (converter != null) {
            value = converter.convert(element);
        } else {
            String textContent = element.getTextContent();
            if (textContent == null || textContent.isEmpty()) {
                value = toProcess.defaultValue;
            } else {
                value = XmlTypeConverter.toJavaValue(element, toProcess.type);
            }
        }

        if (value == null) {
            value = toProcess.defaultValue;
        }

        if (value != null) {
            itemDef.setAnnotation(toProcess.name, value);
        }
    }
}
