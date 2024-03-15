package com.evolveum.midpoint.prism.query;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.ItemDefinition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

public interface AxiomQueryLangService {

    List<AxiomQueryError> validate(@Nullable ItemDefinition definition,  String query);

    Map<String, String> queryCompletion(@Nullable ItemDefinition definition, String query);

}
