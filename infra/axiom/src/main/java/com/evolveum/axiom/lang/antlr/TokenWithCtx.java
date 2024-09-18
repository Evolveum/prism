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
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TokenWithCtx token)) {
            return false;
        }
        return token.index == this.index && token.rules.equals(this.rules);
    }
}
