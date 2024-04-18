/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema.features;

import java.util.List;

import com.evolveum.midpoint.prism.impl.schema.annotation.Annotation;

import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

/** A list of QNames, to be used e.g. for {@link Annotation#NATURAL_KEY}. */
public class QNameList
        extends AbstractValueWrapper.ForList<QName> {

    private QNameList(List<QName> values) {
        super(values);
    }

    public static @Nullable List<QName> unwrap(@Nullable QNameList wrapper) {
        return wrapper != null ? wrapper.getValue() : null;
    }

    static @Nullable QNameList wrap(@Nullable List<QName> values) {
        return values != null && !values.isEmpty() ? new QNameList(values) : null;
    }
}
