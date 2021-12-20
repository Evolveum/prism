package com.evolveum.midpoint.prism.impl.query.lang;

import java.util.Map;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.ExpressionContext;
import com.evolveum.midpoint.prism.ExpressionWrapper;

public interface ExpressionParser {

    ExpressionWrapper parse(Map<String, String> namespaceContext, ExpressionContext expression);
}
