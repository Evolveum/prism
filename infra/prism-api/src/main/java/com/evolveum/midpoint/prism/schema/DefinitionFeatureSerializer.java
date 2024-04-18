/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.schema;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

/**
 * Serializes given feature, currently into XSD DOM.
 *
 * Currently supported only for some features, particularly those that are serialized into "appInfo" XSD element.
 *
 * @param <V> type of the feature value ({@link Boolean}, {@link String}, {@link QName}, ...
 */
public interface DefinitionFeatureSerializer<V> {

    void serialize(@NotNull V value, @NotNull SerializationTarget target);

    /** Interface through which we put information into DOM. */
    interface SerializationTarget {
        void addAnnotation(QName name, Boolean value);
        void addAnnotation(QName name, String value);
        void addAnnotation(QName qname, QName value);
        void addRefAnnotation(QName qname, QName value);
    }
}
