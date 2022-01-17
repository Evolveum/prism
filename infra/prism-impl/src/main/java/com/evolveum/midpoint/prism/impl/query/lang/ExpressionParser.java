package com.evolveum.midpoint.prism.impl.query.lang;

import java.util.Map;

import com.evolveum.midpoint.prism.ExpressionWrapper;

public interface ExpressionParser {

    ExpressionWrapper parseScript(Map<String, String> namespaceContext, String language, String script);

}
