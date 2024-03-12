/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema.annotation;

import static com.evolveum.midpoint.prism.PrismConstants.*;

import java.util.List;
import javax.xml.namespace.QName;

import com.sun.xml.xsom.XSAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.schema.SchemaProcessorUtil;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Prism annotations enumeration that used for processing when definitions are being parsed.
 */
public enum Annotation {

    ALWAYS_USE_FOR_EQUALS(new AlwaysUseForEqualsProcessor()),

    DEPRECATED(new AnnotationProcessor<>(
            A_DEPRECATED, Boolean.class, MutableDefinition::setDeprecated, true)),

    DEPRECATED_SINCE(new AnnotationProcessor<MutableItemDefinition, String>(
            A_DEPRECATED_SINCE, String.class, MutableItemDefinition.class, MutableItemDefinition::setDeprecatedSince, null)),

    DISPLAY_NAME(new AnnotationProcessor<>(
            A_DISPLAY_NAME, String.class, MutableDefinition::setDisplayName)),

    DISPLAY_ORDER(new AnnotationProcessor<>(
            A_DISPLAY_ORDER, Integer.class, MutableDefinition::setDisplayOrder)),

    DOCUMENTATION(new AnnotationProcessor<>(
            DOMUtil.XSD_DOCUMENTATION_ELEMENT, String.class, MutableDefinition::setDocumentation) {

        @Override
        protected @Nullable String convert(@NotNull Element element) {
            return DOMUtil.serializeElementContent(element);
        }
    }),

    ELABORATE(new AnnotationProcessor<MutableItemDefinition<?>, Boolean>(
            A_ELABORATE, Boolean.class, MutableItemDefinition::setElaborate, true)),

    @Deprecated
    EMPHASIZED(new AnnotationProcessor<>(
            A_EMPHASIZED, Boolean.class, MutableDefinition::setEmphasized, true)),

    DISPLAY_HINT(new AnnotationProcessor<>(
            A_DISPLAY_HINT, DisplayHint.class, MutableDefinition::setDisplayHint) {

        @Override
        protected @Nullable DisplayHint convert(@NotNull Element element) {
            return DisplayHint.findByValue(element.getTextContent());
        }

        @Override
        public void process(@NotNull MutableDefinition definition, @NotNull List<Element> elements) throws SchemaException {
            super.process(definition, elements);

            // backward compatibility with emphasized annotation
            if (definition.getDisplayHint() == DisplayHint.EMPHASIZED) {
                definition.setEmphasized(true);
            }
        }
    }),

    EXPERIMENTAL(new AnnotationProcessor<>(
            A_EXPERIMENTAL, Boolean.class, MutableDefinition::setExperimental, true)),

    HELP(new AnnotationProcessor<>(
            A_HELP, String.class, MutableDefinition::setHelp)),

    HETEROGENEOUS_LIST_ITEM(new AnnotationProcessor<MutableItemDefinition<?>, Boolean>(
            A_HETEROGENEOUS_LIST_ITEM, Boolean.class, MutableItemDefinition.class, MutableItemDefinition::setHeterogeneousListItem, true)),

    IGNORE(new IgnoreProcessor()),

    MERGE(new AnnotationProcessor<MutableItemDefinition<?>, Merge>(
            A_MERGE, Merge.class, MutableItemDefinition::setMerge) {

        @Override
        protected @Nullable Merge convert(@NotNull Element element) {
            String merger = null;
            Element mergerElement = DOMUtil.getChildElement(element, A_MERGER);
            if (mergerElement != null) {
                merger = mergerElement.getTextContent();
            }

            List<Element> identifierElements = DOMUtil.getChildElements(element, A_IDENTIFIER);
            List<QName> identifiers = identifierElements.stream()
                    .map(DOMUtil::getQNameValue)
                    .toList();

            return new Merge(merger, identifiers);
        }
    }),

    OBJECT_REFERENCE_TARGET_TYPE(new AnnotationProcessor<>(
            A_OBJECT_REFERENCE_TARGET_TYPE, QName.class, MutablePrismReferenceDefinition.class, MutablePrismReferenceDefinition::setTargetTypeName, null) {

        protected @Nullable QName convert(@NotNull Element element) {
            return DOMUtil.getQNameValue(element);
        }
    }),

    OPERATIONAL(new AnnotationProcessor<MutableItemDefinition<?>, Boolean>(
            A_OPERATIONAL, Boolean.class, MutableItemDefinition.class, MutableItemDefinition::setOperational, true)),

    OPTIONAL_CLEANUP(new AnnotationProcessor<>(
            A_OPTIONAL_CLEANUP, Boolean.class, MutableDefinition::setOptionalCleanup, true)),

    PLANNED_REMOVAL(new AnnotationProcessor<MutableItemDefinition<?>, String>(
            A_PLANNED_REMOVAL, String.class, MutableItemDefinition.class, MutableItemDefinition::setPlannedRemoval, null)),

    PROCESSING(new ItemProcessingProcessor()),

    REMOVED(new AnnotationProcessor<>(
            A_REMOVED, Boolean.class, MutableDefinition::setRemoved, true)),

    REMOVED_SINCE(new AnnotationProcessor<>(
            A_REMOVED_SINCE, String.class, MutableDefinition::setRemovedSince)),

    SEARCHABLE(new AnnotationProcessor<MutableItemDefinition<?>, Boolean>(
            A_SEARCHABLE, Boolean.class, MutableItemDefinition.class, MutableItemDefinition::setSearchable, true));

    final AnnotationProcessor processor;

    Annotation(AnnotationProcessor processor) {
        this.processor = processor;
    }

    public static void processAnnotations(MutableDefinition itemDef, XSAnnotation annotation) throws SchemaException {
        for (Annotation a : Annotation.values()) {
            processAnnotation(itemDef, annotation, a);
        }
    }

    public static void processAnnotation(MutableDefinition definition, XSAnnotation xsAnnotation, Annotation annotation) throws SchemaException {
        if (!annotation.processor.definitionType.isAssignableFrom(definition.getClass())) {
            return;
        }

        List<Element> elements = SchemaProcessorUtil.getAnnotationElements(xsAnnotation, annotation.processor.name);
        if (elements.isEmpty()) {
            return;
        }

        annotation.processor.process(definition, elements);
    }
}
