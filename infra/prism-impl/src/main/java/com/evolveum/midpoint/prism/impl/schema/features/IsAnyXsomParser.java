/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema.features;

import com.evolveum.midpoint.prism.schema.DefinitionFeatureParser;

import com.evolveum.midpoint.util.exception.SchemaException;

import com.sun.xml.xsom.*;
import org.jetbrains.annotations.Nullable;

/** Parser for "is any" definition feature. */
public class IsAnyXsomParser implements DefinitionFeatureParser<IsAnyXsomParser.IsAny, XSType> {

    private static final IsAnyXsomParser INSTANCE = new IsAnyXsomParser();

    public static IsAnyXsomParser instance() {
        return INSTANCE;
    }

    @Override
    public @Nullable IsAny getValue(@Nullable XSType xsType) throws SchemaException {
        if (isAny(xsType, null)) {
            return new IsAny(
                    true,
                    isAny(xsType, XSWildcard.STRTICT));
        } else {
            return null;
        }
    }

    /** Determines whether the type definition contains xsd:any (directly or indirectly) */
    private boolean isAny(XSType xsType, @Nullable Integer mode) {
        if (xsType instanceof XSComplexType complexType) {
            XSContentType contentType = complexType.getContentType();
            if (contentType != null) {
                XSParticle particle = contentType.asParticle();
                if (particle != null) {
                    XSTerm term = particle.getTerm();
                    if (term != null) {
                        return isAny(term, mode);
                    }
                }
            }
        }
        return false;
    }

    /** Determines whether the term definition contains xsd:any (directly or indirectly) */
    private boolean isAny(XSTerm term, @Nullable Integer mode) {
        if (term.isWildcard()) {
            return mode == null || mode == term.asWildcard().getMode();
        }
        if (term.isModelGroup()) {
            XSParticle[] children = term.asModelGroup().getChildren();
            if (children != null) {
                for (XSParticle childParticle : children) {
                    XSTerm childTerm = childParticle.getTerm();
                    if (childTerm != null) {
                        if (isAny(childTerm, mode)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean applicableTo(Object sourceComponent) {
        return sourceComponent instanceof XSType;
    }

    public record IsAny(
            boolean any,
            boolean anyStrict) {
    }
}
