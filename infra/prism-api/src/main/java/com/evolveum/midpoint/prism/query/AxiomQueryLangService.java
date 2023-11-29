package com.evolveum.midpoint.prism.query;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;

import java.util.List;

public interface AxiomQueryLangService {

    List<AxiomQueryError> validate(String query);

    List<String> queryCompletion(String query);

}
