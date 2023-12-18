package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.*;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParserBaseVisitor;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.TypeDefinition;
import com.evolveum.midpoint.prism.impl.marshaller.ItemPathHolder;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Dominik.
 */
public class AxiomQueryCompletionVisitor extends AxiomQueryParserBaseVisitor<Object> {
    private final SchemaRegistry schemaRegistry;
    private ParseTree lastSeparator = null;
    private ParseTree lastType = null;

    public AxiomQueryCompletionVisitor(PrismContext prismContext) {
        schemaRegistry = prismContext.getSchemaRegistry();
    }

    @Override
    public Object visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == AxiomQueryParser.SEP) {
            lastSeparator = node;
        }

        return null;
    }

    @Override
    public Object visitErrorNode(ErrorNode node) {
        return super.visitErrorNode(node);
    }

    @Override
    public Object visitItemComponent(ItemComponentContext ctx) {

        if (schemaRegistry.findTypeDefinitionByType(new QName(ctx.getText())) != null) {
            lastType = ctx;
        }

        return super.visitItemComponent(ctx);
    }

    public List<String> generateSuggestion() {
        List<String> suggestions = new ArrayList<>();
        ParseTree lastNode = getLastNode();

        // generate suggestion
        if (lastNode instanceof AxiomQueryParser.ItemPathComponentContext ctx) {
            suggestions = getFilters(lastNode.getText());
            suggestions.add(FilterNames.NOT.getLocalPart());
        } else if (lastNode instanceof AxiomQueryParser.FilterNameContext ctx) {
            // TODO maybe to add suggestion for value
            // value for @type || . type
            if (findNode(ctx).getChild(0).getText().equals(FilterNames.META_TYPE) || ctx.getText().equals(FilterNames.TYPE.getLocalPart())) {
                TypeDefinition typeDefinition = schemaRegistry.findTypeDefinitionByType(new QName(lastType.getText()));
                suggestions = schemaRegistry.getObjectTypeListByClassType(typeDefinition.getCompileTimeClass());
            }
            // value for @path
            if (findNode(ctx).getChild(0).getText().equals(FilterNames.META_PATH)) {
                suggestions = getAllPath();
            }
            // value for @relation
            if (ctx.getText().equals(FilterNames.META_RELATION)) {
                // TODO add value for @relation to suggestions list
            }

            if (ctx.getText().equals(FilterNames.MATCHES.getLocalPart()) || ctx.getText().equals(FilterNames.REFERENCED_BY.getLocalPart())) {
                suggestions.add("(");
            }
        } else if (lastNode instanceof AxiomQueryParser.GenFilterContext || lastNode instanceof AxiomQueryParser.DescendantPathContext) {
            suggestions = getFilters(lastNode.getText());
            suggestions.add(FilterNames.NOT.getLocalPart());
        } else if (lastNode instanceof AxiomQueryParser.SubfilterOrValueContext ctx) {
            suggestions.add(FilterNames.AND.getLocalPart());
            suggestions.add(FilterNames.OR.getLocalPart());
        } else if (lastNode instanceof TerminalNode ctx) {
            if (ctx.getSymbol().getType() == AxiomQueryParser.SEP || ctx.getSymbol().getType() == AxiomQueryParser.AND_KEYWORD || ctx.getSymbol().getType() == AxiomQueryParser.OR_KEYWORD) {
                suggestions = getAllPath();
                suggestions.add(".");
            }
        } else if (lastNode instanceof ErrorNode ctx) {
            // TODO solve Error token
        }

        return suggestions;
    }

    private ParseTree getLastNode() {
        int separatorIndex = -1;

        if (lastSeparator == null) return null;

        ParseTree lastSeparatorParent = lastSeparator.getParent();

        for (int i = 0; i < lastSeparatorParent.getChildCount(); i++) {
            if (lastSeparatorParent.getChild(i) instanceof TerminalNode terminalNode && terminalNode.getSymbol().getType() == AxiomQueryParser.SEP) {
                separatorIndex = i;
            }
        }

        if (separatorIndex > 0) separatorIndex = separatorIndex - 1;

        ParseTree lastNode = lastSeparatorParent.getChild(separatorIndex);
        int count = lastSeparatorParent.getChild(separatorIndex).getChildCount();

        while (lastNode.getChild(count - 1) != null) {
            lastNode = lastNode.getChild(count - 1);
            count = lastNode.getChildCount();
        }

        while (lastNode.getParent().getChildCount() == 1) {
            lastNode = lastNode.getParent();
        }

        return lastNode;
    }

    private ParseTree findNode(ParseTree parseTree) {
        while (parseTree.getChildCount() == 1) {
            parseTree = parseTree.getParent();
        }

        return parseTree;
    }

    private ParseTree getNextToken(@NotNull ParserRuleContext ctx) {
        int count = ctx.getChildCount();
        return count >= 1 ? ctx.getChild(count + 1) : null;
    }

    private ParseTree getPreviousToken(@NotNull ParserRuleContext ctx) {
        int count = ctx.getChildCount();
        return count >= 1 ? ctx.getChild(count - 1) : null;
    }

    private List<String> getAllPath() {
        TypeDefinition typeDefinition = schemaRegistry.findTypeDefinitionByType(new QName(lastType.getText()));
        PrismObjectDefinition<?> objectDefinition = schemaRegistry.findObjectDefinitionByCompileTimeClass((Class) typeDefinition.getCompileTimeClass());
        return objectDefinition.getItemNames().stream().map(QName::getLocalPart).collect(Collectors.toList());
    }

    private List<String> getFilters(@NotNull String stringItemPath) {
        ItemPath itemPath = ItemPathHolder.parseFromString(stringItemPath);
        TypeDefinition typeDefinition = schemaRegistry.findTypeDefinitionByType(new QName(lastType.getText()));
        PrismObjectDefinition<?> objectDefinition = schemaRegistry.findObjectDefinitionByCompileTimeClass((Class) typeDefinition.getCompileTimeClass());
        ItemDefinition<?> itemDefinition = objectDefinition.findItemDefinition(itemPath, ItemDefinition.class);
        return FilterNamesProvider.findFilterNamesByItemDefinition(itemDefinition, new FilterContext());
    }
}
