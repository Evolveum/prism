/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema.features;

import com.evolveum.midpoint.prism.PrismItemAccessDefinition;
import com.evolveum.midpoint.prism.PrismItemAccessDefinition.Info;
import com.evolveum.midpoint.prism.impl.schema.SchemaProcessorUtil;
import com.evolveum.midpoint.prism.schema.DefinitionFeatureParser;

import com.evolveum.midpoint.prism.schema.DefinitionFeatureSerializer;
import com.evolveum.midpoint.util.exception.SchemaException;

import com.sun.xml.xsom.XSAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.List;

import static com.evolveum.midpoint.prism.PrismConstants.*;
import static com.evolveum.midpoint.prism.PrismConstants.A_ACCESS_READ;

/**
 * XSOM parser and serializer for (canAdd, canModify, canRead) access control triples.
 *
 * @see PrismItemAccessDefinition
 * @see Info
 */
public class AccessXsomProcessor
        implements
        DefinitionFeatureParser.NonNull<Info, XSAnnotation>,
        DefinitionFeatureSerializer<Info> {

    @Override
    public @NotNull Info getValue(@Nullable XSAnnotation source) throws SchemaException {
        List<Element> accessElements = SchemaProcessorUtil.getAnnotationElements(source, A_ACCESS);
        if (accessElements.isEmpty()) {
            return Info.full(); // Default access is read-write-create
        } else {
            boolean canAdd = false;
            boolean canModify = false;
            boolean canRead = false;
            for (Element e : accessElements) {
                switch (e.getTextContent()) {
                    case A_ACCESS_CREATE -> canAdd = true;
                    case A_ACCESS_UPDATE -> canModify = true;
                    case A_ACCESS_READ -> canRead = true;
                    case A_ACCESS_NONE -> { /* ok */ }
                    default -> throw new SchemaException("Unknown value of " + A_ACCESS + " annotation: " + e.getTextContent());
                }
            }
            return new Info(canAdd, canModify, canRead);
        }
    }

    @Override
    public void serialize(@NotNull Info value, @NotNull SerializationTarget target) {
        if (!value.canAdd() || !value.canRead() || !value.canModify()) {
            // read-write-create attribute is the default. If any of this flags is missing, we must add appropriate annotations.
            boolean any = false;
            if (value.canAdd()) {
                target.addAnnotation(A_ACCESS, A_ACCESS_CREATE);
                any = true;
            }
            if (value.canRead()) {
                target.addAnnotation(A_ACCESS, A_ACCESS_READ);
                any = true;
            }
            if (value.canModify()) {
                target.addAnnotation(A_ACCESS, A_ACCESS_UPDATE);
                any = true;
            }
            if (!any) {
                target.addAnnotation(A_ACCESS, A_ACCESS_NONE);
            }
        }
    }
}
