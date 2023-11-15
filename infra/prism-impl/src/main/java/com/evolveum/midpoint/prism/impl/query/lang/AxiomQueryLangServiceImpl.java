package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.axiom.lang.antlr.AxiomQuerySource;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.marshaller.ItemPathHolder;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.AxiomQueryLangService;

import com.evolveum.midpoint.prism.schema.SchemaRegistry;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.evolveum.midpoint.prism.impl.query.lang.FilterNames.EQUAL;

/**
 * Created by Dominik.
 */
public class AxiomQueryLangServiceImpl implements AxiomQueryLangService {
    private PrismContext prismContext;
    private Class userType;

    public AxiomQueryLangServiceImpl(PrismContext prismContext, Class userType) {
        this.prismContext = prismContext;
        this.userType = userType;
    }

    public List<AxiomQueryError> validate(String query) {
        AxiomQueryValidationVisitor axiomQueryValidationVisitor = new AxiomQueryValidationVisitor(this.prismContext, userType);
        AxiomQuerySource.from(query).root().accept(axiomQueryValidationVisitor);
        return axiomQueryValidationVisitor.errorList;
    }

    public List<String> queryCompletion(String query) {
        AxiomQuerySource axiomQuerySource = AxiomQuerySource.from(query);
        AxiomQueryCompletionVisitor axiomQueryCompletionVisitor = new AxiomQueryCompletionVisitor();
        axiomQueryCompletionVisitor.visit(axiomQuerySource.root());

        if (findItemFilter(axiomQueryCompletionVisitor.getLastNode()) instanceof AxiomQueryParser.RootContext) {
            // TODO add the case if there is a node with one child
        } else if(findItemFilter(axiomQueryCompletionVisitor.getLastNode()) instanceof AxiomQueryParser.ItemFilterContext) {
            generateSuggestion(findItemFilter(axiomQueryCompletionVisitor.getLastNode()));
        }

        return null;
    }

    private List<String> generateSuggestion(@NotNull RuleNode ruleNode) {
        List<String> suggestions = new ArrayList<>();
        int childCount = ruleNode.getChildCount();
        SchemaRegistry schemaRegistry = PrismContext.get().getSchemaRegistry();
        PrismObjectDefinition<?> prismObjectDefinition = schemaRegistry.findObjectDefinitionByCompileTimeClass(userType);

        for(int i = 0; i < childCount; ++i) {
            if (ruleNode.getChild(i) instanceof AxiomQueryParser.DescendantPathContext ) {
                ItemPath itemPath = ItemPathHolder.parseFromString(ruleNode.getChild(i).getText());
                prismObjectDefinition.findItemDefinition(itemPath, ItemDefinition.class);
                for (ItemDefinition<?> itemDefinition: prismObjectDefinition.getDefinitions()) {
                    suggestions.add(itemDefinition.getItemName().getLocalPart());
                }
            } else if(ruleNode.getChild(i) instanceof AxiomQueryParser.SelfPathContext) {
                if (ruleNode.getChild(i).getText().equals(".")) {

                }
            } else if(ruleNode.getChild(i) instanceof AxiomQueryParser.FilterNameContext ruleContext) {
                suggestions = FilterNamesProvider.findFilterNamesByItemDefinition(prismObjectDefinition, ruleContext);
            } else if(ruleNode.getChild(i) instanceof AxiomQueryParser.FilterNameAliasContext ruleContext) {
                suggestions = FilterNamesProvider.findFilterNamesByItemDefinition(prismObjectDefinition, ruleContext);
            } else if(ruleNode.getChild(i) instanceof AxiomQueryParser.SubfilterOrValueContext) {
                if (ruleNode.getChild(i - 2).getText().equals(FilterNames.NAME_TO_ALIAS.get(EQUAL)) && ruleNode.getChild(i - 4).getText().equals(FilterNames.META_TYPE)) {
                    suggestions.add("UserType");
                }
                if (ruleNode.getChild(i - 2).getText().equals(FilterNames.TYPE.getLocalPart()) && ruleNode.getChild(i - 4).getText().equals(".")) {
                    suggestions.add("UserType");
                }
                if (ruleNode.getChild(i - 2).getText().equals(FilterNames.NAME_TO_ALIAS.get(EQUAL)) && ruleNode.getChild(i - 4).getText().equals(FilterNames.META_PATH)) {
                    for (ItemDefinition<?> itemDefinition: prismObjectDefinition.getDefinitions()) {
                        suggestions.add(itemDefinition.getItemName().getLocalPart());
                    }
                }
            }
        }

        return suggestions;
    }


    private RuleNode findItemFilter(@NotNull ParseTree parseTreeTerminal) {
        RuleNode ruleNode = (RuleNode) parseTreeTerminal.getParent();

        while (
                ruleNode.getRuleContext().getRuleIndex() != AxiomQueryParser.RULE_root  &&
                ruleNode.getRuleContext().getRuleIndex() != AxiomQueryParser.RULE_itemFilter
        ) {
            ruleNode = (RuleNode) ruleNode.getParent();
        }

        return ruleNode;
    }
}
