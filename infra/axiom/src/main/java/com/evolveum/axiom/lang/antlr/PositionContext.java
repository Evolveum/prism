package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;

/**
 * Position node context by cursor in parser tree.
 *
 * Created by Dominik.
 */
public record PositionContext(int cursorIndex, @NotNull ParseTree node) {
}
