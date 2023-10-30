package com.evolveum.midpoint.prism.impl.query.lang;

import ch.qos.logback.core.pattern.parser.SimpleKeywordNode;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.PrismReferenceDefinition;

import java.util.List;

import static com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.*;
import static com.evolveum.midpoint.prism.impl.query.lang.FilterNames.*;

/**
 * Created by Dominik.
 */
public class FilterNamesProvider {
    public static List<String> findFilterNamesByItemDefinition(ItemDefinition<?> itemDefinition, Object ruleContext) {

        List<String> suggestions = new java.util.ArrayList<>();

        if (ruleContext instanceof FilterNameContext || ruleContext instanceof  FilterNameAliasContext) {

            if (itemDefinition instanceof PrismPropertyDefinition) {
                suggestions.add(EQUAL.getLocalPart());
                suggestions.add(LESS.getLocalPart());
                suggestions.add(GREATER.getLocalPart());
                suggestions.add(LESS_OR_EQUAL.getLocalPart());
                suggestions.add(GREATER_OR_EQUAL.getLocalPart());
                suggestions.add(NOT_EQUAL.getLocalPart());
                suggestions.add(NAME_TO_ALIAS.get(EQUAL));
                suggestions.add(NAME_TO_ALIAS.get(LESS));
                suggestions.add(NAME_TO_ALIAS.get(GREATER));
                suggestions.add(NAME_TO_ALIAS.get(LESS_OR_EQUAL));
                suggestions.add(NAME_TO_ALIAS.get(GREATER_OR_EQUAL));
                suggestions.add(NAME_TO_ALIAS.get(NOT_EQUAL));

                if (itemDefinition.getTypeName().getLocalPart().equals("xsd:string") || itemDefinition.getTypeName().getLocalPart().equals("types3:PolyString")) {
                    suggestions.add(STARTS_WITH.getLocalPart());
                    suggestions.add(CONTAINS.getLocalPart());
                    suggestions.add(ENDS_WITH.getLocalPart());
                }

                if (itemDefinition.getTypeName().getLocalPart().equals("string") || itemDefinition.getTypeName().getLocalPart().equals("PolyString")) {
                    suggestions.add(STARTS_WITH.getLocalPart());
                    suggestions.add(CONTAINS.getLocalPart());
                    suggestions.add(ENDS_WITH.getLocalPart());
                }

                if (itemDefinition.getTypeName().getLocalPart().equals("PolyStringType")) {
                    suggestions.add(MATCHES.getLocalPart());
                }
            } else if (itemDefinition instanceof PrismReferenceDefinition) {
                suggestions.add(REFERENCED_BY.getLocalPart());
                suggestions.add(MATCHES.getLocalPart());
            }

//            suggestions.add(EXISTS.getLocalPart());
//            suggestions.add(FULL_TEXT.getLocalPart());
//            suggestions.add(IN_OID.getLocalPart());
//            suggestions.add(OWNED_BY_OID.getLocalPart());
//            suggestions.add(IN_ORG.getLocalPart());
//            suggestions.add(IS_ROOT.getLocalPart());
//            suggestions.add(OWNED_BY.getLocalPart());
//            suggestions.add(ANY_IN.getLocalPart());
//            suggestions.add(LEVENSHTEIN.getLocalPart());
//            suggestions.add(SIMILARITY.getLocalPart());

//            suggestions.add(NOT.getLocalPart());
//            suggestions.add(TYPE.getLocalPart());

        }

        if (ruleContext instanceof SimpleKeywordNode) {
            suggestions.add(AND.getLocalPart());
            suggestions.add(OR.getLocalPart());
        }

        if (ruleContext instanceof ItemPathComponentContext) {
            suggestions.add(META_TYPE);
            suggestions.add(META_PATH);
            suggestions.add(META_RELATION);
        }

        return suggestions;
    }
}
