package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.AxiomQuerySource;

import com.evolveum.midpoint.prism.query.ContentAssist;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.AxiomQueryContentAssist;

/**
 * Created by Dominik.
 */
public class AxiomQueryContentAssistImpl implements AxiomQueryContentAssist {

    private final PrismContext prismContext;

    public AxiomQueryContentAssistImpl(@NotNull PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    @Override
    public ContentAssist process(@Nullable ItemDefinition<?> rootItem, String query) {
        AxiomQuerySource source = AxiomQuerySource.from(query);
        AxiomQueryContentAssistantVisitor visitor = new AxiomQueryContentAssistantVisitor(prismContext, rootItem);
        source.root().accept(visitor);
        var errors = visitor.getErrorList();
        errors.addAll(source.syntaxErrors());
        return new ContentAssist(errors);
    }

    @Override
    public ContentAssist process(@Nullable ItemDefinition<?> rootItem, String query, int positionCursor) {
        AxiomQuerySource source = AxiomQuerySource.from(query);
        AxiomQueryContentAssistantVisitor visitor = new AxiomQueryContentAssistantVisitor(prismContext, rootItem, source.atn(), positionCursor);
        source.root().accept(visitor);
        var errors = visitor.getErrorList();
        errors.addAll(source.syntaxErrors());
        return new ContentAssist(errors, visitor.generateSuggestions());
    }
}
