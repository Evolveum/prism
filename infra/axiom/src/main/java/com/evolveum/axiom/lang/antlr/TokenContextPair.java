package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.misc.Interval;

/**
 * Created by Dominik.
 */
public record TokenContextPair(Interval token, int ruleCtxIndex) {
}
