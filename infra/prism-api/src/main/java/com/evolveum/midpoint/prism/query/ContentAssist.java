package com.evolveum.midpoint.prism.query;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;

import java.util.List;

public record ContentAssist(List<AxiomQueryError> validate, List<Suggestion> autocomplete) {

    public enum Options {
        VALIDATE,
        AUTOCOMPLETE
    }
}
