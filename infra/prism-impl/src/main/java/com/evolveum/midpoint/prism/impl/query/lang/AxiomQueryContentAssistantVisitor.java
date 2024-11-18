package com.evolveum.midpoint.prism.impl.query.lang;

import java.util.*;

import com.evolveum.axiom.lang.antlr.*;
import com.evolveum.midpoint.prism.*;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.prism.query.Suggestion;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParserBaseVisitor;
import com.evolveum.midpoint.prism.path.ItemPath;

import com.google.common.base.Strings;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.misc.Interval;
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

    /**
     * ANTLR4 ATN network provided by the parser
     */
    private final ATN atn;

    /**
     * PRISM context for get schema context and other helper methods.
     */
    private final PrismContext prismContext;

    /**
     * Table hash for mapping PRISM definition according AST (primary for itemFilter, filter and root context)
     */
    private final HashMap<ParseTree, Definition> itemDefinitions = new HashMap<>();

    /**
     * Error list for save semantic and syntax errors also
     */
    private final List<AxiomQueryError> errorList = new ArrayList<>();

    /**
     * Number of position cursor (start from 1 -> one character is number of position 1)
     */
    private final int positionCursor;

    /**
     * Terminal of cursor position
     */
    private TerminalNode positionTerminal;

    /**
     * Definition in place of cursor position / PRISM definition for root context (initial state), primary from schema context annotation
     */
    private Definition positionDefinition;

    /**
     * variable define if processing flow is before or after cursorPosition
     */
    private boolean beforeCursorPosition;

    /**
     * Allowed types of class for key in hash table {@link this#itemDefinitions}
     */
    private static final List<Class<? extends ParseTree>> CLAZZ_OF_KEY = List.of(AxiomQueryParser.RootContext.class, AxiomQueryParser.SubfilterSpecContext.class, AxiomQueryParser.ItemFilterContext.class);

    public AxiomQueryContentAssistantVisitor(PrismContext prismContext, Definition rootItem) {
        this(prismContext, rootItem, null, 0);
    }

    public AxiomQueryContentAssistantVisitor(PrismContext prismContext, Definition rootItem, ATN atn, int positionCursor) {
        this.prismContext = prismContext;
        this.positionDefinition = rootItem;
        this.atn = atn;
        this.positionCursor = positionCursor;
        this.beforeCursorPosition = true;
    }

