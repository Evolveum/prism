package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.misc.IntervalSet;

/**
 * Created by Dominik.
 */
public record RecognitionsSet(int startState, IntervalSet recognizedTokens) {
}
