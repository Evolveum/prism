package com.evolveum.midpoint.prism.impl.query.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.evolveum.axiom.lang.antlr.ATNTraverseHelper;
import com.evolveum.axiom.lang.antlr.PositionContext;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.query.Suggestion;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParserBaseVisitor;
import com.evolveum.midpoint.prism.path.ItemPath;

import com.google.common.base.Strings;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

/**
 * This class to represent all semantics validation and autocompletion query for Axiom Query Language.
 * The result of the visitor is list of semantic errors and suggestions for additions in place of cursor position.
 * Error list item is type of {@link AxiomQueryError}.
 * Suggestion list item is type of {@link Suggestion}.
 *
 * Created by Dominik.
 */
public class AxiomQueryContentAssistantVisitor extends AxiomQueryParserBaseVisitor<Object> {
    public final List<AxiomQueryError> errorList = new ArrayList<>();

    private final SchemaRegistry schemaRegistry;
    private final ItemDefinition<?> rootItemDefinition;
    private Definition currentItemDefinition;
    private ComplexTypeDefinition metaTypeDefinition;
    private Definition definitionForAutocomplete;

    int positionCursor;
    private PositionContext positionContext;

    private final AxiomQueryParser parser;

    public AxiomQueryContentAssistantVisitor(SchemaRegistry schemaRegistry, @NotNull ItemDefinition<?> rootItem,
            AxiomQueryParser parser, int positionCursor) {
        this.schemaRegistry = schemaRegistry;
        this.rootItemDefinition = rootItem;
        this.currentItemDefinition = rootItem;
        this.parser = parser;
        this.positionCursor = positionCursor;
    }

    @Override
    public Object visitRoot(AxiomQueryParser.RootContext ctx) {
        ParseTree positionNode = findNodeAtPosition(ctx, positionCursor);

        if (positionNode != null) {
            positionContext = findPositionContext(positionNode);
        } else {
            errorList.add(new AxiomQueryError(
                    positionCursor, positionCursor, positionCursor, positionCursor,
                    "Cursor is outside the query."
            ));
        }

        return super.visitRoot(ctx);
    }

    @Override
    public Object visitSelfPath(AxiomQueryParser.SelfPathContext ctx) {
        return super.visitSelfPath(ctx);
    }

    @Override
    public Object visitParentPath(AxiomQueryParser.ParentPathContext ctx) {
        return super.visitParentPath(ctx);
    }

    @Override
    public Object visitDescendantPath(AxiomQueryParser.DescendantPathContext ctx) {
        return super.visitDescendantPath(ctx);
    }

    @Override
    public Object visitPathAxiomPath(AxiomQueryParser.PathAxiomPathContext ctx) {
        return super.visitPathAxiomPath(ctx);
    }

    @Override
    public Object visitIdentifierComponent(AxiomQueryParser.IdentifierComponentContext ctx) {
        // # can use only for container
        errorRegister((currentItemDefinition instanceof PrismContainerDefinition<?>), ctx,
                "Invalid %s in identifier component.", ctx.getText());
        return super.visitIdentifierComponent(ctx);
    }

    @Override
    public Object visitDereferenceComponent(AxiomQueryParser.DereferenceComponentContext ctx) {
        if (currentItemDefinition instanceof PrismReferenceDefinition referenceDefinition) {
            if (referenceDefinition.getTargetTypeName() != null) {
                currentItemDefinition = schemaRegistry.findComplexTypeDefinitionByType(referenceDefinition.getTargetTypeName());
            }
        } else {
            errorRegister(false, ctx, "Invalid reference in definition.");
        }

        return super.visitDereferenceComponent(ctx);
    }

