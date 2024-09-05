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
    public ContentAssist process(@Nullable ItemDefinition<?> rootItem, String query, int positionCursor) {
        AxiomQuerySource source = AxiomQuerySource.from(query);
        AxiomQueryContentAssistantVisitor visitor = new AxiomQueryContentAssistantVisitor(prismContext, rootItem, source.getParser(), positionCursor, source.getRecognitionsSet());
        source.root().accept(visitor);
        // TODO visitor.errorList.addAll(source.getSyntaxError()); -> syntaxError
        return new ContentAssist(visitor.errorList, visitor.generateSuggestions());
    }
}
