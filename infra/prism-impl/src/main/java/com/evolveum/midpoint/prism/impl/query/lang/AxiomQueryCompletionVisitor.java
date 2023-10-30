package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParserBaseVisitor;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

/**
 * Created by Dominik.
 */
public class AxiomQueryCompletionVisitor extends AxiomQueryParserBaseVisitor<Object> {
    /**
     * Override the default behavior for all visit methods. This will only return last node in Parser Tree
     */
    @Override
    public Object visitChildren(RuleNode node) {
        Object result = this.defaultResult();
        int index = node.getChildCount();
        ParseTree child = node.getChild(index -1);
        // skip SEP token
        if (child.getText().equals(" ")) {
            child = node.getChild(index -2);
        }

        Object childResult = child.accept(this);
        result = this.aggregateResult(result, childResult);
        // return last node
        if (result == null) {
            return node;
        }

        return result;
    }
}
