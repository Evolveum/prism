/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import static com.evolveum.midpoint.prism.PrismConstants.*;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.DisplayHint;

import com.sun.xml.xsom.XSAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.ItemProcessing;
import com.evolveum.midpoint.prism.MutableDefinition;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Prism annotations enumeration that used for processing when definitions are being parsed.
 */
public enum Annotation {

    DEPRECATED(A_DEPRECATED, Boolean.class, true),

    DEPRECATED_SINCE(A_DEPRECATED_SINCE, String.class),

    DISPLAY_NAME(A_DISPLAY_NAME, String.class),

    DISPLAY_ORDER(A_DISPLAY_ORDER, Integer.class),

    DOCUMENTATION(DOMUtil.XSD_DOCUMENTATION_ELEMENT, String.class, null, new AnnotationProcessor() {

        @Override
        protected @Nullable Object convert(@NotNull Annotation annotation, @NotNull Element element) {
            return DOMUtil.serializeElementContent(element);
        }
    }),

    ELABORATE(A_ELABORATE, Boolean.class, true),

    @Deprecated
    EMPHASIZED(A_EMPHASIZED, Boolean.class, true),

    DISPLAY(A_DISPLAY, DisplayHint.class, null, new AnnotationProcessor() {

        @Override
        protected @Nullable Object convert(@NotNull Annotation annotation, @NotNull Element element) {
            return DisplayHint.findByValue(element.getTextContent());
        }
    }),

    EXPERIMENTAL(A_EXPERIMENTAL, Boolean.class, true),

    HELP(A_HELP, String.class),

    HETEROGENEOUS_LIST_ITEM(A_HETEROGENEOUS_LIST_ITEM, Boolean.class, true),

    IGNORE(A_IGNORE, ItemProcessing.class, null, new IgnoreProcessor()),

    OBJECT_REFERENCE_TARGET_TYPE(A_OBJECT_REFERENCE_TARGET_TYPE, QName.class, null, new AnnotationProcessor() {

        protected @Nullable Object convert(@NotNull Annotation annotation, @NotNull Element element) {
            return DOMUtil.getQNameValue(element);
        }
    }),

    OPERATIONAL(A_OPERATIONAL, Boolean.class, true),

    PLANNED_REMOVAL(A_PLANNED_REMOVAL, String.class),

    PROCESSING(A_PROCESSING, ItemProcessing.class, null, new ItemProcessingProcessor()),

    REMOVED(A_REMOVED, Boolean.class, true),

    REMOVED_SINCE(A_REMOVED_SINCE, String.class),

    SEARCHABLE(A_SEARCHABLE, Boolean.class, true);

    // todo schema migration
    // todo diagrams?
    // todo others?

    final QName name;

    final Class<?> type;

    final Object defaultValue;

    final AnnotationProcessor processor;

    Annotation(QName name, Class<?> type) {
        this(name, type, null);
    }

    Annotation(QName name, Class<?> type, Object defaultValue) {
        this(name, type, defaultValue, null);
    }

    Annotation(QName name, Class<?> type, Object defaultValue, AnnotationProcessor processor) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.processor = processor != null ? processor : new AnnotationProcessor();
    }

    public static void processAnnotations(MutableDefinition itemDef, XSAnnotation annotation) throws SchemaException {
        for (Annotation a : Annotation.values()) {
            processAnnotation(itemDef, annotation, a);
        }
    }

    public static void processAnnotation(MutableDefinition definition, XSAnnotation annotation, Annotation toProcess) throws SchemaException {
        Element element = SchemaProcessorUtil.getAnnotationElement(annotation, toProcess.name);
        if (element == null) {
            return;
        }

        toProcess.processor.process(toProcess, definition, element);
    }
}
