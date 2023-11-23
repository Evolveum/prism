package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.axiom.lang.antlr.AxiomQuerySource;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.marshaller.ItemPathHolder;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.AxiomQueryLangService;

import com.evolveum.midpoint.prism.schema.SchemaDescription;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Dominik.
 */
public class AxiomQueryLangServiceImpl implements AxiomQueryLangService {
    private PrismContext prismContext;

    public AxiomQueryLangServiceImpl(PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    public List<AxiomQueryError> validate(String query) {
        AxiomQueryValidationVisitor axiomQueryValidationVisitor = new AxiomQueryValidationVisitor(this.prismContext);
        AxiomQuerySource axiomQuerySource = AxiomQuerySource.from(query);
        axiomQuerySource.root().accept(axiomQueryValidationVisitor);
        return axiomQueryValidationVisitor.errorList;
    }

    public List<String> queryCompletion(String query) {
        AxiomQuerySource axiomQuerySource = AxiomQuerySource.from(query);
        AxiomQueryCompletionVisitor axiomQueryCompletionVisitor = new AxiomQueryCompletionVisitor();
        axiomQueryCompletionVisitor.visit(axiomQuerySource.root());

        if (findItemFilter(axiomQueryCompletionVisitor.getLastNode()) instanceof AxiomQueryParser.RootContext) {
            Token token = (Token) axiomQuerySource.getSyntaxError().get(0).getOffendingSymbol();
            RuleNode ruleNode1 = (RuleNode) token;
            generateSuggestion(axiomQueryCompletionVisitor.getLastNode(), axiomQueryCompletionVisitor.getLastType());
        } else if(findItemFilter(axiomQueryCompletionVisitor.getLastNode()) instanceof AxiomQueryParser.ItemFilterContext) {
            // if command is uncompleted
            generateSuggestion(findItemFilter(axiomQueryCompletionVisitor.getLastNode()), axiomQueryCompletionVisitor.getLastType());
        }

        return null;
    }

    private List<String> generateSuggestion(@NotNull ParseTree ruleNode, @Nullable ParseTree lastType) {
        List<String> suggestions = new ArrayList<>();
        int childCount = ruleNode.getChildCount();
        SchemaRegistry schemaRegistry = this.prismContext.getSchemaRegistry();
        PrismObjectDefinition<?> prismObjectDefinition = null;


        if (lastType != null) {
            TypeDefinition typeDefinition = schemaRegistry.findTypeDefinitionByType(new QName(lastType.getText()));
        }

        for(int i = 0; i < childCount; ++i) {
            if (ruleNode.getChild(i) instanceof AxiomQueryParser.DescendantPathContext && prismObjectDefinition != null ) {
                ItemPath itemPath = ItemPathHolder.parseFromString(ruleNode.getChild(i).getText());
                prismObjectDefinition.findItemDefinition(itemPath, ItemDefinition.class);
                for (ItemDefinition<?> itemDefinition: prismObjectDefinition.getDefinitions()) {
                    suggestions.add(itemDefinition.getItemName().getLocalPart());
                }
            } else if(ruleNode.getChild(i) instanceof AxiomQueryParser.FilterNameContext ruleContext && prismObjectDefinition != null) {
                suggestions = FilterNamesProvider.findFilterNamesByItemDefinition(prismObjectDefinition, ruleContext);
            } else if(ruleNode.getChild(i) instanceof AxiomQueryParser.FilterNameAliasContext ruleContext && prismObjectDefinition != null) {
                suggestions = FilterNamesProvider.findFilterNamesByItemDefinition(prismObjectDefinition, ruleContext);
            } else if(ruleNode.getChild(i) instanceof AxiomQueryParser.SubfilterOrValueContext) {
                if (ruleNode.getChild(i - 2).getText().equals(FilterNames.NAME_TO_ALIAS.get(FilterNames.EQUAL)) && ruleNode.getChild(i - 4).getText().equals(FilterNames.META_TYPE)) {
                    if (ruleNode.getChild(i) != null) {
                        prismObjectDefinition = schemaRegistry.findObjectDefinitionByCompileTimeClass(schemaRegistry.findObjectDefinitionByType(new QName(ruleNode.getChild(i).getText())).getTypeClass());
                    } else {
                        suggestions.addAll(getObjectTypeList());
                    }
                }
                if (ruleNode.getChild(i - 2).getText().equals(FilterNames.TYPE.getLocalPart()) && ruleNode.getChild(i - 4).getText().equals(".")) {
                    if (ruleNode.getChild(i) != null) {
                        prismObjectDefinition = schemaRegistry.findObjectDefinitionByCompileTimeClass(schemaRegistry.findObjectDefinitionByType(new QName(ruleNode.getChild(i).getText())).getTypeClass());
                    } else {
                        suggestions.addAll(getObjectTypeList());
                    }
                }
                if (ruleNode.getChild(i - 2).getText().equals(FilterNames.NAME_TO_ALIAS.get(FilterNames.EQUAL)) && ruleNode.getChild(i - 4).getText().equals(FilterNames.META_PATH) && prismObjectDefinition != null) {
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

    private List<String> getObjectTypeList() {
        List<String> objectTypes = new ArrayList<>();

        for (SchemaDescription schemaDescription : this.prismContext.getSchemaRegistry().getSchemaDescriptions()) {
            for (Definition definition : schemaDescription.getSchema().getDefinitions()) {
                objectTypes.add(definition.getTypeName().getLocalPart());
            }
        }

        return objectTypes;
    }
}
