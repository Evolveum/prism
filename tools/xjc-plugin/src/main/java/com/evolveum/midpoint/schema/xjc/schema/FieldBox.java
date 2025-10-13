/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.schema.xjc.schema;

import org.apache.commons.lang3.Validate;

/**
 * @author lazyman
 */
public class FieldBox<T> implements Comparable<FieldBox> {

    private String fieldName;
    private T value;

    public FieldBox(String fieldName, T value) {
        Validate.notEmpty(fieldName, "Field name must not be null or empty.");
        Validate.notNull(value);

        this.fieldName = fieldName;
        this.value = value;
    }

    String getFieldName() {
        return fieldName;
    }

    T getValue() {
        return value;
    }

    @Override
    public int compareTo(FieldBox fieldBox) {
        return String.CASE_INSENSITIVE_ORDER.compare(getFieldName(), fieldBox.getFieldName());
    }
}
