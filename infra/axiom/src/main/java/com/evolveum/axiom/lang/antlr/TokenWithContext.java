package com.evolveum.axiom.lang.antlr;

import java.util.Stack;

/**
 * Created by Dominik.
 *
 * Record represent token with context rules.
 *  Index is index of token.
 *  Rules is stack of related rules with token.
 */
public record TokenWithContext(int index, Stack<Integer> rules) {

    // Does not necessary track context rules for every token (currently only for: IDENTIFIER)
    public TokenWithContext withRules(Stack<Integer> rules) {
        return new TokenWithContext(this.index, rules);
    }
}