    @Override
    public Object visitItemComponent(AxiomQueryParser.ItemComponentContext ctx) {
        Map<String, String> metaFilters;

        if ((metaFilters = FilterProvider.findFilterByItemDefinition(
                (ItemDefinition<?>) currentItemDefinition, ctx.getRuleIndex())).containsKey(ctx.getText()) ||
                Filter.ReferencedKeyword.TARGET_TYPE.getName().equals(ctx.getText()) ||
                Filter.ReferencedKeyword.TARGET.getName().equals(ctx.getText()) ||
                Filter.ReferencedKeyword.RELATION.getName().equals(ctx.getText()) ||
                Filter.ReferencedKeyword.OID.getName().equals(ctx.getText())) {
            return super.visitItemComponent(ctx);
        }

        if (findItemFilterCtx(ctx) instanceof AxiomQueryParser.ItemFilterContext itemFilterContext) {
            if (metaFilters.containsKey(itemFilterContext.getChild(0).getText())) {
                if (Filter.Meta.TYPE.getName().equals(itemFilterContext.getChild(0).getText())) {
                    metaTypeDefinition = schemaRegistry.findComplexTypeDefinitionByType(new QName(ctx.getText()));
                    errorRegister(metaTypeDefinition != null, ctx, "Invalid type %s.", ctx.getText());
                } else if (Filter.Meta.PATH.getName().equals(itemFilterContext.getChild(0).getText())) {
                    errorRegister(findDefinition(metaTypeDefinition, new QName(ctx.getText())) != null, ctx,
                            "Invalid type %s.", ctx.getText());
                } else if (Filter.Meta.RELATION.getName().equals(itemFilterContext.getChild(0).getText())) {
                    // TODO @relation meta filter
                }
            }  else if(Filter.ReferencedKeyword.TARGET_TYPE.getName().equals(itemFilterContext.getChild(0).getText())) {
                errorRegister(currentItemDefinition != null, ctx,
                        "Invalid item component %s in definition.", ctx.getText());
            } else if(Filter.ReferencedKeyword.TARGET.getName().equals(itemFilterContext.getChild(0).getText())) {
                currentItemDefinition = findDefinition(currentItemDefinition, new QName(ctx.getText()));
                errorRegister(currentItemDefinition != null, ctx,
                        "Invalid item component %s in definition.", ctx.getText());
            } else if(Filter.ReferencedKeyword.RELATION.getName().equals(itemFilterContext.getChild(0).getText())) {
                currentItemDefinition = findDefinition(currentItemDefinition, new QName(ctx.getText()));
                errorRegister(currentItemDefinition != null, ctx,
                        "Invalid item component %s in definition.", ctx.getText());
            } else if(Filter.ReferencedKeyword.OID.getName().equals(itemFilterContext.getChild(0).getText())) {
                currentItemDefinition = findDefinition(currentItemDefinition, new QName(ctx.getText()));
                errorRegister(currentItemDefinition != null, ctx,
                        "Invalid item component %s in definition.", ctx.getText());
            } else {
                currentItemDefinition = findDefinition(currentItemDefinition, new QName(ctx.getText()));
                errorRegister(currentItemDefinition != null, ctx,
                        "Invalid item component %s in definition.", ctx.getText());
            }
        } else {
            currentItemDefinition = findDefinition(currentItemDefinition, new QName(ctx.getText()));
            errorRegister(currentItemDefinition != null, ctx,
                    "Invalid item component %s in definition.", ctx.getText());
        }

        return super.visitItemComponent(ctx);
    }

    @Override
    public Object visitPathComponent(AxiomQueryParser.PathComponentContext ctx) {
        // TODO itemName [ id ]
        return super.visitPathComponent(ctx);
    }

    @Override
    public Object visitFilterName(AxiomQueryParser.FilterNameContext ctx) {
        if (findItemFilterCtx(ctx) instanceof AxiomQueryParser.ItemFilterContext itemFilterContext) {
            if (Arrays.stream(Filter.Meta.values())
                    .anyMatch(meta -> meta.getName().equals(itemFilterContext.getChild(0).getText())) ||
                Arrays.stream(Filter.ReferencedKeyword.values())
                    .anyMatch(meta -> meta.getName().equals(itemFilterContext.getChild(0).getText()))) {
                errorRegister(Filter.Alias.EQUAL.getName().equals(ctx.getText()), ctx,
                        "Invalid %s filter. Only the assignment sign (=) is correct for %s.", ctx.getText(), itemFilterContext.getChild(0).getText());
            } else {
                errorRegister((FilterProvider.findFilterByItemDefinition(
                                (ItemDefinition<?>) currentItemDefinition, ctx.getRuleIndex()).containsKey(ctx.getText())), ctx,
                        "Invalid %s filter.", ctx.getText());
            }
        }

        return super.visitFilterName(ctx);
    }

    @Override
    public Object visitFilterNameAlias(AxiomQueryParser.FilterNameAliasContext ctx) {
        if (findItemFilterCtx(ctx) instanceof AxiomQueryParser.ItemFilterContext itemFilterContext) {
            if (Arrays.stream(Filter.Meta.values())
                    .anyMatch(meta -> meta.getName().equals(itemFilterContext.getChild(0).getText())) ||
                Arrays.stream(Filter.ReferencedKeyword.values())
                    .anyMatch(meta -> meta.getName().equals(itemFilterContext.getChild(0).getText()))) {
                errorRegister(Filter.Alias.EQUAL.getName().equals(ctx.getText()), ctx,
                    "Invalid %s filter alias. Only the assignment sign (=) is correct for %s.", ctx.getText(), itemFilterContext.getChild(0).getText());
            } else {
                if (itemFilterContext.getChild(0) instanceof AxiomQueryParser.DescendantPathContext) {
                    errorRegister((FilterProvider.findFilterByItemDefinition(
                        (ItemDefinition<?>) currentItemDefinition, ctx.getRuleIndex()).containsValue(ctx.getText())), ctx,
                        "Invalid %s filter alias.", ctx.getText());
                }
            }
        }

        return super.visitFilterNameAlias(ctx);
    }

