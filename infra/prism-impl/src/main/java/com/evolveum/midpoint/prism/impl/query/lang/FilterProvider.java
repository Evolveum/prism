package com.evolveum.midpoint.prism.impl.query.lang;

import static com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.*;
import static com.evolveum.midpoint.prism.impl.query.lang.Filter.Name.*;

import java.util.HashMap;
import java.util.Map;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.util.DOMUtil;

/**
 * Created by Dominik.
 */
public class FilterProvider {

    public static Map<String, String> findFilterByItemDefinition(ItemDefinition<?> itemDefinition, int ruleIndex) {
        Map<String, String> suggestions = new HashMap<>();

        if (ruleIndex == RULE_filterName || ruleIndex == RULE_filterNameAlias) {
            if (itemDefinition instanceof PrismPropertyDefinition) {
                addFilterSuggestion(EQUAL, suggestions);
                addFilterSuggestion(LESS, suggestions);
                addFilterSuggestion(GREATER, suggestions);
                addFilterSuggestion(LESS_OR_EQUAL, suggestions);
                addFilterSuggestion(GREATER_OR_EQUAL, suggestions);
                addFilterSuggestion(NOT_EQUAL, suggestions);

                addFilterSuggestion(EXISTS, suggestions);
                addFilterSuggestion(LEVENSHTEIN, suggestions);
                addFilterSuggestion(SIMILARITY, suggestions);
                addFilterSuggestion(OWNED_BY_OID, suggestions);
                addFilterSuggestion(ANY_IN, suggestions);
                addFilterSuggestion(TYPE, suggestions);

                if (itemDefinition.getTypeName().equals(DOMUtil.XSD_STRING)
                        || itemDefinition.getTypeName().equals(PrismConstants.POLYSTRING_TYPE_QNAME)) {
                    addFilterSuggestion(STARTS_WITH, suggestions);
                    addFilterSuggestion(ENDS_WITH, suggestions);
                    addFilterSuggestion(CONTAINS, suggestions);
                    addFilterSuggestion(FULL_TEXT, suggestions);
                }

                if (itemDefinition.getTypeName().equals(PrismConstants.POLYSTRING_TYPE_QNAME)) {
                    addFilterSuggestion(MATCHES, suggestions);
                }
            }

            if (itemDefinition instanceof PrismContainerDefinition
                    || itemDefinition instanceof PrismReferenceDefinition) {
                addFilterSuggestion(MATCHES, suggestions);
                addFilterSuggestion(REFERENCED_BY, suggestions);
                addFilterSuggestion(OWNED_BY, suggestions);
                addFilterSuggestion(IN_ORG, suggestions);
                addFilterSuggestion(IN_OID, suggestions);
                addFilterSuggestion(IS_ROOT, suggestions);
            }
        }

        if (ruleIndex == RULE_subfilterOrValue) {
            addFilterSuggestion(AND, suggestions);
            addFilterSuggestion(OR, suggestions);
            addFilterSuggestion(NOT, suggestions);
        }

        if (ruleIndex == RULE_itemPathComponent) {
            suggestions.put(Filter.Meta.TYPE.getName(), null);
            suggestions.put(Filter.Meta.PATH.getName(), null);
            suggestions.put(Filter.Meta.RELATION.getName(), null);
        }

        if (ruleIndex == RULE_negation) {
            addFilterSuggestion(NOT, suggestions);
        }

        return suggestions;
    }

    private static void addFilterSuggestion(Filter.Name filter, Map<String, String> suggestions) {
        suggestions.put(
                filter.getLocalPart(),
                Filter.aliasFor(filter).map(a -> a != null ? a.getName() : null).orElse(null));
    }
}
