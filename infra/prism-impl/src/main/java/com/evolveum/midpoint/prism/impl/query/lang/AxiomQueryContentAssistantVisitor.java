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

    private final ATN atn;
    private final PrismContext prismContext;
    private final ItemDefinition<?> rootItemDefinition;
    private final HashMap<RuleContext, Definition> itemDefinitions = new HashMap<>();
    public final List<AxiomQueryError> errorList = new ArrayList<>();
    private final int positionCursor;

    private Definition infraPathDefinition;
    private PositionContext positionContext;

    /**
     * itemComponents should be treated as item path
     */
    private boolean itemExpected;
    /**
     *  is first item path component expected?
     */
    private boolean firstItemComponentExpected;

    public AxiomQueryContentAssistantVisitor(PrismContext prismContext, ItemDefinition<?> rootItem,
            ATN atn, int positionCursor) {
        this.prismContext = prismContext;
        this.rootItemDefinition = rootItem;
        this.atn = atn;
        this.positionCursor = positionCursor;
    }

    @Override
    public Object visitRoot(AxiomQueryParser.RootContext ctx) {
        ParseTree positionNode = findNodeLeftOfCursor(ctx, positionCursor);

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
        // '#' can use only for container
        errorRegister((itemDefinitions.get(findIdentifierDefinition(ctx)) instanceof PrismContainerDefinition<?>), ctx,
                "Invalid '%s' in identifier component.", ctx.getText());
        return super.visitIdentifierComponent(ctx);
    }

    @Override
    public Object visitItemFilter(AxiomQueryParser.ItemFilterContext ctx) {
        this.itemExpected = true;
        return super.visitItemFilter(ctx);

    }

    @Override
    public Object visitSubfilterOrValue(AxiomQueryParser.SubfilterOrValueContext ctx) {
        this.itemExpected = false;

        var parent = parentItemFilter(ctx);
        if (parent.filterName() != null && Filter.Name.MATCHES.getLocalPart().equals(parent.filterName().getText())) {
            if (ctx.subfilterSpec() != null) {
                updateItemDefinitionForContext(ctx.subfilterSpec(), itemFilterDefinition(parent));
            }
        }

        return super.visitSubfilterOrValue(ctx);
    }

    @Override
    public Object visitDescendantPath(AxiomQueryParser.DescendantPathContext ctx) {
        this.firstItemComponentExpected = true;
        return super.visitDescendantPath(ctx);

    }

    @Override
    public Object visitSelfPath(AxiomQueryParser.SelfPathContext ctx) {
        this.firstItemComponentExpected = false;
        var filterCtx = parentItemFilter(ctx);
        updateItemDefinitionForContext(filterCtx, findParentContextDefinition(filterCtx));
        return super.visitSelfPath(ctx);
    }

    @Override
    public Object visitAxiomPath(AxiomQueryParser.AxiomPathContext ctx) {
        this.firstItemComponentExpected = true;
        return super.visitAxiomPath(ctx);
    }

    @Override
    public Object visitParentPath(AxiomQueryParser.ParentPathContext ctx) {
        this.firstItemComponentExpected = true;
        return super.visitParentPath(ctx);
    }

    @Override
    public Object visitDereferenceComponent(AxiomQueryParser.DereferenceComponentContext ctx) {
        var first = firstItemComponentExpected;
        firstItemComponentExpected = false;
        var itemFilterContext = parentItemFilter(ctx);
        if (first) {
            updateItemDefinitionForContext(itemFilterContext, findParentContextDefinition(itemFilterContext));
        }
        if (itemFilterDefinition(itemFilterContext) instanceof PrismReferenceDefinition referenceDefinition) {
            if (referenceDefinition.getTargetObjectDefinition() != null) {
                updateItemDefinitionForContext(itemFilterContext, prismContext.getSchemaRegistry().findObjectDefinitionByType(referenceDefinition.getTargetTypeName()));
            } else if (referenceDefinition.getTargetTypeName() != null) {
                updateItemDefinitionForContext(itemFilterContext, prismContext.getSchemaRegistry().findObjectDefinitionByType(referenceDefinition.getTargetTypeName()));
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
        var identifier = ctx.getText();
        var isFirst = firstItemComponentExpected;
        firstItemComponentExpected = false;
        if (itemExpected) {
            // Item path is part of item

        }

        if ((metaFilters = FilterProvider.findFilterByItemDefinition(itemDefinitions.get(findIdentifierDefinition(ctx)),
                ctx.getRuleIndex())).containsKey(identifier)
                || Filter.ReferencedKeyword.TARGET_TYPE.getName().equals(identifier)
                || Filter.ReferencedKeyword.TARGET.getName().equals(identifier)
                || Filter.ReferencedKeyword.RELATION.getName().equals(identifier)
                || Filter.ReferencedKeyword.OID.getName().equals(identifier)) {
            return super.visitItemComponent(ctx);
        }

        var itemFilterContext = parentItemFilter(ctx);
        if (metaFilters.containsKey(itemFilterContext.getChild(0).getText())) {
            if (Filter.Meta.TYPE.getName().equals(itemFilterContext.getChild(0).getText())) {
                updateItemDefinitionForContext(findIdentifierDefinition(ctx), prismContext.getSchemaRegistry().findComplexTypeDefinitionByType(new QName(identifier)));
                errorRegister(itemDefinitions.get(findIdentifierDefinition(ctx)) != null, ctx, "Invalid meta type '%s'.", identifier);
            } else if (Filter.Meta.PATH.getName().equals(itemFilterContext.getChild(0).getText())) {
                if (infraPathDefinition == null) {
                    infraPathDefinition = itemDefinitions.get(findIdentifierDefinition(ctx));
                }

                infraPathDefinition = findChildDefinition(infraPathDefinition, new QName(identifier));
                errorRegister(infraPathDefinition != null, ctx,
                        "Invalid meta path '%s'.", identifier);
            } else if (Filter.Meta.RELATION.getName().equals(itemFilterContext.getChild(0).getText())) {
                // TODO @relation meta filter
            }
        }  else if(isTargetTypeFilter(itemFilterContext)) {
            PrismObjectDefinition<?> objectTypeDefinition = prismContext.getSchemaRegistry().findObjectDefinitionByType(prismContext.getDefaultReferenceTargetType());
            List<TypeDefinition> objectSubTypes = new ArrayList<>(prismContext.getSchemaRegistry().findTypeDefinitionByCompileTimeClass(objectTypeDefinition.getCompileTimeClass(), TypeDefinition.class).getStaticSubTypes());

            objectSubTypes.stream().map(item -> {
                if (item.getTypeName().getLocalPart().equals(identifier)) return item;
                else {
                    var subItem = item.getStaticSubTypes().stream().filter(sub -> sub.getTypeName().getLocalPart().equals(identifier)).findFirst();
                    if (subItem.isPresent()) return subItem.get();
                }

                return null;
            }).filter(Objects::nonNull).findFirst().ifPresent(targetTypeDefinition -> updateItemDefinitionForContext(findIdentifierDefinition(ctx), targetTypeDefinition));

            errorRegister(itemDefinitions.get(findIdentifierDefinition(ctx)) != null, ctx, "Invalid target type '%s'.", identifier);
        } else if (isTypeFilter(itemFilterContext)) {
            PrismObjectDefinition<?> objectTypeDefinition = prismContext.getSchemaRegistry().findObjectDefinitionByType(prismContext.getDefaultReferenceTargetType());
            List<TypeDefinition> objectSubTypes = new ArrayList<>(prismContext.getSchemaRegistry().findTypeDefinitionByCompileTimeClass(objectTypeDefinition.getCompileTimeClass(), TypeDefinition.class).getStaticSubTypes());

            objectSubTypes.stream().map(item -> {
                if (item.getTypeName().getLocalPart().equals(identifier)) return item;
                else {
                    var subItem = item.getStaticSubTypes().stream().filter(sub -> sub.getTypeName().getLocalPart().equals(identifier)).findFirst();
                    if (subItem.isPresent()) return subItem.get();
                }

                return null;
            }).filter(Objects::nonNull).findFirst().ifPresent(targetTypeDefinition -> updateItemDefinitionForContext(findIdentifierDefinition(ctx), targetTypeDefinition));
            errorRegister(itemDefinitions.get(findIdentifierDefinition(ctx)) != null, ctx, "Invalid type '%s'.", identifier);
        } else if(Filter.ReferencedKeyword.RELATION.getName().equals(itemFilterContext.getChild(0).getText())) {
            // TODO relation semantic control
        } else if(Filter.ReferencedKeyword.OID.getName().equals(itemFilterContext.getChild(0).getText())) {
            // TODO oid semantic control
        } else {

            Definition  contextDefinition;
            if (isFirst) {
                // If item component is first we see, we use parent definition
                contextDefinition = findParentContextDefinition(itemFilterContext);
            } else {
                // Otherwise we use current filter definition (may be null if previous item did not existed)
                contextDefinition = itemFilterDefinition(ctx);
            }
            var foundDefinition = findChildDefinition(contextDefinition, new QName(identifier));
            updateItemDefinitionForContext(itemFilterContext, foundDefinition);
            errorRegister(foundDefinition != null, ctx,
                    "Invalid item component '%s' definition.", identifier);
        }
        return super.visitItemComponent(ctx);
    }

    /**
     * Finds context definition - it can be definition in current item filter (if processing descendant paths, or in any parent
     * item filters and / or root.
     * @param ctx
     */
    private Definition findParentContextDefinition(AxiomQueryParser.ItemFilterContext ctx) {
        var parentCtx = ctx.getParent();
        while (parentCtx != null) {
            var maybe = itemDefinitions.get(parentCtx);
            if (maybe != null) {
                return maybe;
            }
            if (parentCtx instanceof AxiomQueryParser.ItemFilterContext) {
                break;
            }
            if (parentCtx instanceof AxiomQueryParser.SubfilterSpecContext) {
                break;
            }
            parentCtx = parentCtx.getParent();
        }
        return null;
    }

    private boolean isTargetTypeFilter(AxiomQueryParser.ItemFilterContext itemFilterContext) {
        return Filter.ReferencedKeyword.TARGET_TYPE.getName().equals(itemFilterContext.getChild(0).getText());
    }

    private boolean isTypeFilter(AxiomQueryParser.ItemFilterContext itemFilterContext) {
        return itemFilterContext.getChild(2) != null && Filter.Name.TYPE.getName().getLocalPart().equals(itemFilterContext.getChild(2).getText());
    }

    @Override
    public Object visitPathComponent(AxiomQueryParser.PathComponentContext ctx) {
        // TODO itemName [ id ]
        return super.visitPathComponent(ctx);
    }

    @Override
    public Object visitFilterName(AxiomQueryParser.FilterNameContext ctx) {
        var itemFilterContext = parentItemFilter(ctx);
        var itemDefinition = itemFilterDefinition(ctx);
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
                                    itemDefinition, ctx.getRuleIndex()).containsKey(ctx.getText())), ctx,
                            "Invalid '%s' filter for self path.", ctx.getText());
                }
            } else {
                errorRegister((FilterProvider.findFilterByItemDefinition(
                                itemDefinition, ctx.getRuleIndex()).containsKey(ctx.getText())), ctx,
                        "Invalid '%s' filter.", ctx.getText());
            }
        }
        return super.visitFilterName(ctx);
    }

    @Override
    public Object visitFilterNameAlias(AxiomQueryParser.FilterNameAliasContext ctx) {
        var itemFilterContext = parentItemFilter(ctx);
        if (Arrays.stream(Filter.Meta.values())
                .anyMatch(meta -> meta.getName().equals(itemFilterContext.getChild(0).getText()))
            || Arrays.stream(Filter.ReferencedKeyword.values())
                .anyMatch(meta -> meta.getName().equals(itemFilterContext.getChild(0).getText()))) {
            errorRegister(Filter.Alias.EQUAL.getName().equals(ctx.getText()), ctx,
                "Invalid '%s' filter alias. Only the assignment sign (=) is correct for %s.", ctx.getText(), itemFilterContext.getChild(0).getText());
        } else {
            if(itemFilterContext.getChild(0) instanceof AxiomQueryParser.SelfPathContext) {
                errorRegister((FilterProvider.findFilterByItemDefinition(
                                itemFilterDefinition(ctx), ctx.getRuleIndex()).containsValue(ctx.getText())), ctx,
                        "Invalid '%s' filter alias for self path.", ctx.getText());
            } else {
                errorRegister((FilterProvider.findFilterByItemDefinition(
                                itemFilterDefinition(ctx), ctx.getRuleIndex()).containsValue(ctx.getText())), ctx,
                    "Invalid '%s' filter alias.", ctx.getText());
            }
        }
        return super.visitFilterNameAlias(ctx);
    }

    private Definition itemFilterDefinition(ParserRuleContext context) {
        return itemDefinitions.get(parentItemFilter(context));
    }

    @Override
    public Object visitSubfilterSpec(AxiomQueryParser.SubfilterSpecContext ctx) {
        infraPathDefinition = null;
        return super.visitSubfilterSpec(ctx);
    }

    /**
     * Generate auto completions suggestion for AxiomQuery language by context.
     * @return List {@link Suggestion}
     */
    public List<Suggestion> generateSuggestions() {
        List<Suggestion> suggestions = new ArrayList<>();

        if (positionContext != null) {
            Definition definition = itemDefinitions.get(findIdentifierDefinition((RuleContext) positionContext.node().getChild(positionContext.cursorIndex()).getParent()));

            for (TokenWithCtx token : getExpectedTokenCtxByPositionCtx(atn, positionContext)) {
                if (token.index() == AxiomQueryLexer.IDENTIFIER) {
                    if (token.rules().contains(AxiomQueryParser.RULE_filterName)) {
                        var itemDefinition = itemDefinitions.get(parentItemFilter(positionContext.node()));
                        if (itemDefinition != null) {
                            FilterProvider.findFilterByItemDefinition(itemDefinition, AxiomQueryParser.RULE_filterName).forEach((name, alias) -> {
                                suggestions.add(new Suggestion(name, alias, -1));
                            });
                        }
                    } else if (token.rules().contains(AxiomQueryParser.RULE_path)) {
                        definitionProcessingToPathSuggestion(definition, suggestions);
                    } else if (token.rules().contains(AxiomQueryParser.RULE_matchingRule)) {
                        // generate matching paths to [... ]
                    } else if (token.rules().contains(AxiomQueryParser.RULE_subfilterOrValue)) {
                        // subfilter path ???
                    }
                } else if (token.index() == AxiomQueryLexer.NOT_KEYWORD) {
                    suggestions.add(new Suggestion(Filter.Name.NOT.name().toLowerCase(), Filter.Name.NOT.name().toLowerCase(), -1));
                } else if (token.index() == AxiomQueryLexer.AND_KEYWORD) {
                    suggestions.add(new Suggestion(Filter.Name.AND.name().toLowerCase(), Filter.Name.AND.name().toLowerCase(), -1));
                } else if (token.index() == AxiomQueryLexer.OR_KEYWORD) {
                    suggestions.add(new Suggestion(Filter.Name.OR.name().toLowerCase(), Filter.Name.OR.name().toLowerCase(), -1));
                }else {
                    suggestions.add(suggestionFromVocabulary(token, -1));
                }
            }
        }

        return suggestions;
    }

    private Suggestion suggestionFromVocabulary(TokenWithCtx token, int priority) {
        // DisplayName (or LiteralName) is escaped with single qoutes, so we remove them
        var tokenValue = AxiomStrings.fromOptionallySingleQuoted(AxiomQueryLexer.VOCABULARY.getDisplayName(token.index()));
        return new Suggestion(tokenValue, tokenValue, -1);
    }

    /**
     * Find definition of schema context.
     * @param parentDefinition
     * @param name
     * @return Definition found or null
     */
    private ItemDefinition<?> findChildDefinition(Definition parentDefinition, QName name) {
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
     * Processing definition of all types for generate path suggestion.
     * @param definition
     * @param suggestions
     */
    private void definitionProcessingToPathSuggestion(Definition definition, List<Suggestion> suggestions) {
        if (definition instanceof PrismContainerDefinition<?> containerDefinition) {
            containerDefinition.getDefinitions().forEach(prop -> {
                suggestions.add(new Suggestion(prop.getItemName().getLocalPart(), prop.getDisplayName(), -1));
                if (prop instanceof PrismContainerDefinition<?> containerDefinition1) {
                    containerDefinition1.getPropertyDefinitions().forEach( o -> {
                        suggestions.add(new Suggestion(containerDefinition1.getItemName().getLocalPart() + "/" + o.getItemName().getLocalPart(), "prop", -1));
                    });
                }
            });
        } else if (definition instanceof PrismReferenceDefinition referenceDefinition) {
            suggestions.add(new Suggestion(referenceDefinition.getItemName().getLocalPart(), "reference", -1));
        } else if (definition instanceof ComplexTypeDefinition complexTypeDefinition) {
            complexTypeDefinition.getDefinitions().forEach(d -> {
                suggestions.add(new Suggestion(d.getItemName().getLocalPart(), "path", -1));
            });
        } else if (definition instanceof PrismPropertyDefinition<?> propertyDefinition) {
            suggestions.add(new Suggestion(propertyDefinition.getItemName().getLocalPart(), "property", -1));
        }
    }

    /**
     * Find node in parserTree which to content cursor by cursor position.
     * @param tree
     * @param position
     * @return the node in which it is located cursor or null
     */
    private ParseTree findNodeLeftOfCursor(ParseTree tree, int position) {
        if (tree instanceof TerminalNode) {
            Token token = ((TerminalNode) tree).getSymbol();
            if (token.getStartIndex() <= position && token.getStopIndex() >= position) {
                return tree;
            }
        } else {
            for (int i = 0; i < tree.getChildCount(); i++) {
                ParseTree child = tree.getChild(i);
                ParseTree result = findNodeLeftOfCursor(child, position);
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

    private PositionContext findPositionContextAdjusted(ParseTree tree) {
        int count = tree.getChildCount();
        ParseTree parent;

        /*
        if (tree instanceof AxiomQueryParser.RootContext) {
            return new PositionContext(0, tree);
        }
        */

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
    private AxiomQueryParser.ItemFilterContext parentItemFilter(ParseTree ctx) {
        if (ctx == null) return null;

        while (ctx != null && !ctx.getClass().equals(AxiomQueryParser.ItemFilterContext.class)) {
            ctx = ctx.getParent();
        }

        return AxiomQueryParser.ItemFilterContext.class.cast(ctx);
    }

    /**
     * Find reference object of node which present change of definition in AST (rootContext or subfilterSpecContext).
     * @param ctx
     * @return reference object as identifier for {@link AxiomQueryContentAssistantVisitor#itemDefinitions}
     */
    private RuleContext findIdentifierDefinition(RuleContext ctx) {
        while (!AxiomQueryParser.RootContext.class.equals(ctx.getClass())) {
            if (ctx instanceof AxiomQueryParser.SubfilterSpecContext) break;
            ctx = ctx.getParent();
        }

        return ctx;
    }

    private void updateItemDefinitionForContext(RuleContext key, Definition itemDefinition) {
        itemDefinitions.put(key, itemDefinition);
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
     * Method find all possible following rules based on the cursor position.
     * @param atn rule network
     * @param positionCtx position node
     * @return expected list of pair token with rule ctx
     */
    private List<TokenWithCtx> getExpectedTokenCtxByPositionCtx(ATN atn, PositionContext positionCtx) {
        List<TokenWithCtx> expected = new ArrayList<>();
        ParseTree node = positionCtx.node().getChild(positionCtx.cursorIndex());

        if (node instanceof TerminalNode terminalNode) {
            // if position token is SEPARATOR then find following rule of previous token
            if (terminalNode.getSymbol().getType() == AxiomQueryLexer.SEP) {
                node = positionCtx.node().getChild(positionCtx.cursorIndex() - 1);
                if (node instanceof TerminalNode) {
                    node = node.getParent();
                }
            } else {
                node = terminalNode.getParent();
            }
        }

        if (node instanceof RuleContext ruleContext) {
            findTokensWithRuleCtxInATN(atn, ruleContext, getTerminalNode(positionCtx.node().getChild(positionCtx.cursorIndex())), expected);
        }

        return expected;
    }

    /**
     * The method fill the list of expected tokens with the appropriate rules context in ATN network.
     * first analyzes position context rule -> find all following rules
     * next analyzes all following rules and to collect all excepted tokens with rules ctx
     * @param atn ANTLR4 ATN network
     * @param context position context on the based on the which search following rules
     * @param terminal following terminal after position context (usually SEPARATOR)
     * @param expected list to fill expected tokens with rule context
     */
    private void findTokensWithRuleCtxInATN(ATN atn, RuleContext context, TerminalNode terminal, List<TokenWithCtx> expected) {
        Stack<ATNState> states = new Stack<>(), passedStates = new Stack<>();
        Stack<Integer> rules = new Stack<>();
        ATNState nextState;

        if (context.invokingState == -1) {
            states.push(atn.states.get(0));
        } else {
            states.push(atn.states.get(context.invokingState));
        }

        while (!states.isEmpty()) {
            nextState = states.pop();
            passedStates.push(nextState);

            for (Transition transition : nextState.getTransitions()) {
                if (transition instanceof AtomTransition atomTransition) {
                    if (atomTransition.label == terminal.getSymbol().getType()) {
                        states.push(atomTransition.target);
                    }
                } else if (transition instanceof RuleTransition ruleTransition) {
                    if (context.getRuleIndex() == ruleTransition.ruleIndex) {
                        states.push(ruleTransition.followState);
                    }

                    if (ruleTransition.ruleIndex == AxiomQueryParser.RULE_path || ruleTransition.ruleIndex == AxiomQueryParser.RULE_matchingRule) {
                        if (terminal.getSymbol().getType() == AxiomQueryLexer.IDENTIFIER) {
                            rules.push(ruleTransition.ruleIndex);
                        }
                    } else {
                        rules.push(ruleTransition.ruleIndex);
                    }
                } else {
                    // check looping
                    if (!passedStates.contains(transition.target) && nextState.getClass() != RuleStopState.class) {
                        states.push(transition.target);
                    }
                }
            }
        }

        rules.forEach(followingRule -> {
            states.push(atn.ruleToStartState[followingRule]);
        });

        rules.clear();

        while (!states.isEmpty()) {
            nextState = states.pop();
            passedStates.push(nextState);

            if (!rules.contains(nextState.ruleIndex)) {
                rules.push(nextState.ruleIndex);
            }

            for (Transition transition : nextState.getTransitions()) {
                if (transition instanceof AtomTransition atomTransition) {
                    TokenWithCtx token = new TokenWithCtx(atomTransition.label, null);
                    // currently to need rules context only for IDENTIFIER token
                    if (atomTransition.label == AxiomQueryLexer.IDENTIFIER) {
                        token = token.withRules(rules);
                    }

                    if (atomTransition.label != -1 && !(expected.contains(token))) {
                        expected.add(token);
                    }
                } else if (transition instanceof SetTransition setTransition) {
                    setTransition.set.getIntervals().forEach(interval -> {
                        for (int i = interval.a; i <= interval.b; i++) {
                            expected.add(new TokenWithCtx(i, null));
                        }
                    });
                } else if (transition instanceof RuleTransition ruleTransition) {
                    if (context instanceof AxiomQueryParser.FilterContext) {
                        if (context instanceof AxiomQueryParser.AndFilterContext || context instanceof AxiomQueryParser.OrFilterContext) {
                            if (ruleTransition.ruleIndex != AxiomQueryParser.RULE_negation) {
                                states.push(ruleTransition.target);
                            }
                        } else {
                            states.push(ruleTransition.followState);
                        }
                    } else {
                        states.push(ruleTransition.target);
                    }
                } else {
                    // check looping
                    if (!passedStates.contains(transition.target) && nextState.getClass() != RuleStopState.class) {
                        states.push(transition.target);
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
