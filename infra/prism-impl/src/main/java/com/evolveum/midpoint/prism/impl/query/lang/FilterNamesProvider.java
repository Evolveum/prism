package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.util.DOMUtil;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashMap;
import java.util.Map;

import static com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.*;
import static com.evolveum.midpoint.prism.impl.query.lang.FilterNames.*;

/**
 * Created by Dominik.
 */
public class FilterNamesProvider {
    public static Map<String, String> findFilterNamesByItemDefinition(ItemDefinition<?> itemDefinition, ParserRuleContext ruleContext) {

        Map<String, String> suggestions = new HashMap<>();

        if (ruleContext instanceof FilterNameContext || ruleContext instanceof  FilterNameAliasContext || ruleContext instanceof FilterContext) {
            if (itemDefinition instanceof PrismPropertyDefinition) {
                suggestions.put(EQUAL.getLocalPart(), NAME_TO_ALIAS.get(EQUAL));
                suggestions.put(LESS.getLocalPart(), NAME_TO_ALIAS.get(LESS));
                suggestions.put(GREATER.getLocalPart(), NAME_TO_ALIAS.get(GREATER));
                suggestions.put(LESS_OR_EQUAL.getLocalPart(), NAME_TO_ALIAS.get(LESS_OR_EQUAL));
                suggestions.put(GREATER_OR_EQUAL.getLocalPart(), NAME_TO_ALIAS.get(GREATER_OR_EQUAL));
                suggestions.put(NOT_EQUAL.getLocalPart(), NAME_TO_ALIAS.get(NOT_EQUAL));

                suggestions.put(EXISTS.getLocalPart(), NAME_TO_ALIAS.get(EXISTS));
                suggestions.put(LEVENSHTEIN.getLocalPart(), NAME_TO_ALIAS.get(LEVENSHTEIN));
                suggestions.put(SIMILARITY.getLocalPart(), NAME_TO_ALIAS.get(SIMILARITY));
                suggestions.put(OWNED_BY_OID.getLocalPart(), NAME_TO_ALIAS.get(OWNED_BY_OID));
                suggestions.put(ANY_IN.getLocalPart(), NAME_TO_ALIAS.get(ANY_IN));
                suggestions.put(TYPE.getLocalPart(), NAME_TO_ALIAS.get(TYPE));

                if (itemDefinition.getTypeName().equals(DOMUtil.XSD_STRING) || itemDefinition.getTypeName().equals(PrismConstants.POLYSTRING_TYPE_QNAME)) {
                    suggestions.put(STARTS_WITH.getLocalPart(), NAME_TO_ALIAS.get(STARTS_WITH));
                    suggestions.put(ENDS_WITH.getLocalPart(), NAME_TO_ALIAS.get(ENDS_WITH));
                    suggestions.put(CONTAINS.getLocalPart(), NAME_TO_ALIAS.get(CONTAINS));
                    suggestions.put(FULL_TEXT.getLocalPart(), NAME_TO_ALIAS.get(FULL_TEXT));
                }

                if (itemDefinition.getTypeName().equals(PrismConstants.POLYSTRING_TYPE_QNAME)) {
                    suggestions.put(MATCHES.getLocalPart(), NAME_TO_ALIAS.get(MATCHES));
                }
            }

            if (itemDefinition instanceof PrismContainerDefinition || itemDefinition instanceof PrismReferenceDefinition) {
                suggestions.put(MATCHES.getLocalPart(), NAME_TO_ALIAS.get(MATCHES));
                suggestions.put(REFERENCED_BY.getLocalPart(), NAME_TO_ALIAS.get(REFERENCED_BY));
                suggestions.put(OWNED_BY.getLocalPart(), NAME_TO_ALIAS.get(OWNED_BY));
                suggestions.put(IN_ORG.getLocalPart(), NAME_TO_ALIAS.get(IN_ORG));
                suggestions.put(IN_OID.getLocalPart(), NAME_TO_ALIAS.get(IN_OID));
                suggestions.put(IS_ROOT.getLocalPart(), NAME_TO_ALIAS.get(IS_ROOT));
            }
        }

        if (ruleContext instanceof SubfilterOrValueContext) {
            suggestions.put(AND.getLocalPart(), NAME_TO_ALIAS.get(AND));
            suggestions.put(OR.getLocalPart(), NAME_TO_ALIAS.get(OR));
            suggestions.put(NOT.getLocalPart(), NAME_TO_ALIAS.get(NOT));
        }

        if (ruleContext instanceof ItemPathComponentContext) {
            suggestions.put(META_TYPE, null);
            suggestions.put(META_PATH, null);
            suggestions.put(META_RELATION, null);
        }

        return suggestions;
    }
}
