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
import java.util.function.BiConsumer;

/**
 * Helper base class to process annotations in different ways.
 *
 * Non-null value is stored in annotation map via {@link MutableDefinition#setAnnotation(QName, Object)}.
 */
public class AnnotationProcessor<D extends MutableDefinition, T> {

    public final QName name;

    public final Class<T> type;

    public final Class<D> definitionType;

    public final BiConsumer<D, T> setValue;

    public final T defaultValue;

    public AnnotationProcessor(QName name, Class<T> type, BiConsumer<D, T> setValue) {
        this(name, type, MutableDefinition.class, setValue, null);
    }

    public AnnotationProcessor(QName name, Class<T> type, BiConsumer<D, T> setValue, T defaultValue) {
        this(name, type, MutableDefinition.class, setValue, defaultValue);
    }

    public AnnotationProcessor(QName name, Class<T> type, Class definitionType, BiConsumer<D, T> setValue, T defaultValue) {
        this.name = name;
        this.type = type;
        this.definitionType = definitionType;
        this.setValue = setValue;
        this.defaultValue = defaultValue;
    }

    protected @Nullable T convert(@NotNull Element element) throws SchemaException {
        String textContent = element.getTextContent();
        if (textContent == null || textContent.isEmpty()) {
            return defaultValue;
        }

        return XmlTypeConverter.toJavaValue(element, type);
    }

    public void process(@NotNull D definition, @NotNull Element element) throws SchemaException {
        T value = convert(element);
        if (value == null) {
            return;
        }

        setValue.accept(definition, value);

        definition.setAnnotation(name, value);
    }
}
