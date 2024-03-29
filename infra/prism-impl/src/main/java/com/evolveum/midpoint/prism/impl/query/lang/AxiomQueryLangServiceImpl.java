package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.axiom.lang.antlr.AxiomQuerySource;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.AxiomQueryLangService;

import org.jetbrains.annotations.Nullable;

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

    public List<AxiomQueryError> validate(@Nullable ItemDefinition<?> rootItem, String query) {
        AxiomQueryValidationVisitor axiomQueryValidationVisitor = new AxiomQueryValidationVisitor(rootItem, this.prismContext);
        AxiomQuerySource axiomQuerySource = AxiomQuerySource.from(query);
        axiomQuerySource.root().accept(axiomQueryValidationVisitor);
        axiomQueryValidationVisitor.errorList.addAll(axiomQuerySource.getSyntaxError());
        return axiomQueryValidationVisitor.errorList;
    }

    public Map<String, String> queryCompletion(@Nullable ItemDefinition<?> rootItem, String query) {

        if (query.isEmpty()) query = " ";

        AxiomQuerySource axiomQuerySource = AxiomQuerySource.from(query);
        AxiomQueryCompletionVisitor axiomQueryCompletionVisitor = new AxiomQueryCompletionVisitor(rootItem, this.prismContext);
        axiomQueryCompletionVisitor.visit(axiomQuerySource.root());
        return axiomQueryCompletionVisitor.generateSuggestion();
    }
}
