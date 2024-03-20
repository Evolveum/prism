package com.evolveum.midpoint.prism.query;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;

import com.evolveum.midpoint.prism.ItemDefinition;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface AxiomQueryLangService {

    @Deprecated
    default List<AxiomQueryError> validate(String query) {
        return validate(null, query);
    }


    @Deprecated
    default Map<String, String> queryCompletion(String query) {
        return queryCompletion(null, query);
    }

    List<AxiomQueryError> validate(@Nullable ItemDefinition<?> rootItem, String query);

    // FIXME: Return value should be more structured, e.g item definition, filter definition (with help text?)
    //        type definition?
    //        Something like suggested text, documentation for suggestion?
    Map<String, String> queryCompletion(@Nullable ItemDefinition<?> rootItem, String query);

}
