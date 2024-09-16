package com.evolveum.midpoint.prism.impl.query.lang;

import java.util.*;

import com.evolveum.axiom.lang.antlr.*;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.query.Suggestion;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParserBaseVisitor;
import com.evolveum.midpoint.prism.path.ItemPath;

import com.google.common.base.Strings;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
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
    private final AxiomQueryParser parser;
    public final List<AxiomQueryError> errorList = new ArrayList<>();

    private final PrismContext prismContext;
    private final ItemDefinition<?> rootItemDefinition;
    private final HashMap<ParserRuleContext, Definition> itemDefinitions = new HashMap<>();
    private Definition infraPathDefinition = null;

    private final int positionCursor;
    private PositionContext positionContext;
    private final Definition positionCtxDefinition = null;

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
                    errorRegister(itemDefinitions.get(findIdentifierDefinition(ctx)) != null, ctx, "Invalid meta type '%s'.", ctx.getText());
                } else if (Filter.Meta.PATH.getName().equals(itemFilterContext.getChild(0).getText())) {
                    if (infraPathDefinition == null) {
                        infraPathDefinition = itemDefinitions.get(findIdentifierDefinition(ctx));
                    }

                    infraPathDefinition = findDefinition(infraPathDefinition, new QName(ctx.getText()));
                    errorRegister(infraPathDefinition != null, ctx,
                            "Invalid meta path '%s'.", ctx.getText());
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
            }
