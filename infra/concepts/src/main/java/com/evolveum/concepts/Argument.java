/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.concepts;

public record Argument(
        Object value,
        ArgumentType type
) {

    public enum ArgumentType {
        XNODE,
        QNAME,
        ELEMENT,
        DEFINITION,
        DEFINITION_LIST,
        STRING,
        RAW,
        UNKNOW
    }
}
