package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;

/**
 * Node by locations position cursor in AST, cursorIndex points to branch in node where the terminal symbol is located
 *
 * Created by Dominik.
 */
public record PositionContext(int cursorIndex, @NotNull ParseTree node) {
}