// --------------------------------- Error Handling & Semantics Validation ---------------------------------- //

    @Override
    public Object visitRoot(AxiomQueryParser.RootContext ctx) {
        ParseTree positionNode = findNodeLeftOfCursor(ctx, positionCursor);

        if (positionNode != null) {
            positionTerminal = getTerminalNode(positionNode);
        } else {
            errorList.add(new AxiomQueryError(
                    positionCursor, positionCursor, positionCursor, positionCursor,
                    "Cursor is outside the query."
            ));
        }

        itemDefinitions.clear();
        // Initialization root definition
        itemDefinitions.put(ctx, positionDefinition);
        return super.visitRoot(ctx);
    }

    @Override
    public Object visitIdentifierComponent(AxiomQueryParser.IdentifierComponentContext ctx) {
        // '#' can use only for container
        errorRegister((itemDefinitions.get(findIdentifierOfDefinition(ctx)) instanceof PrismContainerDefinition<?>), ctx,
                "Invalid '%s' in identifier component.", ctx.getText());
        return super.visitIdentifierComponent(ctx);
    }

    @Override
    public Object visitSubfilterOrValue(AxiomQueryParser.SubfilterOrValueContext ctx) {
        var itemFilter = findItemFilterContextInTree(ctx);

        if (itemFilter != null && itemFilter.filterName() != null && Filter.Name.MATCHES.getLocalPart().equals(itemFilter.filterName().getText())) {
            if (ctx.subfilterSpec() != null) {
                updateDefinitionByContext(ctx.subfilterSpec(), itemDefinitions.get(findIdentifierOfDefinition(ctx)));
            }
        }

        return super.visitSubfilterOrValue(ctx);
    }

    @Override
    public Object visitSelfPath(AxiomQueryParser.SelfPathContext ctx) {
        var itemFilter = findIdentifierOfDefinition(ctx);

        // if query started with selfPath use definition from root or not then using definition from subFilterSpec
        if (findIdentifierOfDefinition(itemFilter.getParent()) instanceof AxiomQueryParser.RootContext) {
            updateDefinitionByContext(ctx, findParentContextDefinition(ctx));
        } else {
            updateDefinitionByContext(ctx, findParentContextDefinition(ctx, AxiomQueryParser.SubfilterSpecContext.class), List.of(AxiomQueryParser.ItemFilterContext.class, AxiomQueryParser.SubfilterSpecContext.class));
        }

        return super.visitSelfPath(ctx);
    }

    @Override
    public Object visitDereferenceComponent(AxiomQueryParser.DereferenceComponentContext ctx) {
        var def = findParentContextDefinition(ctx, AxiomQueryParser.ItemFilterContext.class);
        // Is first ItemComponent?
        if (getTerminalNode(ctx).getSymbol().equals(ctx.getParent().start)) {
            def = findParentContextDefinition(ctx);
        }

        if (def instanceof PrismReferenceDefinition referenceDefinition) {
            if (referenceDefinition.getTargetObjectDefinition() != null) {
                updateDefinitionByContext(ctx, prismContext.getSchemaRegistry().findObjectDefinitionByType(referenceDefinition.getTargetTypeName()));
            } else if (referenceDefinition.getTargetTypeName() != null) {
                updateDefinitionByContext(ctx, prismContext.getSchemaRegistry().findObjectDefinitionByType(referenceDefinition.getTargetTypeName()));
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
        Map<String, String> infraFilters;
        var identifier = ctx.getText();

        if ((infraFilters = FilterProvider.findFilterByItemDefinition(null,
                ctx.getRuleIndex())).containsKey(identifier)
                || Filter.ReferencedKeyword.TARGET_TYPE.getName().equals(identifier)
                || Filter.ReferencedKeyword.TARGET.getName().equals(identifier)
                || Filter.ReferencedKeyword.RELATION.getName().equals(identifier)
                || Filter.ReferencedKeyword.OID.getName().equals(identifier)) {
            return super.visitItemComponent(ctx);
        }

        var itemFilterContext = findItemFilterContextInTree(ctx);

        if (infraFilters.containsKey(itemFilterContext.getChild(0).getText())) {
            if (Filter.Infra.TYPE.getName().equals(itemFilterContext.getChild(0).getText())) {
                var def = prismContext.getSchemaRegistry().findComplexTypeDefinitionByType(new QName(identifier));
                updateDefinitionByContext(ctx, def, List.of(AxiomQueryParser.ItemFilterContext.class, AxiomQueryParser.SubfilterSpecContext.class));
                errorRegister(def != null, ctx, "Invalid infra type '%s'.", identifier);
            } else if (Filter.Infra.PATH.getName().equals(itemFilterContext.getChild(0).getText())) {
                var key = findIdentifierOfDefinition(ctx);
                var def = itemDefinitions.get(key);

                if (def == null) {
                    def = itemDefinitions.get(findIdentifierOfDefinition(key.getParent()));
                }

                def = findDefinition(def, new QName(identifier));
                updateDefinitionByContext(ctx, def,
                        List.of(AxiomQueryParser.ItemFilterContext.class, AxiomQueryParser.SubfilterSpecContext.class));
                errorRegister(def != null, ctx,
                        "Invalid infra path '%s'.", identifier);
            } else if (Filter.Infra.RELATION.getName().equals(itemFilterContext.getChild(0).getText())) {
                // TODO @relation infra filter
            }
        } else if (isTargetTypeItemFilter(itemFilterContext) || isTypeItemFilter(itemFilterContext)) {
            PrismObjectDefinition<?> objectTypeDefinition = prismContext.getSchemaRegistry().findObjectDefinitionByType(prismContext.getDefaultReferenceTargetType());
            List<TypeDefinition> objectSubTypes = new ArrayList<>(prismContext.getSchemaRegistry().findTypeDefinitionByCompileTimeClass(objectTypeDefinition.getCompileTimeClass(), TypeDefinition.class).getStaticSubTypes());

            objectSubTypes.stream().map(item -> {
                if (item.getTypeName().getLocalPart().equals(identifier)) return item;
                else {
                    var subItem = item.getStaticSubTypes().stream().filter(sub -> sub.getTypeName().getLocalPart().equals(identifier)).findFirst();
                    if (subItem.isPresent()) return subItem.get();
                }

                return null;
            }).filter(Objects::nonNull).findFirst().ifPresent(
                    targetTypeDefinition -> {
                        updateDefinitionByContext(ctx, targetTypeDefinition, List.of(AxiomQueryParser.ItemFilterContext.class, AxiomQueryParser.SubfilterSpecContext.class));
                    }
            );
            errorRegister(itemDefinitions.get(findIdentifierOfDefinition(ctx)) != null, ctx, "Invalid type '%s'.", identifier);
        } else if (isRelationItemFilter(itemFilterContext)) {
            // TODO relation semantic control
        } else if (isOidItemFilter(itemFilterContext)) {
            // TODO oid semantic control
        } else {
            var def = findParentContextDefinition(ctx, AxiomQueryParser.ItemFilterContext.class);
            // Is first ItemComponent
            if (getTerminalNode(ctx).getSymbol().equals(ctx.getParent().start)) {
                def = findParentContextDefinition(ctx);
            }

            def = findDefinition(def, new QName(identifier));
            updateDefinitionByContext(ctx, def);
            errorRegister(def != null, ctx,
                    "Invalid item component '%s' definition.", identifier);
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
        var itemFilterContext = findItemFilterContextInTree(ctx);
        var itemDefinition = itemDefinitions.get(findIdentifierOfDefinition(ctx));

        if (Arrays.stream(Filter.Infra.values())
                .anyMatch(infra -> infra.getName().equals(itemFilterContext.getChild(0).getText()))
                || Arrays.stream( Filter.ReferencedKeyword.values())
                .anyMatch(infra -> infra.getName().equals(itemFilterContext.getChild(0).getText()))) {
            errorRegister(Filter.Alias.EQUAL.getName().equals(ctx.getText()), ctx,
                    "Invalid '%s' filter. Only the assignment sign (=) is correct for '%s'.", ctx.getText(), itemFilterContext.getChild(0).getText());
        } else {
            if (itemFilterContext.getChild(0) instanceof AxiomQueryParser.SelfPathContext) {
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
        var itemFilterContext = findItemFilterContextInTree(ctx);
        if (Arrays.stream(Filter.Infra.values())
                .anyMatch(infra -> infra.getName().equals(itemFilterContext.getChild(0).getText()))
                || Arrays.stream(Filter.ReferencedKeyword.values())
                .anyMatch(infra -> infra.getName().equals(itemFilterContext.getChild(0).getText()))) {
            errorRegister(Filter.Alias.EQUAL.getName().equals(ctx.getText()), ctx,
                    "Invalid '%s' filter alias. Only the assignment sign (=) is correct for %s.", ctx.getText(), itemFilterContext.getChild(0).getText());
        } else {
            if (itemFilterContext.getChild(0) instanceof AxiomQueryParser.SelfPathContext) {
                errorRegister((FilterProvider.findFilterByItemDefinition(
                                itemDefinitions.get(findIdentifierOfDefinition(ctx)), ctx.getRuleIndex()).containsValue(ctx.getText())), ctx,
                        "Invalid '%s' filter alias for self path.", ctx.getText());
            } else {
                errorRegister((FilterProvider.findFilterByItemDefinition(
                                itemDefinitions.get(findIdentifierOfDefinition(ctx)), ctx.getRuleIndex()).containsValue(ctx.getText())), ctx,
                        "Invalid '%s' filter alias.", ctx.getText());
            }
        }
        return super.visitFilterNameAlias(ctx);
    }

    @Override
    public Object visitErrorNode(ErrorNode node) {
        updateBeforeCursorPosition(node);
        if (node.getParent() instanceof AxiomQueryParser.PathContext) {
            if (!node.getText().equals(Filter.Token.SLASH.getName())) {
                var definition = itemDefinitions.get(findItemFilterContextInTree(node));
                if (definition == null) {
                    definition = itemDefinitions.get(findIdentifierOfDefinition(node));
                }
                updateDefinitionByContext(node, findDefinition(definition, new QName(node.getText())));
            }
        }

        return super.visitErrorNode(node);
    }

    @Override
    public Object visitTerminal(TerminalNode node) {
        updateBeforeCursorPosition(node);
        return super.visitTerminal(node);
    }

    /**
     * Find the closest item filter context in the analysis tree from the input node.
     * @param node
     * @return found itemFilter.
     */
    private AxiomQueryParser.ItemFilterContext findItemFilterContextInTree(ParseTree node) {
        if (node == null) return null;

        if (node instanceof AxiomQueryParser.ItemFilterContext itemFilterContext) return itemFilterContext;

        // look down in branch form input node
        while (node != null && !(node instanceof AxiomQueryParser.ItemFilterContext) && !(node instanceof TerminalNode)) {
            node = node.getChild(node.getChildCount() - 1);
        }

        int index;
        //  look top in branch & look to deep in parser tree form input node
        while (node != null && !(node instanceof AxiomQueryParser.ItemFilterContext)) {
            if (node.getParent() instanceof AxiomQueryParser.RootContext || node.getParent() instanceof AxiomQueryParser.FilterContext) {
                // -1 because need previous branch from node
                index = getChildIndexInParent(node, node.getParent());

                while (index > 0 && !(node instanceof AxiomQueryParser.ItemFilterContext)) {
                    // Loop down searching until find itemFilter
                    while (node != null && !node.getClass().equals(AxiomQueryParser.ItemFilterContext.class)) {
                        node = node.getParent().getChild(index - 1);
                        if (node != null && node.getChildCount() > 0) {
                            node = node.getChild(node.getChildCount() - 1);
                        } else {
                            index = index -1;
                        }
                    }
                    index = index -1;
                }
            }

            if (node != null && !(node instanceof AxiomQueryParser.ItemFilterContext)) {
                node = node.getParent();
            }
        }

        return (AxiomQueryParser.ItemFilterContext) node;
    }


    /**
     * Method return last terminal node from branch node.
     * @param parseTree
     * @return
     */
    private TerminalNode getTerminalNode(ParseTree parseTree) {
        if (parseTree instanceof TerminalNode terminalNode) {
            return terminalNode;
        }

        if (parseTree != null) {
            while (parseTree.getChildCount() > 0) {
                parseTree = parseTree.getChild(parseTree.getChildCount() - 1);

                if (parseTree instanceof TerminalNode terminalNode) {
                    return terminalNode;
                }
            }
        }

        return null;
    }

    /**
     * Find definition of schema context.
     * @param parentDefinition
     * @param name
     * @return Definition found or null
     */
    private Definition findDefinition(Definition parentDefinition, QName name) {
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
     * Find first definition of parent identifier.
     * @param ctx node from which the identifier is searched towards the parents in the AST
     */
    private Definition findParentContextDefinition(ParseTree ctx) {
        var identifier = findIdentifierOfDefinition(ctx);
        var definition = itemDefinitions.get(identifier);

        while (definition == null && identifier != null) {
            identifier = identifier.getParent();
            definition = itemDefinitions.get(findIdentifierOfDefinition(identifier));
        }

        return definition;
    }

    /**
     * Find definition of parent identifier by class of identifier.
     * @param ctx node from which the identifier is searched towards the parents in the AST
     * @param clazz type of class identifier AST
     * @return
     */
    private Definition findParentContextDefinition(ParseTree ctx, Class<? extends ParseTree> clazz) {
        if (!CLAZZ_OF_KEY.contains(clazz)) return null;

        while (!(clazz.isInstance(ctx)) && ctx != null) {
            ctx = findIdentifierOfDefinition(ctx.getParent());
        }

        return itemDefinitions.get(ctx);
    }

    private boolean isTargetTypeItemFilter(AxiomQueryParser.ItemFilterContext itemFilterContext) {
        return Filter.ReferencedKeyword.TARGET_TYPE.getName().equals(itemFilterContext.getChild(0).getText());
    }

    private boolean isTypeItemFilter(AxiomQueryParser.ItemFilterContext itemFilterContext) {
        return itemFilterContext.getChild(2) != null && Filter.Name.TYPE.getName().getLocalPart().equals(itemFilterContext.getChild(2).getText());
    }

    private boolean isRelationItemFilter(AxiomQueryParser.ItemFilterContext itemFilterContext) {
        return Filter.ReferencedKeyword.RELATION.getName().equals(itemFilterContext.getChild(0).getText());
    }

    private boolean isOidItemFilter(AxiomQueryParser.ItemFilterContext itemFilterContext) {
        return Filter.ReferencedKeyword.OID.getName().equals(itemFilterContext.getChild(0).getText());
    }

    /**
     * Find reference object of node which present change of definition in AST (rootContext or subFilterSpecContext).
     * @param node
     * @return reference object as identifier for {@link AxiomQueryContentAssistantVisitor#itemDefinitions}
     */
    private ParseTree findIdentifierOfDefinition(ParseTree node) {
        if (node == null) return null;

        while (!(node instanceof AxiomQueryParser.RootContext)) {
            if (node instanceof AxiomQueryParser.SubfilterSpecContext) break;
            if (node instanceof AxiomQueryParser.ItemFilterContext) break;
            node = node.getParent();
        }

        return node;
    }

    private ParseTree findIdentifierOfDefinition(ParseTree node, Class<? extends ParseTree> clazz) {
        if (node == null || !CLAZZ_OF_KEY.contains(clazz)) return null;

        while (!(clazz.isInstance(node)) && node != null) {
            node = node.getParent();
        }

        return node;
    }

    /**
     * Method do update definition in itemDefinitions table hash and to assigment definition for positionDefinition based context.
     * @param node
     * @param definition
     */
    private void updateDefinitionByContext(ParseTree node, Definition definition) {
        itemDefinitions.put(findIdentifierOfDefinition(node), definition);
        // Set definition of position cursor for code completions
//        setPositionDefinition(node, definition);
    }

    /**
     * Method do update definition in itemDefinitions table hash on different levels of the identifier of the AST.
     * @param node key of hash table
     * @param definition new definition
     * @param classes type of class identifier which represent levels of identifier in the AST
     */
    private void updateDefinitionByContext(ParseTree node, Definition definition, List<Class<? extends ParseTree>> classes) {
        for (Class<? extends ParseTree> clazz : classes) {
            var key = findIdentifierOfDefinition(node, clazz);
            if (key != null) {
                updateDefinitionByContext(key, definition);
            }
        }
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

    public List<AxiomQueryError> getErrorList() {
        return errorList;
    }

// --------------------------------- Code Completions --------------------------------------------------------- //
    /**
     * Generate code completions suggestion for AxiomQuery language by position context.
     * @return List {@link Suggestion}
     */
    public List<Suggestion> generateSuggestions() {
        List<Suggestion> suggestions = new ArrayList<>();
        boolean isSelfPath = false;
        boolean isSelfDereference = false;

        if (positionTerminal != null) {
            for (TerminalWithContext terminal : getExpectedTokenWithCtxByPosition(atn, positionTerminal)) {
                if (terminal.type() == AxiomQueryParser.IDENTIFIER) {
                    if (terminal.rules() != null && terminal.rules().contains(AxiomQueryParser.RULE_filterName)) {
                        FilterProvider.findFilterByItemDefinition(positionDefinition, AxiomQueryParser.RULE_filterName).forEach((name, alias) -> {
                            suggestions.add(new Suggestion(name, alias, -1));
                        });
                    } else if (terminal.rules() != null && terminal.rules().contains(AxiomQueryParser.RULE_path)) {
                        processingDefinitionToPathSuggestion(positionDefinition, suggestions);
                    } else if (terminal.rules() != null && terminal.rules().contains(AxiomQueryParser.RULE_matchingRule)) {
                        // matching rule filter
                    } else if (terminal.rules() != null && terminal.rules().contains(AxiomQueryParser.RULE_subfilterOrValue)) {
                        // generate value for IDENTIFIER value path maybe???
                    }
                } else if (terminal.type() == AxiomQueryParser.NOT_KEYWORD) {
                    suggestions.add(new Suggestion(Filter.Name.NOT.name().toLowerCase(), Filter.Name.NOT.name().toLowerCase(), -1));
                } else if (terminal.type() == AxiomQueryParser.AND_KEYWORD) {
                    suggestions.add(new Suggestion(Filter.Name.AND.name().toLowerCase(), Filter.Name.AND.name().toLowerCase(), -1));
                } else if (terminal.type() == AxiomQueryParser.OR_KEYWORD) {
                    suggestions.add(new Suggestion(Filter.Name.OR.name().toLowerCase(), Filter.Name.OR.name().toLowerCase(), -1));
                } else if (terminal.type() == AxiomQueryParser.STRING_MULTILINE ||
                        terminal.type() == AxiomQueryParser.STRING_DOUBLEQUOTE ||
                        terminal.type() == AxiomQueryParser.STRING_SINGLEQUOTE ||
                        terminal.type() == AxiomQueryParser.STRING_BACKTICK_TRIQOUTE ||
                        terminal.type() == AxiomQueryParser.STRING_BACKTICK
                ) {
                    suggestions.add(new Suggestion("'", "String value", -1));
                    suggestions.add(new Suggestion("\"", "String value", -1));
                } else {
                    if (isSelfPath || isSelfDereference) {
                        if (terminal.type() != AxiomQueryParser.EQ &&
                                terminal.type() != AxiomQueryParser.NOT_EQ &&
                                terminal.type() != AxiomQueryParser.GT &&
                                terminal.type() != AxiomQueryParser.GT_EQ &&
                                terminal.type() != AxiomQueryParser.LT &&
                                terminal.type() != AxiomQueryParser.LT_EQ) {
                            suggestions.add(suggestionFromVocabulary(terminal, -1));
                        }
                    } else {
                        suggestions.add(suggestionFromVocabulary(terminal, -1));
                    }
                }
            }
        }

        return suggestions;
    }

    /**
     * Method find all possible following rules based on the cursor position.
     * @param atn rule ATN network
     * @param positionTerminal terminal of cursor position
     * @return expected list of pair token with rule ctx
     */
    private List<TerminalWithContext> getExpectedTokenWithCtxByPosition(@NotNull ATN atn, @NotNull TerminalNode positionTerminal) {
        List<TerminalWithContext> expected = new ArrayList<>();
        ParseTree node = positionTerminal;
        TerminalWithContext previousTerminalWithContext = null;
        int lastProcessingRuleIndex = -1;

        if (positionTerminal.getSymbol().getType() == AxiomQueryParser.SEP) {
            var previous = getTerminalNode(getPreviousNode(positionTerminal));

            if (previous != null) {
                previousTerminalWithContext = new TerminalWithContext(previous.getSymbol().getType(), null);
                node = previous;
            }
        }

        do {
            node = node.getParent();



            if (node instanceof RuleContext ruleContext) {
                transitionATN(atn, new TerminalWithContext(
                        positionTerminal.getSymbol().getType(),
                        ruleContext,
                        null,
                        previousTerminalWithContext,
                        null
                ), lastProcessingRuleIndex, expected);

                lastProcessingRuleIndex = ruleContext.getRuleIndex();
            }
        } while (!(node instanceof AxiomQueryParser.RootContext));

        return expected;
    }

    /**
     * The method fill the list of expected tokens with the appropriate rules context in ATN network.
     * first analyzes position context rule -> find all following rules
     * next analyzes all following rules and to collect all excepted tokens with rules ctx
     * @param atn ANTLR4 ATN network
     * @param terminal position terminal with context
     * @param expected list to fill expected tokens with rule context
     */
    private void transitionATN(@NotNull ATN atn,
            @NotNull TerminalWithContext terminal,
            int lastProcessingRuleIndex,
            @NotNull List<TerminalWithContext> expected
    ) {
        Stack<ATNState> states = new Stack<>(), passedStates = new Stack<>();
        Stack<Integer> rules = new Stack<>();
        ATNState currentState;

        states.push(atn.states.get(terminal.context().invokingState == -1 ? 0 : terminal.context().invokingState));

        while (!states.isEmpty()) {
            currentState = states.pop();
            passedStates.push(currentState);

            if (!rules.contains(currentState.ruleIndex)) {
                rules.push(currentState.ruleIndex);
            }

            for (Transition transition : currentState.getTransitions()) {
                if (transition instanceof AtomTransition atomTransition) {
                    if (terminal.type() == atomTransition.label) {
                        states.push(atomTransition.target);
                    } else {
                        registerExpectedTokens(atomTransition.label, rules, expected);
                    }
                } else if (transition instanceof SetTransition setTransition) {
                    setTransition.set.getIntervals().forEach(interval -> {
                        if (intervalContainsToken(interval, terminal.type()) && terminal.context().getRuleIndex() == setTransition.target.ruleIndex) {
                            states.push(setTransition.target);
                        } else {
                            for (int i = interval.a; i <= interval.b; i++) {
                                registerExpectedTokens(i, rules, expected);
                            }
                        }
                    });
                } else if (transition instanceof RuleTransition ruleTransition) {
                    if (ruleTransition.target.ruleIndex == terminal.context().getRuleIndex()) {
                        if (AxiomQueryParser.SEP != terminal.type()) {
                            states.push(ruleTransition.target);
                        }
                        states.push(ruleTransition.followState);
                    } else {
                        if (ruleTransition.target.ruleIndex != lastProcessingRuleIndex) {
                            states.push(ruleTransition.target);
                            lastProcessingRuleIndex = -1;
                        } else {
                            states.push(ruleTransition.followState);
                        }
                    }
                } else {
                    if (!passedStates.contains(transition.target) && !(currentState instanceof RuleStopState)) {
                        states.push(transition.target);
                    }
                }
            }
        }
    }

    /**
     * Method find following rules by cursor terminal with context in ATN network.
     * @param atn ATN network of rules.
     * @param terminal terminal with context from place of cursor
     * @return
     */
    private List<ATNState> findFollowingRules(@NotNull ATN atn, @NotNull TerminalWithContext terminal) {
        Stack<ATNState> states = new Stack<>(), passedStates = new Stack<>();
        List<ATNState> followingRules = new ArrayList<>();
        ATNState currentState;

        states.push(atn.states.get(terminal.context().invokingState == -1 ? 0 : terminal.context().invokingState));

        while (!states.isEmpty()) {
            currentState = states.pop();
            passedStates.push(currentState);

            for (Transition transition : currentState.getTransitions()) {
                if (transition instanceof AtomTransition atomTransition) {
                    if (terminal.type() == atomTransition.label) {
                        states.push(atomTransition.target);
                    }
                } else if (transition instanceof SetTransition setTransition) {
                    setTransition.set.getIntervals().forEach(interval -> {
                        if (intervalContainsToken(interval, terminal.type())) {
                            states.push(setTransition.target);
                        }
                    });
                } else if (transition instanceof RuleTransition ruleTransition) {
                    if (terminal.context().getRuleIndex() == ruleTransition.target.ruleIndex) {
                        if (terminal.type() != AxiomQueryParser.SEP) {
                            followingRules.add(ruleTransition.target);
                        }
                        states.push(ruleTransition.followState);
                    } else {
                        if (Arrays.stream(Filter.RulesWithoutSep.values()).map(Filter.RulesWithoutSep::getIndex).toList().contains(ruleTransition.ruleIndex)) {
                            if (terminal.type() != AxiomQueryParser.SEP) {
                                followingRules.add(ruleTransition.target);
                            }
                        } else {
                            followingRules.add(ruleTransition.target);
                        }
                    }
                } else {
                    if (!passedStates.contains(transition.target) && !(currentState instanceof RuleStopState)) {
                        states.push(transition.target);
                    }
                }
            }
        }

        return followingRules;
    }

    /**
     * Generate path suggestion from schema definition.
     * @param definition
     * @param suggestions
     */
    private void processingDefinitionToPathSuggestion(Definition definition, List<Suggestion> suggestions) {
        if (definition instanceof PrismContainerDefinition<?> containerDefinition) {
            containerDefinition.getDefinitions().forEach(prop -> {
                suggestions.add(new Suggestion(prop.getItemName().getLocalPart(), prop.getDisplayName(), -1));
                if (prop instanceof PrismContainerDefinition<?> containerDefinition1) {
                    containerDefinition1.getDefinitions().forEach( o -> {
                        suggestions.add(new Suggestion(containerDefinition1.getItemName().getLocalPart() + "/" + o.getItemName().getLocalPart(), o.getTypeName().getLocalPart(), -1));
                    });
                }
            });
        } else if (definition instanceof PrismReferenceDefinition) {
            // selected tokens by semantics rules
            // suggestions.add(new Suggestion(referenceDefinition.getItemName().getLocalPart(), referenceDefinition.getTypeName().getLocalPart(), -1));
        } else if (definition instanceof ComplexTypeDefinition complexTypeDefinition) {
            complexTypeDefinition.getDefinitions().forEach(d -> {
                suggestions.add(new Suggestion(d.getItemName().getLocalPart(),  d.getTypeName().getLocalPart(), -1));
            });
        } else if (definition instanceof PrismPropertyDefinition<?>) {
            processingDefinitionToPathSuggestion(definition, suggestions);
        }
    }

    private Suggestion suggestionFromVocabulary(TerminalWithContext token, int priority) {
        // DisplayName (or LiteralName) is escaped with single qoutes, so we remove them
        var tokenValue = AxiomStrings.fromOptionallySingleQuoted(AxiomQueryParser.VOCABULARY.getDisplayName(token.type()));
        return new Suggestion(tokenValue, tokenValue, -1);
    }

    /**
     * Find node in parser tree which to content cursor by cursor position.
     * @param tree
     * @param position
     * @return the node in which it is located cursor or null
     */
    private ParseTree findNodeLeftOfCursor(ParseTree tree, int position) {
        if (tree instanceof TerminalNode terminalNode) {
            Token token = terminalNode.getSymbol();

            if (token.getStartIndex() <= position - 1 && token.getStopIndex() >= position - 1) {
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
     * Method to get the index of index node (ctx) in its parent's children list.
     * @param child
     * @param parent
     * @return
     */
    private int getChildIndexInParent(ParseTree child, ParseTree parent) {
        if (child == null || parent == null) return -1;

        for (int i = 0; i < parent.getChildCount(); i++) {
            ParseTree childAtIndex = parent.getChild(i);
            if (childAtIndex == child) {
                return i;
            }
        }

        return 0;
    }

    /**
     * Method find previous node form input node.
     * @param node
     * @return
     */
    private ParseTree getPreviousNode(ParseTree node) {
        if (node == null) return null;

        int index = node.getChildCount();

        while (node != null && node.getChildCount() <= 1) {
            index = getChildIndexInParent(node, node.getParent());
            node = node.getParent();
        }

        if (node == null || index == 0) {
            return null;
        }

        return node.getChild(index - 1);
    }

    /**
     * Method append token to expected tokens list.
     * @param label
     * @param rules
     * @param expected
     */
    private void registerExpectedTokens(int label, Stack<Integer> rules, List<TerminalWithContext> expected) {
        // currently to need rules context only for IDENTIFIER token
        var terminal = new TerminalWithContext(
                label,
                null,
                label == AxiomQueryParser.IDENTIFIER ? rules : null
        );

        if (label != -1 && !expected.contains(terminal)) {
            expected.add(terminal);
        }
    }

    /**
     * Method do find context of terminal (context of node in which existing terminal symbol in AST).
     * @param terminal
     * @return context of terminal
     */
    private RuleContext findContextOfTerminal(TerminalNode terminal) {
        if (terminal == null) return null;

        ParseTree node = terminal;
        int index, count;

        do {
            index = getChildIndexInParent(node, node.getParent());
            node = node.getParent();
            count = node.getChildCount();
        } while (count == 1 && !(node instanceof AxiomQueryParser.RootContext));

        var context = node.getChild(index);

        if (context instanceof TerminalNode) {
            context = context.getParent();
        } else {
            // if branch has one child it's need go to deep to tree for more specific context
            while (context.getChildCount() == 1 &&
                    (context instanceof AxiomQueryParser.ItemFilterContext || context instanceof AxiomQueryParser.FilterContext)) {
                context = context.getChild(0);
            }
        }

        return (RuleContext) context;
    }

    private void updateBeforeCursorPosition(TerminalNode node) {
        if (node.getSymbol().getStopIndex() >= positionCursor - 1) {
            beforeCursorPosition = false;
        }
    }

    /**
     * Method find out if interval exists token
     * @param interval interval set
     * @param index token index
     * @return
     */
    private boolean intervalContainsToken(Interval interval, int index) {
        for (int i = interval.a; i <= interval.b; i++) {
            if (i == index) return true;
        }

        return false;
    }
}
