package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.RuleContext;

import java.util.Stack;

/**
 * Created by Dominik.
 *
 * Record represent terminal (symbol type) with context, relate rules, previous & next token.
 *  Index is index of token.
 *  Rules is stack of related rules with token.
 */
public record TerminalWithContext(int type, RuleContext context, Stack<Integer> rules, TerminalWithContext previousTerminal, TerminalWithContext nextTerminal) {

    public TerminalWithContext(int type, RuleContext context) {
        this(type, context, null, null, null);
    }

    public TerminalWithContext(int type, RuleContext context, Stack<Integer> rules) {
        this(type, context, rules, null, null);
    }
}
