/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import static com.evolveum.midpoint.prism.PrismConstants.A_IGNORE;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.ItemProcessing;
import com.evolveum.midpoint.prism.MutableDefinition;
import com.evolveum.midpoint.prism.PrismConstants;

/**
 * Processes {@link PrismConstants#A_IGNORE} annotation.
 * This one is special, it's not stored directly as {@link PrismConstants#A_IGNORE} but transformed
 * to {@link PrismConstants#A_PROCESSING} equal to {@link ItemProcessing#IGNORE}.
 */
public class IgnoreProcessor extends AnnotationProcessor<MutableDefinition, ItemProcessing> {

    public IgnoreProcessor() {
        super(A_IGNORE, ItemProcessing.class, MutableDefinition::setProcessing);
    }

    @Override
    public @Nullable ItemProcessing convert(@NotNull Element element) {
        String value = element.getTextContent();
        if (value == null || value.isEmpty()) {
            return ItemProcessing.IGNORE;
        }

        return "true".equals(value) ? ItemProcessing.IGNORE : null;
    }

    @Override
    public void process(@NotNull MutableDefinition definition, @NotNull Element element) {
        ItemProcessing value = convert(element);
        if (value == null) {
            return;
        }

        setValue.accept(definition, value);
        definition.setAnnotation(PrismConstants.A_PROCESSING, value);
    }
}
