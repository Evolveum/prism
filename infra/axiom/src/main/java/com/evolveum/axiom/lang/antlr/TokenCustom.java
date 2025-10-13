/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.lang.antlr;

import java.util.Objects;

/**
 * Created by Dominik.
 *
 * Record represent token (symbol type) with specific properties.
 */
public record TokenCustom(int type, IdentifierContext identifierContext) {
    // currently to need rules context just for IDENTIFIER token in path & filterName cases
    public enum IdentifierContext {
        PATH,
        SUBFILTER_OR_VALUE,
        FILTER_NAME,
        MATCHING
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TokenCustom other = (TokenCustom) obj;
        return type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
