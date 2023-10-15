package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.AxiomQuerySource;
import com.evolveum.midpoint.prism.query.AxiomQueryCompletion;

import org.antlr.v4.runtime.tree.RuleNode;
import static com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.*;
import static com.evolveum.midpoint.prism.impl.query.lang.FilterNames.*;

import java.util.List;

/**
 * Created by Dominik.
 */
public class AxiomQueryCompletionProvider implements AxiomQueryCompletion {
    private final AxiomQueryCompletionsVisitor axiomVisitor = new AxiomQueryCompletionsVisitor();

    public AxiomQueryCompletionProvider() {
    }

    public List<String> generateSuggestions(String query) {

        List<String> suggestions = new java.util.ArrayList<>();

        if (query.equals("") || query.equals(" ")) {
            return suggestions;
        }

        RootContext axiomQuerySource = AxiomQuerySource.from(query).root();
        RuleNode node = (RuleNode) axiomVisitor.visitChildren(axiomQuerySource);

        if (node.getRuleContext().getRuleIndex() == RULE_itemFilter) {
            suggestions.add(EQUAL.getLocalPart());
            suggestions.add(LESS.getLocalPart());
            suggestions.add(GREATER.getLocalPart());
            suggestions.add(LESS_OR_EQUAL.getLocalPart());
            suggestions.add(GREATER_OR_EQUAL.getLocalPart());
            suggestions.add(CONTAINS.getLocalPart());
            suggestions.add(STARTS_WITH.getLocalPart());
            suggestions.add(ENDS_WITH.getLocalPart());
            suggestions.add(EXISTS.getLocalPart());
            suggestions.add(FULL_TEXT.getLocalPart());
            suggestions.add(IN_OID.getLocalPart());
            suggestions.add(OWNED_BY_OID.getLocalPart());
            suggestions.add(IN_ORG.getLocalPart());
            suggestions.add(IS_ROOT.getLocalPart());
            suggestions.add(NOT_EQUAL.getLocalPart());
            suggestions.add(TYPE.getLocalPart());
            suggestions.add(OWNED_BY.getLocalPart());
            suggestions.add(ANY_IN.getLocalPart());
            suggestions.add(LEVENSHTEIN.getLocalPart());
            suggestions.add(SIMILARITY.getLocalPart());
            suggestions.add(MATCHES.getLocalPart());
            suggestions.add(NOT.getLocalPart());
        }

        if (node.getRuleContext().getRuleIndex() == RULE_stringLiteral || node.getRuleContext().getRuleIndex() == RULE_literalValue) {
            suggestions.add(AND.getLocalPart());
            suggestions.add(OR.getLocalPart());
        }

        if (node.getText().equals(".") && node.getRuleContext().getRuleIndex() == RULE_path) {
            suggestions.add(REFERENCED_BY.getLocalPart());
            suggestions.add(MATCHES.getLocalPart());
        }

        if (node.getText().equals("@") && node.getRuleContext().getRuleIndex() == RULE_itemPathComponent) {
            suggestions.add(META_TYPE);
            suggestions.add(META_PATH);
            suggestions.add(META_RELATION);
        }

        return suggestions;
    }
}
