package com.evolveum.axiom.lang.antlr;

/**
 * Created by Dominik.
 *
 * Record represent token (symbol type) with specific properties.
 */
public record CustomToken(int type, IdentifierContext identifierContext) {

    // currently to need rules context just for IDENTIFIER token in path & filterName cases
    public enum IdentifierContext {
        PATH,
        FILTER_NAME,
    }
}
