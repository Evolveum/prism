/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.MutableDefinition;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * Helper base class to process annotations in different ways.
 *
 * Non-null value is stored in annotation map via {@link MutableDefinition#setAnnotation(QName, Object)}.
 */
public class AnnotationProcessor {

    /**
     * Default implementation that converts annotation value to Java value using {@link XmlTypeConverter}.
     */
    protected @Nullable Object convert(@NotNull Annotation annotation, @NotNull Element element) throws SchemaException {
        String textContent = element.getTextContent();
        if (textContent == null || textContent.isEmpty()) {
            return annotation.defaultValue;
        }

        return XmlTypeConverter.toJavaValue(element, annotation.type);
    }

    /**
     * Non-null value is stored in annotation map via {@link MutableDefinition#setAnnotation(QName, Object)}.
     */
    public void process(@NotNull Annotation annotation, @NotNull MutableDefinition definition, @NotNull Element element)
            throws SchemaException {
        Object value = convert(annotation, element);
        if (value != null) {
            definition.setAnnotation(annotation.name, value);
        }
    }
}
