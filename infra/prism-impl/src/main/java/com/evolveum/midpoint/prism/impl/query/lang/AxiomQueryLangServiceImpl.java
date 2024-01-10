package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.axiom.lang.antlr.AxiomQuerySource;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.AxiomQueryLangService;

import java.util.List;
import java.util.Map;

/**
 * Created by Dominik.
 */
public class AxiomQueryLangServiceImpl implements AxiomQueryLangService {
    private PrismContext prismContext;

    public AxiomQueryLangServiceImpl(PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    public List<AxiomQueryError> validate(String query) {
        AxiomQueryValidationVisitor axiomQueryValidationVisitor = new AxiomQueryValidationVisitor(this.prismContext);
        AxiomQuerySource axiomQuerySource = AxiomQuerySource.from(query);
        axiomQuerySource.root().accept(axiomQueryValidationVisitor);
        axiomQueryValidationVisitor.errorList.addAll(axiomQuerySource.getSyntaxError());
        return axiomQueryValidationVisitor.errorList;
    }

    public Map<String, String> queryCompletion(String query) {

        if (query.isEmpty()) query = " ";

        AxiomQuerySource axiomQuerySource = AxiomQuerySource.from(query);
        AxiomQueryCompletionVisitor axiomQueryCompletionVisitor = new AxiomQueryCompletionVisitor(this.prismContext);
        axiomQueryCompletionVisitor.visit(axiomQuerySource.root());
        return axiomQueryCompletionVisitor.generateSuggestion();
    }
}
