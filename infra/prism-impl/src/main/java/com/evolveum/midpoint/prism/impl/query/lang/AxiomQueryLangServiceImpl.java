package com.evolveum.midpoint.prism.impl.query.lang;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.axiom.lang.antlr.AxiomQuerySource;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.AxiomQueryLangService;

/**
 * Created by Dominik.
 */
public class AxiomQueryLangServiceImpl implements AxiomQueryLangService {

    private final PrismContext prismContext;

    public AxiomQueryLangServiceImpl(@NotNull PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    public List<AxiomQueryError> validate(@Nullable ItemDefinition<?> rootItem, String query) {
        AxiomQueryValidationVisitor visitor = new AxiomQueryValidationVisitor(rootItem, this.prismContext);
        AxiomQuerySource source = AxiomQuerySource.from(query);
        source.root().accept(visitor);
        visitor.errorList.addAll(source.getSyntaxError());
        return visitor.errorList;
    }

    public Map<String, String> queryCompletion(@Nullable ItemDefinition<?> rootItem, String query) {
        if (query.isEmpty()) {
            query = " ";
        }

        AxiomQuerySource source = AxiomQuerySource.from(query);
        AxiomQueryCompletionVisitor visitor = new AxiomQueryCompletionVisitor(rootItem, this.prismContext);
        visitor.visit(source.root());
        return visitor.generateSuggestion();
    }
}
