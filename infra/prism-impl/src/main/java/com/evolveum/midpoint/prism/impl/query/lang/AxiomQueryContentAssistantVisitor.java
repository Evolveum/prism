package com.evolveum.midpoint.prism.impl.query.lang;

import java.util.*;

import com.evolveum.axiom.lang.antlr.ATNTraverseHelper;
import com.evolveum.axiom.lang.antlr.PositionContext;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.query.Suggestion;
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

    private final PrismContext prismContext;
    private final ItemDefinition<?> rootItemDefinition;
    private final HashMap<ParserRuleContext, Definition> itemDefinitions = new HashMap<>();
    private Definition definitionForAutocomplete;

    int positionCursor;
    private PositionContext positionContext;

    private final AxiomQueryParser parser;

    public AxiomQueryContentAssistantVisitor(PrismContext prismContext, @NotNull ItemDefinition<?> rootItem,
            AxiomQueryParser parser, int positionCursor) {
        this.prismContext = prismContext;
        this.rootItemDefinition = rootItem;
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

        itemDefinitions.clear();
        // Initialization root definition
        itemDefinitions.put(ctx, rootItemDefinition);

        return super.visitRoot(ctx);
    }

    @Override
    public Object visitIdentifierComponent(AxiomQueryParser.IdentifierComponentContext ctx) {
        // # can use only for container
        errorRegister((itemDefinitions.get(findIdentifierDefinition(ctx)) instanceof PrismContainerDefinition<?>), ctx,
                "Invalid '%s' in identifier component.", ctx.getText());
        return super.visitIdentifierComponent(ctx);
    }

    @Override
    public Object visitDereferenceComponent(AxiomQueryParser.DereferenceComponentContext ctx) {
        if (itemDefinitions.get(findIdentifierDefinition(ctx)) instanceof PrismReferenceDefinition referenceDefinition) {
            if (referenceDefinition.getTargetObjectDefinition() != null) {
                itemDefinitions.put(findIdentifierDefinition(ctx), prismContext.getSchemaRegistry().findObjectDefinitionByType(referenceDefinition.getTargetTypeName()));
            } else if (referenceDefinition.getTargetTypeName() != null) {
                itemDefinitions.put(findIdentifierDefinition(ctx), prismContext.getSchemaRegistry().findObjectDefinitionByType(referenceDefinition.getTargetTypeName()));
            } else {
                errorRegister(false, ctx, "Invalid dereference path is null.");
            }
        } else {
            errorRegister(false, ctx, "Invalid dereference path because reference definition is null.");
        }

        return super.visitDereferenceComponent(ctx);
    }

    @Override
    public Object visitItemComponent(AxiomQueryParser.ItemComponentContext ctx) {
        Map<String, String> metaFilters;

        if ((metaFilters = FilterProvider.findFilterByItemDefinition(itemDefinitions.get(findIdentifierDefinition(ctx)),
                ctx.getRuleIndex())).containsKey(ctx.getText())
                || Filter.ReferencedKeyword.TARGET_TYPE.getName().equals(ctx.getText())
                || Filter.ReferencedKeyword.TARGET.getName().equals(ctx.getText())
                || Filter.ReferencedKeyword.RELATION.getName().equals(ctx.getText())
                || Filter.ReferencedKeyword.OID.getName().equals(ctx.getText())) {
            return super.visitItemComponent(ctx);
        }

        if (findItemFilterCtx(ctx) instanceof AxiomQueryParser.ItemFilterContext itemFilterContext) {
            if (metaFilters.containsKey(itemFilterContext.getChild(0).getText())) {
                if (Filter.Meta.TYPE.getName().equals(itemFilterContext.getChild(0).getText())) {
                    itemDefinitions.put(findIdentifierDefinition(ctx), prismContext.getSchemaRegistry().findComplexTypeDefinitionByType(new QName(ctx.getText())));
                    errorRegister(itemDefinitions.get(findIdentifierDefinition(ctx)) != null, ctx, "Invalid type %s.", ctx.getText());
                } else if (Filter.Meta.PATH.getName().equals(itemFilterContext.getChild(0).getText())) {
                    itemDefinitions.put(findIdentifierDefinition(ctx), findDefinition(itemDefinitions.get(findIdentifierDefinition(ctx)), new QName(ctx.getText())));
                    errorRegister(itemDefinitions.get(findIdentifierDefinition(ctx)) != null, ctx,
                            "Invalid type '%s'.", ctx.getText());
                } else if (Filter.Meta.RELATION.getName().equals(itemFilterContext.getChild(0).getText())) {
                    // TODO @relation meta filter
                }
            }  else if(Filter.ReferencedKeyword.TARGET_TYPE.getName().equals(itemFilterContext.getChild(0).getText())) {
                PrismObjectDefinition<?> objectTypeDefinition = prismContext.getSchemaRegistry().findObjectDefinitionByType(prismContext.getDefaultReferenceTargetType());
                List<TypeDefinition> objectSubTypes = new ArrayList<>(prismContext.getSchemaRegistry().findTypeDefinitionByCompileTimeClass(objectTypeDefinition.getCompileTimeClass(), TypeDefinition.class).getStaticSubTypes());

                objectSubTypes.stream().map(item -> {
                    if (item.getTypeName().getLocalPart().equals(ctx.getText())) return item;
                    else {
                        var subItem = item.getStaticSubTypes().stream().filter(sub -> sub.getTypeName().getLocalPart().equals(ctx.getText())).findFirst();
                        if (subItem.isPresent()) return subItem.get();
                    }

                    return null;
                }).filter(Objects::nonNull).findFirst().ifPresent(targetTypeDefinition -> itemDefinitions.put(findIdentifierDefinition(ctx), targetTypeDefinition));

                errorRegister(itemDefinitions.get(findIdentifierDefinition(ctx)) != null, ctx, "Invalid target type '%s'.", ctx.getText());
            } else if(Filter.ReferencedKeyword.RELATION.getName().equals(itemFilterContext.getChild(0).getText())) {
                // TODO relation semantic control
            } else if(Filter.ReferencedKeyword.OID.getName().equals(itemFilterContext.getChild(0).getText())) {
                // TODO oid semantic control
            } else if (Filter.Name.TYPE.getName().getLocalPart().equals(itemFilterContext.getChild(2).getText())) {
                PrismObjectDefinition<?> objectTypeDefinition = prismContext.getSchemaRegistry().findObjectDefinitionByType(prismContext.getDefaultReferenceTargetType());
                List<TypeDefinition> objectSubTypes = new ArrayList<>(prismContext.getSchemaRegistry().findTypeDefinitionByCompileTimeClass(objectTypeDefinition.getCompileTimeClass(), TypeDefinition.class).getStaticSubTypes());

                objectSubTypes.stream().map(item -> {
                    if (item.getTypeName().getLocalPart().equals(ctx.getText())) return item;
                    else {
                        var subItem = item.getStaticSubTypes().stream().filter(sub -> sub.getTypeName().getLocalPart().equals(ctx.getText())).findFirst();
                        if (subItem.isPresent()) return subItem.get();
                    }

                    return null;
                }).filter(Objects::nonNull).findFirst();

                System.out.println(ctx.getText() + " SDSKDKKS>> " + objectSubTypes);

                errorRegister(itemDefinitions.get(findIdentifierDefinition(ctx)) != null, ctx, "Invalid type '%s'.", ctx.getText());
            } else {
                itemDefinitions.put(findIdentifierDefinition(ctx), findDefinition(itemDefinitions.get(findIdentifierDefinition(ctx)), new QName(ctx.getText())));
                errorRegister(itemDefinitions.get(findIdentifierDefinition(ctx)) != null, ctx,
                        "Invalid item component '%s' definition.", ctx.getText());
            }
        } else {
            itemDefinitions.put(findIdentifierDefinition(ctx), findDefinition(itemDefinitions.get(findIdentifierDefinition(ctx)), new QName(ctx.getText())));
            errorRegister(itemDefinitions.get(findIdentifierDefinition(ctx)) != null, ctx,
                    "Invalid item component '%s' definition.", ctx.getText());
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
                    .anyMatch(meta -> meta.getName().equals(itemFilterContext.getChild(0).getText()))
                || Arrays.stream(Filter.ReferencedKeyword.values())
                    .anyMatch(meta -> meta.getName().equals(itemFilterContext.getChild(0).getText()))) {
                errorRegister(Filter.Alias.EQUAL.getName().equals(ctx.getText()), ctx,
                        "Invalid '%s' filter. Only the assignment sign (=) is correct for '%s'.", ctx.getText(), itemFilterContext.getChild(0).getText());
            } else {
                if(itemFilterContext.getChild(0) instanceof AxiomQueryParser.SelfPathContext) {
                    errorRegister((FilterProvider.findFilterByItemDefinition(
                                    itemDefinitions.get(findIdentifierDefinition(ctx)), ctx.getRuleIndex()).containsKey(ctx.getText())), ctx,
                            "Invalid '%s' filter for self path.", ctx.getText());
                } else {
                    errorRegister((FilterProvider.findFilterByItemDefinition(
                                    itemDefinitions.get(findIdentifierDefinition(ctx)), ctx.getRuleIndex()).containsKey(ctx.getText())), ctx,
                            "Invalid '%s' filter.", ctx.getText());
                }
            }
        }

        return super.visitFilterName(ctx);
    }

    @Override
    public Object visitFilterNameAlias(AxiomQueryParser.FilterNameAliasContext ctx) {
        if (findItemFilterCtx(ctx) instanceof AxiomQueryParser.ItemFilterContext itemFilterContext) {
            if (Arrays.stream(Filter.Meta.values())
                    .anyMatch(meta -> meta.getName().equals(itemFilterContext.getChild(0).getText()))
                || Arrays.stream(Filter.ReferencedKeyword.values())
                    .anyMatch(meta -> meta.getName().equals(itemFilterContext.getChild(0).getText()))) {
                errorRegister(Filter.Alias.EQUAL.getName().equals(ctx.getText()), ctx,
                    "Invalid '%s' filter alias. Only the assignment sign (=) is correct for %s.", ctx.getText(), itemFilterContext.getChild(0).getText());
            } else {
                if(itemFilterContext.getChild(0) instanceof AxiomQueryParser.SelfPathContext) {
                    errorRegister((FilterProvider.findFilterByItemDefinition(
                                    itemDefinitions.get(findIdentifierDefinition(ctx)), ctx.getRuleIndex()).containsValue(ctx.getText())), ctx,
                            "Invalid '%s' filter alias for self path.", ctx.getText());
                } else {
                    errorRegister((FilterProvider.findFilterByItemDefinition(
                        itemDefinitions.get(findIdentifierDefinition(ctx)), ctx.getRuleIndex()).containsValue(ctx.getText())), ctx,
                        "Invalid '%s' filter alias.", ctx.getText());
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

    /**
     * Generate auto completions suggestion for AxiomQuery language by context.
     * @return List {@link Suggestion}
     */
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

    /**
     * Registering error record on based input parameter condition.
     * @param condition
     * @param ctx
     * @param message
     * @param arguments
     */
    private void errorRegister(boolean condition, ParserRuleContext ctx, String message, Object... arguments) {
        if (!condition) {
            errorList.add(new AxiomQueryError(
                    ctx.getStart().getLine(), ctx.getStop().getLine(),
                    ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1,
                    Strings.lenientFormat(message, arguments)
            ));
        }
    }

    /**
     * Find definition of schema context.
     * @param parentDefinition
     * @param name
     * @return Definition found or null
     */
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

    /**
     * Find node in parserTree which to content cursor by cursor position.
     * @param tree
     * @param position
     * @return the node in which it is located cursor or null
     */
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

    /**
     * Find node which has cursor position with branch index
     * @param tree
     * @return {@link PositionContext}
     */
    private PositionContext findPositionContext(ParseTree tree) {
        int count = tree.getChildCount();
        ParseTree parent;

        if (tree instanceof AxiomQueryParser.RootContext) {
            return new PositionContext(0, tree);
        }

        while (count <= 1) {
            if ((parent = tree.getParent()).getChildCount() > 1) {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    if (parent.getChild(i).equals(tree)) {
                        return new PositionContext(i, parent);
                    }
                }
            }

            tree = tree.getParent();
            count = tree.getChildCount();
        }

        return null;
    }

    /**
     * Found itemFilter context in input parserTree. ItemFilter is main non-terminal for every filter query see you grammar
     * language AxiomQueryParser.g4.
     * @param ctx
     * @return
     */
    private ParseTree findItemFilterCtx(ParseTree ctx) {
        if (ctx == null) return null;

        while (!ctx.getClass().equals(AxiomQueryParser.ItemFilterContext.class)) {
            ctx = ctx.getParent();
        }

        return ctx;
    }

    /**
     * Find reference object of node which present change of definition in AST (rootContext or subfilterSpecContext).
     * @param ctx
     * @return reference object as identifier for {@link AxiomQueryContentAssistantVisitor#itemDefinitions}
     */
    private ParserRuleContext findIdentifierDefinition(ParserRuleContext ctx) {
        while (!AxiomQueryParser.RootContext.class.equals(ctx.getClass())) {
            if (ctx instanceof AxiomQueryParser.SubfilterSpecContext) break;
            ctx = ctx.getParent();
        }

        return ctx;
    }
}
