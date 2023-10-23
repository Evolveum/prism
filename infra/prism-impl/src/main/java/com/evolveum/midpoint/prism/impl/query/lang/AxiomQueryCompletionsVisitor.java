package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParserBaseVisitor;

import com.evolveum.midpoint.prism.PrismContext;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.evolveum.midpoint.prism.impl.query.lang.FilterNames.*;

/**
 * Created by Dominik.
 */
public class AxiomQueryCompletionsVisitor extends AxiomQueryParserBaseVisitor<Object> {
    /**
     * Override the default behavior for all visit methods. This will only return last node in Parser Tree
     */
    @Override
    public Object visitChildren(RuleNode node) {
        Object result = this.defaultResult();
        int childCount = node.getChildCount();

        for(int i = 0; i < childCount && this.shouldVisitNextChild(node, result); ++i) {
            ParseTree c = node.getChild(i);
            // skip SEP token
            if (!c.getText().equals(" ")) {
                Object childResult = c.accept(this);
                result = this.aggregateResult(result, childResult);
            }
        }

        if (node.getRuleContext().getRuleIndex() == AxiomQueryParser.RULE_itemFilter) {
            try {
                validateItemFilter(node);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    private void validateItemFilter(@NotNull ParseTree itemFilter) throws ClassNotFoundException {
        int childCount = itemFilter.getChildCount();
        PrismContext prismContext = PrismContext.get();


        for(int i = 0; i < childCount; ++i) {
            ParseTree itemFilterChild = itemFilter.getChild(i);
            if (!itemFilterChild.getText().equals(" ")) {
                RuleNode nodeItemFilter = (RuleNode) itemFilterChild;
                // find Type class by type or @type filterNames
                if (
                    (nodeItemFilter.getRuleContext().getRuleIndex() == AxiomQueryParser.RULE_path && nodeItemFilter.getText().equals(META_TYPE)) ||
                    (nodeItemFilter.getRuleContext().getRuleIndex() == AxiomQueryParser.RULE_filterName && nodeItemFilter.getText().equals(TYPE.getLocalPart()))
                ) {
                    Class typeClass = Class.forName(Objects.requireNonNull(getLastNode(nodeItemFilter)).getText());
                    prismContext.getSchemaRegistry().findObjectDefinitionByCompileTimeClass(typeClass);
                }
                // find ItemPath
                if (nodeItemFilter.getRuleContext().getRuleIndex() == AxiomQueryParser.RULE_path) {

                }
            }
        }
    }

    private ParseTree getLastNode(RuleNode currentPosition) {
        while (currentPosition.getRuleContext().getRuleIndex() != AxiomQueryParser.RULE_itemFilter) {
            currentPosition = (RuleNode) currentPosition.getParent();
            return currentPosition.getChild(currentPosition.getChildCount() - 1);
        }
        return null;
    }

    private ParseTree getFirstNode(RuleNode currentPosition) {
        while (currentPosition.getRuleContext().getRuleIndex() != AxiomQueryParser.RULE_itemFilter) {
            currentPosition = (RuleNode) currentPosition.getParent();
            return currentPosition.getChild(0);
        }
        return null;
    }
}
