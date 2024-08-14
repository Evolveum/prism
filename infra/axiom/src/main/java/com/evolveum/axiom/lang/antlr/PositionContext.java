package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Position node context by cursor in parser tree.
 *
 * Created by Dominik.
 */
public record PositionContext(int cursorIndex, ParseTree node) {
}
