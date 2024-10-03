package com.evolveum.axiom.lang.antlr;

import java.util.Stack;

/**
 * Created by Dominik.
 *
 * Record represent token with context rules.
 *  Index is index of token.
 *  Rules is stack of related rules with token.
 */
public record TokenWithCtx(int index, Stack<Integer> rules) {

    // Does not necessary track context rules for every token (currently only for: IDENTIFIER)
    public TokenWithCtx withRules(Stack<Integer> rules) {
        return new TokenWithCtx(this.index, rules);
    }
}
