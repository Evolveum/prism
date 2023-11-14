/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema.annotation;

import java.util.List;
import java.util.function.BiConsumer;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.MutableDefinition;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Annotation processor base class.
 *
 * Non-null value is stored in definition via setter if provided and in annotation map via {@link MutableDefinition#setAnnotation(QName, Object)}.
 * This implementation doesn't take multi-value annotations into account, only first one is processed.
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

    public void process(@NotNull D definition, @NotNull List<Element> elements) throws SchemaException {
        if (elements.isEmpty()) {
            return;
        }

        T value = convert(elements.get(0));
        if (value == null) {
            return;
        }

        setValue.accept(definition, value);

        definition.setAnnotation(name, value);
    }
}