    @Override
    public Object visitErrorNode(ErrorNode node) {
//        Token token  = node.getSymbol();
//        if (errorTokenContextMap.getErrorToken().equals(token)) {
//            if (node.getParent() instanceof AxiomQueryParser.ItemFilterContext itemFilter) {
//
//            }
//        }
        return super.visitErrorNode(node);
    }

    public List<Suggestion> generateSuggestions() {
        List<Suggestion> suggestions = new ArrayList<>();

        if (positionContext != null) {
            ATN atn = parser.getATN();

            for (int ruleIndex : ATNTraverseHelper.findFollowingRulesByPositionContext(atn, positionContext)) {
                atn.nextTokens(atn.states.get(atn.ruleToStartState[ruleIndex].stateNumber)).getIntervals().forEach(interval -> {
                    for (int i = interval.a; i <= interval.b; i++) {
                        // handle tokens
                        if (i == AxiomQueryLexer.IDENTIFIER) {
                            // handle rules for IDENTIFIER
                            if (ruleIndex == AxiomQueryParser.RULE_filterName) {
                                FilterProvider.findFilterByItemDefinition(rootItemDefinition, ruleIndex)
                                        .forEach((name, alias) -> suggestions.add(new Suggestion(name, alias, -1)));
                            } else if (ruleIndex == AxiomQueryParser.RULE_matchingRule) {
                                // find path for matching rule
                            } else if (ruleIndex == AxiomQueryParser.RULE_path || ruleIndex == AxiomQueryParser.RULE_filter) {
                                // find path
                            }
                        } if (i == AxiomQueryLexer.NOT_KEYWORD) {
                            suggestions.add(new Suggestion(Filter.Name.NOT.name(), Filter.Name.NOT.name(), -1));
                        } else {
                            // skip filter alias
                            if (ruleIndex == AxiomQueryParser.RULE_filterNameAlias) {
                                suggestions.add(new Suggestion(AxiomQueryLexer.VOCABULARY.getDisplayName(i),
                                        AxiomQueryLexer.VOCABULARY.getDisplayName(i), -1));
                            }
                        }
                        // handle rules for other cases
                        if (ruleIndex == AxiomQueryParser.RULE_negation) {
                            FilterProvider.findFilterByItemDefinition(rootItemDefinition, ruleIndex)
                                    .forEach((name, alias) -> suggestions.add(new Suggestion(name, alias, -1)));
                        }
                    }
                });
            }
        }

        return suggestions;
    }

    private void errorRegister(boolean condition, ParserRuleContext ctx, String message, Object... arguments) {
        if (!condition) {
            errorList.add(new AxiomQueryError(
                    ctx.getStart().getLine(), ctx.getStop().getLine(),
                    ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1,
                    Strings.lenientFormat(message, arguments)
            ));
        }
    }

    private ItemDefinition<?> findDefinition(Definition parentDefinition, QName name) {
        if (parentDefinition instanceof PrismContainerDefinition<?> containerDefinition) {
            return containerDefinition.getComplexTypeDefinition().findLocalItemDefinition(name);
        } else if (parentDefinition instanceof PrismReferenceDefinition referenceDefinition) {
            return ItemPath.isObjectReference(name) && referenceDefinition.getTargetObjectDefinition() != null
                    ? referenceDefinition.getTargetObjectDefinition() : null;
        } else if (parentDefinition instanceof ComplexTypeDefinition complexTypeDefinition) {
            return complexTypeDefinition.findLocalItemDefinition(name);
        }
        return null;
    }

    private ParseTree findNodeAtPosition(ParseTree tree, int position) {
        if (tree instanceof TerminalNode) {
            Token token = ((TerminalNode) tree).getSymbol();
            if (token.getStartIndex() <= position && token.getStopIndex() >= position) {
                return tree;
            }
        } else {
            for (int i = 0; i < tree.getChildCount(); i++) {
                ParseTree child = tree.getChild(i);
                ParseTree result = findNodeAtPosition(child, position);
                if (result != null) {
                    return result;
                }
            }
        }

        if (tree instanceof AxiomQueryParser.RootContext && position == 0) return tree;

        return null;
    }

    private PositionContext findPositionContext(ParseTree tree) {
        int count = tree.getChildCount();
        ParseTree parent;

        if (tree instanceof AxiomQueryParser.RootContext) {
            return new PositionContext(0, tree);
        }

        while (count <= 1) {
            if ((parent = tree.getParent()).getChildCount() > 1) {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    if (parent.getChild(i).hashCode() == tree.hashCode()) {
                        return new PositionContext(i, parent);
                    }
                }
            }

            tree = tree.getParent();
            count = tree.getChildCount();
        }

        return null;
    }

    private ParseTree findItemFilterCtx(ParseTree ctx) {
        if (ctx == null) return null;

        while (!ctx.getClass().equals(AxiomQueryParser.ItemFilterContext.class)) {
            ctx = ctx.getParent();
        }

        return ctx;
    }
}
