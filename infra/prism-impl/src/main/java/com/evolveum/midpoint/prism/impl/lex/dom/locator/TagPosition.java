package com.evolveum.midpoint.prism.impl.lex.dom.locator;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Dominik.
 */
public record TagPosition(
        String name,
        int line,
        int column,
        TagPosition.Type type
) {
    public enum Type {START, END}

    @Override
    public @NotNull String toString() {
        return type + " <" + name + "> at line " + line + ", column " + column;
    }
}
