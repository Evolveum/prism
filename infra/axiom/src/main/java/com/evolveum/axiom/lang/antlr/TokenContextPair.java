package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;

/**
 * Created by Dominik.
 */
public record TokenContextPair(Token token, int ruleCtxIndex) {
}
