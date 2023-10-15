package com.evolveum.midpoint.prism.query;

import java.util.List;

public interface AxiomQueryCompletion {
    /**
     * Generate code continuation suggestions for query provided
     */
    List<String> generateSuggestions(String query);
}
