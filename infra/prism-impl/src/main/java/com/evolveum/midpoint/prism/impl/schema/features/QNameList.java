/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
