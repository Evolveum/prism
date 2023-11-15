package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.ItemDefinition;

import java.util.List;

public interface AxiomQueryLangService {

    List<?> validate(String query);

    List<String> queryCompletion(String query);
}
