package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.util.DOMUtil;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;

import static com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.*;
import static com.evolveum.midpoint.prism.impl.query.lang.FilterNames.*;

/**
 * Created by Dominik.
 */
public class FilterNamesProvider {
    public static List<String> findFilterNamesByItemDefinition(ItemDefinition<?> itemDefinition, ParserRuleContext ruleContext) {

        List<String> suggestions = new ArrayList<>();

        if (ruleContext instanceof FilterNameContext || ruleContext instanceof  FilterNameAliasContext) {
            // || itemDefinition instanceof PrismObjectDefinition<?>
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

                suggestions.add(EXISTS.getLocalPart());
                suggestions.add(LEVENSHTEIN.getLocalPart());
                suggestions.add(SIMILARITY.getLocalPart());
                suggestions.add(IN_OID.getLocalPart());
                suggestions.add(OWNED_BY_OID.getLocalPart());
                suggestions.add(IN_ORG.getLocalPart());
                suggestions.add(IS_ROOT.getLocalPart());
                suggestions.add(OWNED_BY.getLocalPart());
                suggestions.add(ANY_IN.getLocalPart());
                suggestions.add(TYPE.getLocalPart());

                if (itemDefinition.getTypeName().equals(DOMUtil.XSD_STRING) || itemDefinition.getTypeName().equals(PrismConstants.POLYSTRING_TYPE_QNAME)) {
                    suggestions.add(STARTS_WITH.getLocalPart());
                    suggestions.add(ENDS_WITH.getLocalPart());
                    suggestions.add(CONTAINS.getLocalPart());
                    suggestions.add(FULL_TEXT.getLocalPart());
                }

                // TODO AssignmentType
                if (itemDefinition.getTypeName().equals(PrismConstants.POLYSTRING_TYPE_QNAME)) {
                    suggestions.add(MATCHES.getLocalPart());
                }
            } else if (itemDefinition instanceof PrismReferenceDefinition) {
                suggestions.add(REFERENCED_BY.getLocalPart());
            } else if (itemDefinition instanceof PrismContainerDefinition<?>) {
                suggestions.add(MATCHES.getLocalPart());
            }
        }

        if (ruleContext instanceof SubfilterOrValueContext) {
            suggestions.add(AND.getLocalPart());
            suggestions.add(OR.getLocalPart());
            suggestions.add(NOT.getLocalPart());
        }

        if (ruleContext instanceof ItemPathComponentContext) {
            suggestions.add(META_TYPE);
            suggestions.add(META_PATH);
            suggestions.add(META_RELATION);
        }

        return suggestions;
    }
}
