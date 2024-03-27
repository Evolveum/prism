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
    public @Nullable Info getValue(@Nullable XSAnnotation source) throws SchemaException {
        List<Element> accessElements = SchemaProcessorUtil.getAnnotationElements(source, A_ACCESS);
        if (accessElements.isEmpty()) {
            // This means the default, which is "full access", but due to the way element annotations are processed
            // in SchemaXsomParser, we should return null value if there's no explicit information. So the XSAnnotation items
            // with no "a:access" annotation will not falsely give full access.
            //
            // This works well with prism item definitions, which have "all access" as their default; and keeps it,
            // unless explicitly stated otherwise in XSD.
            return null;
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

        if (value.canAdd() && value.canRead() && value.canModify()) {
            // Read-write-create access is the default. Only if any of this flags is missing,
            // we must add appropriate annotations.
            return;
        }

        boolean anyAccess = false;
        if (value.canAdd()) {
            target.addAnnotation(A_ACCESS, A_ACCESS_CREATE);
            anyAccess = true;
        }
        if (value.canRead()) {
            target.addAnnotation(A_ACCESS, A_ACCESS_READ);
            anyAccess = true;
        }
        if (value.canModify()) {
            target.addAnnotation(A_ACCESS, A_ACCESS_UPDATE);
            anyAccess = true;
        }
        if (!anyAccess) {
            target.addAnnotation(A_ACCESS, A_ACCESS_NONE);
        }
    }
}
