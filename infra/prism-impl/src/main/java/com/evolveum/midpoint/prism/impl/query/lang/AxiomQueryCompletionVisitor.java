package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.*;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParserBaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Created by Dominik.
 */
public class AxiomQueryCompletionVisitor extends AxiomQueryParserBaseVisitor<Object> {
    private ParseTree lastNode;
    private String lastType = null;

    @Override
    public Object visitTerminal(TerminalNode node) {
        // set lastNode if visiting a terminal node
        lastNode = node;
        return null;
    }

    @Override
    public Object visitChildren(RuleNode node) {
        // set lastNode if visiting a rule node
        lastNode = node;
        return super.visitChildren(node);
    }

    @Override
    public Object visitItemFilter(ItemFilterContext ctx) {
        for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
            if (ctx.getChild(i).getText().equals(FilterNames.TYPE.getLocalPart())) lastType = ctx.getChild(i + 2).getText();
            if (ctx.getChild(i).getText().equals(FilterNames.META_TYPE)) lastType = ctx.getChild(i + 4).getText();
        }

        return super.visitItemFilter(ctx);
    }

    public ParseTree getLastNode() {
        return lastNode;
    }

    public String getLastType() {
        return lastType;
    }
}
