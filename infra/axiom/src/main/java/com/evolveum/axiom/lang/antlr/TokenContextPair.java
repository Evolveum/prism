package com.evolveum.axiom.lang.antlr;

import java.util.Stack;

/**
 * Created by Dominik.
 */
public record TokenContextPair(int token, Stack<Integer> ruleCtxIndex) {
}
