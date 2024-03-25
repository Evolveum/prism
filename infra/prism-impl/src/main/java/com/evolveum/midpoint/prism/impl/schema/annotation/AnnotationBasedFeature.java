/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema.annotation;

import java.util.function.BiConsumer;
import javax.xml.namespace.QName;

import com.sun.xml.xsom.XSAnnotation;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.impl.DefinitionImpl;
import com.evolveum.midpoint.prism.impl.schema.features.DefinitionFeatures.XsomParsers;
import com.evolveum.midpoint.prism.schema.DefinitionFeature;
import com.evolveum.midpoint.prism.schema.DefinitionFeatureParser;

/**
 * Basically the same as the default (and currently only) implementation of the {@link DefinitionFeature}.
 * It contains an extra {@link #annotationName}, but it's unused since we - maybe temporarily - stopped putting
 * these annotations into {@link DefinitionImpl#annotations} map.
 *
 * @see Annotation
 */
public class AnnotationBasedFeature<V, DB>
        extends DefinitionFeature.DefaultImpl<V, DB, XSAnnotation, Object> {

    /** Name of the corresponding annotation, e.g. {@link PrismConstants#A_ALWAYS_USE_FOR_EQUALS} */
    private final QName annotationName;

    private AnnotationBasedFeature(
            QName annotationName, Class<V> valueType, Class<DB> definitionBuilderType,
            BiConsumer<DB, V> setterMethod, @NotNull DefinitionFeatureParser<V, XSAnnotation> xsomParser) {
        super(valueType, definitionBuilderType, setterMethod, xsomParser, null, null, null);
        this.annotationName = annotationName;
    }

    static <DB> AnnotationBasedFeature<Boolean, DB> forBooleanMark(
            QName annotationName, Class<DB> definitionBuilderType, BiConsumer<DB, Boolean> setterMethod) {
        return custom(
                annotationName, Boolean.class, definitionBuilderType, setterMethod,
                XsomParsers.marker(annotationName).restrictToSource(XSAnnotation.class));
    }

    static <DB> AnnotationBasedFeature<String, DB> forString(
            QName annotationName, Class<DB> definitionBuilderType, BiConsumer<DB, String> setterMethod) {
        return forType(annotationName, String.class, definitionBuilderType, setterMethod);
    }

    public static <V, DB> AnnotationBasedFeature<V, DB> forType(
            QName annotationName, Class<V> valueType, Class<DB> definitionBuilderType, BiConsumer<DB, V> setterMethod) {
        return custom(
                annotationName, valueType, definitionBuilderType, setterMethod,
                XsomParsers.singleAnnotationValue(valueType, annotationName));
    }

    public static <V, DB> AnnotationBasedFeature<V, DB> custom(
            QName annotationName, Class<V> valueType, Class<DB> definitionBuilderType, BiConsumer<DB, V> setterMethod,
            DefinitionFeatureParser<V, XSAnnotation> xsomParser) {
        return new AnnotationBasedFeature<>(
                annotationName, valueType, definitionBuilderType, setterMethod, xsomParser);
    }
}
