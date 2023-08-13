package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.ItemFilterContext;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.util.exception.SchemaException;

interface DefinitionOverrideContext {

     boolean isApplicable(QueryParsingContext.Local context, ItemFilterContext itemFilter);

    void process(QueryParsingContext.Local context, ItemFilterContext itemFilter) throws SchemaException;
    boolean shouldRemove(ItemFilterContext itemFilter);

     ObjectFilter toFilter();

     boolean isComplete();

    void apply(QueryParsingContext.Local context);
    boolean addsFilter();
}
