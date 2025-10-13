/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;

import java.util.List;

public record ContentAssist(List<AxiomQueryError> validate, List<Suggestion> autocomplete) {

    public ContentAssist(List<AxiomQueryError> validate) {
        this(validate, null);
    }
}
