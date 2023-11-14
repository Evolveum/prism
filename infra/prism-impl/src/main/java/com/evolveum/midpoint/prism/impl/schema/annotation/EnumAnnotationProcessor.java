/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema.annotation;

import com.evolveum.midpoint.prism.MutableDefinition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

/**
 * Helper class to process annotations containing enum values.
 */
public class EnumAnnotationProcessor<D extends MutableDefinition, T extends Enum<?>> extends AnnotationProcessor<D, T> {

    public EnumAnnotationProcessor(QName name, Class<T> type, Class definitionType, BiConsumer<D, T> setValue) {
        super(name, type, definitionType, setValue, null);
    }

    @Override
    public @Nullable T convert(@NotNull Element element) {
        String value = element.getTextContent();
        if (value == null) {
            return null;
        }

        for (T t : type.getEnumConstants()) {
            if (equals(t, value)) {
                return t;
            }
        }

        return null;
    }

    protected boolean equals(@NotNull T t, @NotNull String value) {
        return t.name().equals(value);
    }
}
