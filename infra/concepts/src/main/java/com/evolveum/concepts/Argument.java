/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.concepts;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Value referenced from a message template, together with a hint about what kind of value it is,
 * so that a consumer can process the message without parsing its text.
 */
public record Argument(
        Object value,
        ArgumentType type
) {

    /** Creates argument with the type derived from the value. An argument passed in is returned as is. */
    public static Argument of(Object value) {
        if (value instanceof Argument argument) {
            return argument;
        }
        return new Argument(value, typeOf(value));
    }

    /** For pre-rendered content such as dumps, where the type cannot be derived from the value. */
    public static Argument raw(Object value) {
        return new Argument(value, ArgumentType.RAW);
    }

    private static ArgumentType typeOf(Object value) {
        if (value instanceof String) {
            return ArgumentType.STRING;
        } else if (value instanceof Integer || value instanceof Long || value instanceof Short) {
            return ArgumentType.INT;
        } else if (value instanceof QName) {
            return ArgumentType.QNAME;
        } else if (value instanceof Element) {
            return ArgumentType.ELEMENT;
        } else {
            return ArgumentType.UNKNOWN;
        }
    }

    public enum ArgumentType {
        XNODE,
        XMAP,
        XLIST,
        QNAME,
        ELEMENT,
        DEFINITION,
        DEFINITION_LIST,
        STRING,
        INT,
        RAW,
        UNKNOWN
    }
}
