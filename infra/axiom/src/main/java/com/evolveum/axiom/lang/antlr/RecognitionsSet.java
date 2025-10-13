/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.misc.IntervalSet;

/**
 * Created by Dominik.
 */
public record RecognitionsSet(int startState, IntervalSet recognizedTokens) {
}
