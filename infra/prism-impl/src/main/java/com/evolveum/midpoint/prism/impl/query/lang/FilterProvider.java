package com.evolveum.midpoint.prism.impl.query.lang;

import static com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.*;
import static com.evolveum.midpoint.prism.impl.query.lang.Filter.*;

import java.util.HashMap;
import java.util.Map;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.util.DOMUtil;

/**
 * Created by Dominik.
 */
public class FilterProvider {

    public static Map<String, String> findFilterByItemDefinition(Definition itemDefinition, int ruleIndex) {
        Map<String, String> suggestions = new HashMap<>();

        if (ruleIndex == RULE_filterName || ruleIndex == RULE_filterNameAlias) {
            if (itemDefinition instanceof PrismPropertyDefinition) {
                addFilterSuggestion(Name.EQUAL, suggestions);
                addFilterSuggestion(Name.LESS, suggestions);
                addFilterSuggestion(Name.GREATER, suggestions);
                addFilterSuggestion(Name.LESS_OR_EQUAL, suggestions);
                addFilterSuggestion(Name.GREATER_OR_EQUAL, suggestions);
                addFilterSuggestion(Name.NOT_EQUAL, suggestions);

                addFilterSuggestion(Name.EXISTS, suggestions);
                addFilterSuggestion(Name.LEVENSHTEIN, suggestions);
                addFilterSuggestion(Name.SIMILARITY, suggestions);
                addFilterSuggestion(Name.ANY_IN, suggestions);

                if (itemDefinition.getTypeName().equals(DOMUtil.XSD_STRING)
                        || itemDefinition.getTypeName().equals(PrismConstants.POLYSTRING_TYPE_QNAME)) {
                    addFilterSuggestion(Name.STARTS_WITH, suggestions);
                    addFilterSuggestion(Name.ENDS_WITH, suggestions);
                    addFilterSuggestion(Name.CONTAINS, suggestions);
                    addFilterSuggestion(Name.FULL_TEXT, suggestions);
                }

                if (itemDefinition.getTypeName().equals(PrismConstants.POLYSTRING_TYPE_QNAME)) {
                    addFilterSuggestion(Name.MATCHES, suggestions);
                }
            }

            if (itemDefinition instanceof PrismContainerDefinition
                    || itemDefinition instanceof PrismReferenceDefinition
                    || itemDefinition instanceof ComplexTypeDefinition) {
                addFilterSuggestion(Name.OWNED_BY, suggestions);
                addFilterSuggestion(Name.MATCHES, suggestions);
                addFilterSuggestion(Name.REFERENCED_BY, suggestions);
                addFilterSuggestion(Name.IN_ORG, suggestions);
                addFilterSuggestion(Name.IN_OID, suggestions);
                addFilterSuggestion(Name.IS_ROOT, suggestions);
                addFilterSuggestion(Name.TYPE, suggestions);
                addFilterSuggestion(Name.OWNED_BY_OID, suggestions);
            }
        }

        if (ruleIndex == RULE_subfilterOrValue) {
            addFilterSuggestion(Name.AND, suggestions);
            addFilterSuggestion(Name.OR, suggestions);
            addFilterSuggestion(Name.NOT, suggestions);
        }

        if (ruleIndex == RULE_itemPathComponent) {
            suggestions.put(Infra.TYPE.getName(), null);
            suggestions.put(Infra.PATH.getName(), null);
            suggestions.put(Infra.RELATION.getName(), null);
            suggestions.put(Infra.METADATA.getName(), null);
        }

        if (ruleIndex == RULE_negation) {
            addFilterSuggestion(Name.NOT, suggestions);
        }

        return suggestions;
    }

    private static void addFilterSuggestion(Filter.Name filter, Map<String, String> suggestions) {
        suggestions.put(
                filter.getLocalPart(),
                Filter.aliasFor(filter).map(Alias::getName).orElse(null));
    }
}
