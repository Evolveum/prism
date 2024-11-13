package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.RuleContext;

import java.util.Stack;

/**
 * Created by Dominik.
 *
 * Record represent token with context rules.
 *  Index is index of token.
 *  Rules is stack of related rules with token.
 */
public record TerminalWithContext(int type, RuleContext context, Stack<Integer> rules) {

    public TerminalWithContext withIndex(int type) {
        return new TerminalWithContext(type, this.context, this.rules);
    }

    public TerminalWithContext withContext(RuleContext context) {
        return new TerminalWithContext(this.type, context, this.rules);
    }

    // Does not necessary track context rules for every token (currently only for: IDENTIFIER)
    public TerminalWithContext withRules(Stack<Integer> rules) {
        return new TerminalWithContext(this.type, this.context, rules);
    }
}
