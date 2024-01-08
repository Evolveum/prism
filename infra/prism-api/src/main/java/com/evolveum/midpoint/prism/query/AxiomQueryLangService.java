package com.evolveum.midpoint.prism.query;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;

import java.util.List;
import java.util.Map;

public interface AxiomQueryLangService {

    List<AxiomQueryError> validate(String query);

    Map<String, String> queryCompletion(String query);

}
