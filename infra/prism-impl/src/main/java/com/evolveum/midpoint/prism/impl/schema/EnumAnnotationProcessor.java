/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

/**
 * Helper class to process annotations containing enum values.
 */
public class EnumAnnotationProcessor<T extends Enum<?>> extends AnnotationProcessor {

    private final Class<T> type;

    public EnumAnnotationProcessor(Class<T> type) {
        this.type = type;
    }

    @Override
    public @Nullable T convert(@NotNull Annotation annotation, @NotNull Element element) {
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