//            else if (Filter.Name.TYPE.getName().getLocalPart().equals(itemFilterContext.getChild(2).getText())) {
//                PrismObjectDefinition<?> objectTypeDefinition = prismContext.getSchemaRegistry().findObjectDefinitionByType(prismContext.getDefaultReferenceTargetType());
//                List<TypeDefinition> objectSubTypes = new ArrayList<>(prismContext.getSchemaRegistry().findTypeDefinitionByCompileTimeClass(objectTypeDefinition.getCompileTimeClass(), TypeDefinition.class).getStaticSubTypes());
//
//                objectSubTypes.stream().map(item -> {
//                    if (item.getTypeName().getLocalPart().equals(ctx.getText())) return item;
//                    else {
//                        var subItem = item.getStaticSubTypes().stream().filter(sub -> sub.getTypeName().getLocalPart().equals(ctx.getText())).findFirst();
//                        if (subItem.isPresent()) return subItem.get();
//                    }
//
//                    return null;
//                }).filter(Objects::nonNull).findFirst().ifPresent(targetTypeDefinition -> itemDefinitions.put(findIdentifierDefinition(ctx), targetTypeDefinition));
//                errorRegister(itemDefinitions.get(findIdentifierDefinition(ctx)) != null, ctx, "Invalid type '%s'.", ctx.getText());
//            }
            else {
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
                    if (!itemFilterContext.getChild(2).getText().equals(Filter.Name.TYPE.getName().getLocalPart())) {
                        errorRegister((FilterProvider.findFilterByItemDefinition(
                                        itemDefinitions.get(findIdentifierDefinition(ctx)), ctx.getRuleIndex()).containsKey(ctx.getText())), ctx,
                                "Invalid '%s' filter for self path.", ctx.getText());
                    }
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
    public Object visitSubfilterSpec(AxiomQueryParser.SubfilterSpecContext ctx) {
        infraPathDefinition = null;
        return super.visitSubfilterSpec(ctx);
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
     * Generate auto completions suggestion for AxiomQuery language by context.
     * @return List {@link Suggestion}
     */
    public List<Suggestion> generateSuggestions() {
        List<Suggestion> suggestions = new ArrayList<>();
        Definition definition = null;

        if (positionContext != null) {
            ATN atn = parser.getATN();
            // foreach expected tokens by positionContext
            for (TokenWithCtx token : getExpectedTokenCtxByPositionCtx(atn, positionContext)) {
                if (token.index() == AxiomQueryLexer.IDENTIFIER) {
                    if (token.rules().contains(AxiomQueryParser.RULE_filterName)) {
                        suggestions.add(new Suggestion(Filter.Name.NOT.name().toLowerCase(), Filter.Name.NOT.name().toLowerCase(), -1));
                    } else if (token.rules().contains(AxiomQueryParser.RULE_path)) {
//                        suggestions.add(new Suggestion(Filter.Name.NOT.name().toLowerCase(), Filter.Name.NOT.name().toLowerCase(), -1));
                    } else if (token.rules().contains(AxiomQueryParser.RULE_matchingRule)) {
//                        suggestions.add(new Suggestion(Filter.Name.NOT.name().toLowerCase(), Filter.Name.NOT.name().toLowerCase(), -1));
                    } else if (token.rules().contains(AxiomQueryParser.RULE_subfilterOrValue)) {
//                        suggestions.add(new Suggestion(Filter.Name.NOT.name().toLowerCase(), Filter.Name.NOT.name().toLowerCase(), -1));
                    }
                } else if (token.index() == AxiomQueryLexer.NOT_KEYWORD) {
                    suggestions.add(new Suggestion(Filter.Name.NOT.name().toLowerCase(), Filter.Name.NOT.name().toLowerCase(), -1));
                } else {
                    suggestions.add(new Suggestion(AxiomQueryLexer.VOCABULARY.getDisplayName(token.index()),
                            AxiomQueryLexer.VOCABULARY.getDisplayName(token.index()), -1));
                }
            }
        }

        return suggestions;
    }

    /**
     * Method find all possible following rules based on the cursor position.
     * @param atn rule network
     * @param positionCtx position node
     * @return expected list of pair token with rule ctx
     */
    private List<TokenWithCtx> getExpectedTokenCtxByPositionCtx(ATN atn, PositionContext positionCtx) {
        List<TokenWithCtx> expected = new ArrayList<>();
        ParseTree node = positionCtx.node().getChild(positionCtx.cursorIndex());

        if (node instanceof TerminalNode terminalNode) {
            // if position token is SEPARATOR then find following rule for previous token
            if (terminalNode.getSymbol().getType() == AxiomQueryLexer.SEP) {
                node = positionCtx.node().getChild(positionCtx.cursorIndex() - 1);
                if (node instanceof TerminalNode terminalNode2) {
                    node = terminalNode2.getParent();
                }
            } else {
                node = terminalNode.getParent();
            }
        }

        if (node instanceof RuleContext ruleContext) {
            RuleContext a = (RuleContext) ruleContext.getChild(ruleContext.getChildCount() - 1);
            findTokensWithRuleCtxInATN(atn, a, getTerminalNode(positionCtx.node().getChild(positionCtx.cursorIndex())), expected);
        }

        return expected;
    }

    private void findTokensWithRuleCtxInATN(ATN atn, RuleContext ruleContext, TerminalNode nextTerminalNode, List<TokenWithCtx> expected) {
        Stack<ATNState> states = new Stack<>();
        Stack<ATNState> passedStates = new Stack<>();
        Stack<Integer> rules = new Stack<>();
        ATNState nextState;
        int invokingState;

        if (ruleContext.invokingState == -1) {
            invokingState = 0;
        } else {
            invokingState = ruleContext.invokingState;
        }

        states.push(atn.states.get(invokingState));

        while (!states.isEmpty()) {
            nextState = states.pop();
            passedStates.push(nextState);

            if (!rules.contains(nextState.ruleIndex)) {
                rules.push(nextState.ruleIndex);
            }

            for (Transition transition : nextState.getTransitions()) {
                if (transition instanceof AtomTransition atomTransition) {
                    if (nextTerminalNode.getSymbol().getType() == atomTransition.label) {
                        states.push(atomTransition.target);
                    } else {
                        if (atomTransition.label != -1) {
                            if (atomTransition.label == AxiomQueryLexer.IDENTIFIER) {
                                expected.add(new TokenWithCtx(atomTransition.label, rules));
                            } else {
                                expected.add(new TokenWithCtx(atomTransition.label, null));
                            }
                        }
                    }
                } else if (transition instanceof SetTransition setTransition) {
                    setTransition.set.getIntervals().forEach(interval -> {
                        for (int i = interval.a; i <= interval.b; i++) {
                            expected.add(new TokenWithCtx(i, null));
                        }
                    });
                } else {
                    // check looping
                    if (!passedStates.contains(transition.target) && nextState.getClass() != RuleStopState.class) {
                        if (transition instanceof RuleTransition ruleTransition && ruleTransition.ruleIndex == AxiomQueryParser.RULE_path) {
                            states.push(ruleTransition.followState);
                        } else {
                            states.push(transition.target);
                        }
                    }
                }
            }
        }
    }

    private TerminalNode getTerminalNode(ParseTree parseTree) {
        if (parseTree instanceof TerminalNode terminalNode) {
            return terminalNode;
        }

        if (parseTree != null) {
            while (parseTree.getChildCount() > 0) {
                parseTree = parseTree.getChild(parseTree.getChildCount() - 1);

                if (parseTree instanceof TerminalNode node) {
                    return node;
                }
            }
        }

        return null;
    }
}
