package com.evolveum.midpoint.prism.impl.query.lang;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    }

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
    public Object visitInfraName(AxiomQueryParser.InfraNameContext ctx) {
         if (Filter.Infra.METADATA.getName().equals(ctx.getText())) {
            var def = prismContext.getSchemaRegistry().getValueMetadataDefinition();
            updateDefinitionByContext(ctx, def);
        }

        return super.visitInfraName(ctx);
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
        if (node.getParent() instanceof AxiomQueryParser.PathContext) {
            var def = itemDefinitions.get(findIdentifierOfDefinition(node));
            if (!node.getText().equals(Filter.Token.SLASH.getName())) {
                do {
                    def = findParentContextDefinition(findIdentifierOfDefinition(node));
                } while (def == null);

                updateDefinitionByContext(findIdentifierOfDefinition(node), findDefinition(def, new QName(node.getText())));
            }

            if (node.equals(positionTerminal)) {
                positionDefinition = itemDefinitions.get(findIdentifierOfDefinition(node));

                ParseTree parent = node.getParent();

                while (positionDefinition == null) {
                    positionDefinition = itemDefinitions.get(findIdentifierOfDefinition(parent));
                    if (!(parent instanceof AxiomQueryParser.RootContext)) {
                        parent = parent.getParent();
                    }
                }
            }
        }

        return super.visitErrorNode(node);
    }

    @Override
    public Object visitTerminal(TerminalNode node) {
        if (node.equals(positionTerminal)) {
            if (positionTerminal.getParent() instanceof AxiomQueryParser.RootContext) {
                positionDefinition = itemDefinitions.get(findIdentifierOfDefinition(getPreviousTerminal(positionTerminal)));
            } else {
                positionDefinition = itemDefinitions.get(findIdentifierOfDefinition(node));
            }

            ParseTree parent = node.getParent();

            while (positionDefinition == null) {
                positionDefinition = itemDefinitions.get(findIdentifierOfDefinition(parent));
                if (!(parent instanceof AxiomQueryParser.RootContext)) {
                    parent = parent.getParent();
                }
            }
        }

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
        } else if (parentDefinition instanceof PrismPropertyDefinition<?> prismPropertyDefinition) {
            return prismPropertyDefinition;
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
     * Method do update definition in itemDefinitions table hash.
     * @param node
     * @param definition
     */
    private void updateDefinitionByContext(ParseTree node, Definition definition) {
        itemDefinitions.put(findIdentifierOfDefinition(node), definition);
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

    /**
     * Generate code completion suggestions for AxiomQuery language by position context.
     * @return List {@link Suggestion}
     */
    public List<Suggestion> generateSuggestions() {
        List<Suggestion> suggestions = new ArrayList<>();

        if (positionTerminal != null) {
            ParseTree parentNode = positionTerminal.getParent();
            int[] positionTerminalItemPathOrFilterCtx = { -1 };

            for (TokenCustom token : getExpectedTokenWithCtxByPosition(atn, positionTerminal, positionTerminalItemPathOrFilterCtx)) {
                if (isInfraFilter(positionTerminal)) {
                    suggestions.add(new Suggestion(Filter.Alias.EQUAL.getName(), "", -1));
                    break;
                }

                if (token.type() != AxiomQueryParser.IDENTIFIER && token.type() == positionTerminal.getSymbol().getType()) {
                    continue;
                }

                if (token.type() == AxiomQueryParser.IDENTIFIER) {
                    if (token.identifierContext() == TokenCustom.IdentifierContext.FILTER_NAME) {
                        FilterProvider.findFilterByItemDefinition(positionDefinition, AxiomQueryParser.RULE_filterName).forEach((name, alias) -> {
                            suggestions.add(new Suggestion(name, alias, -1));
                        });
                    } else if (token.identifierContext() == TokenCustom.IdentifierContext.PATH) {
                        ParseTree infraName = findInfraName(positionTerminal);
                        if (infraName != null && (infraName.getText().equals(Filter.Infra.TYPE.getName())
                            || infraName.getText().equals(Filter.ReferencedKeyword.TARGET_TYPE.getName()))) {
                            prismContext.getSchemaRegistry().getSchemas().forEach(definition -> {
                                definition.getDefinitions().forEach(def -> {
                                    suggestions.add(new Suggestion(def.getTypeName().getLocalPart(), "Object Definitions", -1));
                                });
                            });
                        } else if (infraName != null && infraName.getChild(0).getText().equals(Filter.Infra.PATH.getName())) {
                            processingDefinitionToPathSuggestion(positionDefinition, null, positionTerminal, suggestions);
                        } else {
                            TerminalNode prevTerminal = getPreviousTerminal(positionTerminal);

                            if (positionTerminal.getSymbol().getType() == AxiomQueryParser.SEP
                            || positionTerminal.getSymbol().getType() == AxiomQueryParser.ROUND_BRACKET_LEFT
                            || positionTerminal.getSymbol().getType() == AxiomQueryParser.EOF
                            || positionTerminal.getSymbol().getType() == AxiomQueryParser.AT_SIGN
                            || Filter.Infra.METADATA.getName().contains(positionTerminal.getParent().getText())
                            || prevTerminal != null && Filter.Infra.METADATA.getName().contains(prevTerminal.getText())) {
                                for (Filter.Infra value : Filter.Infra.values()) {
                                    if (value == Filter.Infra.METADATA) {
                                        processingDefinitionToPathSuggestion(prismContext.getSchemaRegistry().getValueMetadataDefinition(), value, null, suggestions);
                                    } else {
                                        suggestions.add(new Suggestion(value.getName(), "Infra", -1));
                                    }
                                }
                            }

                            if (positionTerminalItemPathOrFilterCtx[0] == AxiomQueryParser.RULE_filterName
                                    || positionTerminalItemPathOrFilterCtx[0] == AxiomQueryParser.RULE_filterNameAlias) {
                                processingDefinitionToPathSuggestion(itemDefinitions.get(findIdentifierOfDefinition(parentNode, AxiomQueryParser.RootContext.class)), null, positionTerminal, suggestions);
                            } else {
                                processingDefinitionToPathSuggestion(positionDefinition, null, positionTerminal, suggestions);
                            }
                        }
                    } else if (token.identifierContext() == TokenCustom.IdentifierContext.MATCHING) {
                        suggestions.add(new Suggestion(Filter.PolyStringKeyword.MatchingRule.NORM_IGNORE_CASE.getName(), "Ignore case", -1));
                        suggestions.add(new Suggestion(Filter.PolyStringKeyword.MatchingRule.ORIG_IGNORE_CASE.getName(), "Ignore case", -1));
                        suggestions.add(new Suggestion(Filter.PolyStringKeyword.MatchingRule.STRICT_IGNORE_CASE.getName(), "Ignore case", -1));
                    }
                } else if (token.type() == AxiomQueryParser.NOT_KEYWORD) {
                    suggestions.add(new Suggestion(Filter.Name.NOT.name().toLowerCase(), Filter.Name.NOT.name().toLowerCase(), -1));
                } else if (token.type() == AxiomQueryParser.AND_KEYWORD) {
                    if (positionTerminal.getSymbol().getType() == AxiomQueryParser.SEP) {
                        suggestions.add(new Suggestion(Filter.Name.AND.name().toLowerCase(), Filter.Name.AND.name().toLowerCase(), -1));
                    }
                } else if (token.type() == AxiomQueryParser.OR_KEYWORD) {
                    if (positionTerminal.getSymbol().getType() == AxiomQueryParser.SEP) {
                        suggestions.add(new Suggestion(Filter.Name.OR.name().toLowerCase(), Filter.Name.OR.name().toLowerCase(), -1));
                    }
                }else if (token.type() == AxiomQueryParser.SLASH) {
                    if (!(positionDefinition instanceof PrismPropertyDefinition<?>)
                            && positionTerminal.getSymbol().getType() != AxiomQueryParser.SEP
                            && positionTerminalItemPathOrFilterCtx[0] == AxiomQueryParser.RULE_path) {
                        suggestions.add(suggestionFromVocabulary(token, -1));
                    }
                } else if (token.type() == AxiomQueryParser.SHARP) {
                    if (!(positionDefinition instanceof PrismPropertyDefinition<?>)
                            && !(positionDefinition instanceof PrismReferenceDefinition)
                            && positionTerminal.getSymbol().getType() != AxiomQueryParser.SEP
                            && positionTerminalItemPathOrFilterCtx[0] == AxiomQueryParser.RULE_path) {
                        suggestions.add(suggestionFromVocabulary(token, -1));
                    }
                } else if (token.type() == AxiomQueryParser.AT_SIGN) {
                    if (positionTerminal.getSymbol().getType() != AxiomQueryParser.IDENTIFIER) {
                        suggestions.add(suggestionFromVocabulary(token, -1));
                    }
                } else if (token.type() == AxiomQueryParser.DOLLAR) {
                    if (positionTerminal.getSymbol().getType() != AxiomQueryParser.SEP
                            && positionTerminalItemPathOrFilterCtx[0] == AxiomQueryParser.RULE_path) {
                        suggestions.add(suggestionFromVocabulary(token, -1));
                    }
                } else if (token.type() == AxiomQueryParser.COLON) {
                    if (positionTerminal.getSymbol().getType() != AxiomQueryParser.SEP
                            && positionTerminalItemPathOrFilterCtx[0] == AxiomQueryParser.RULE_path) {
                        suggestions.add(suggestionFromVocabulary(token, -1));
                    }
                } else if (token.type() == AxiomQueryParser.DOT) {
                    if (positionTerminal.getSymbol().getType() != AxiomQueryParser.IDENTIFIER) {
                        suggestions.add(suggestionFromVocabulary(token, -1));
                    }
                } else if (token.type() == AxiomQueryParser.PARENT) {
                    if (positionTerminal.getSymbol().getType() != AxiomQueryParser.IDENTIFIER) {
                        suggestions.add(suggestionFromVocabulary(token, -1));
                    }
                } else if (token.type() == AxiomQueryParser.STRING_SINGLEQUOTE) {
                    suggestions.add(new Suggestion("'", "String value", -1));
                } else if (token.type() == AxiomQueryParser.STRING_DOUBLEQUOTE) {
                    suggestions.add(new Suggestion("\"", "String value", -1));
                } else if (token.type() == AxiomQueryParser.SEP
                        || token.type() == AxiomQueryParser.ERRCHAR
                        || token.type() == AxiomQueryParser.QUESTION_MARK
                        || token.type() == AxiomQueryParser.INT
                        || token.type() == AxiomQueryParser.FLOAT
                        || token.type() == AxiomQueryParser.PLUS
                        || token.type() == AxiomQueryParser.STRING_BACKTICK_TRIQOUTE
                        || token.type() == AxiomQueryParser.STRING_BACKTICK
                        || token.type() == AxiomQueryParser.STRING_MULTILINE
                        || token.type() == AxiomQueryParser.TRUE
                        || token.type() == AxiomQueryParser.FALSE
                        || token.type() == AxiomQueryParser.NULL) {
                    // skip tokens which can not generate value
                    continue;
                } else {
                    suggestions.add(suggestionFromVocabulary(token, -1));
                }
            }
        }

        return suggestions;
    }

    /**
     * Method find all possible following tokens based on the cursor position.
     * @param atn rule ATN network
     * @param positionTerminal cursor position token
     * @return expected list of pair token with rule ctx
     */
    private Set<TokenCustom> getExpectedTokenWithCtxByPosition(
            @NotNull ATN atn,
            @NotNull TerminalNode positionTerminal,
            int[] positionTerminalContext
    ) {
        Set<TokenCustom> expected = new HashSet<>();
        if (positionTerminal.getParent() instanceof AxiomQueryParser.RootContext ruleContext && getPreviousNode(positionTerminal) == null) {
            traverseATN(atn.states.get(ruleContext.invokingState == -1 ? 0 : ruleContext.invokingState), null, -1, expected);
        } else {
            TerminalNode lastTerminal = positionTerminal.getSymbol().getType() == AxiomQueryParser.SEP ? getPreviousTerminal(positionTerminal) : positionTerminal;

            while (lastTerminal.getParent() instanceof AxiomQueryParser.RootContext){
                lastTerminal = getPreviousTerminal(lastTerminal);
            }

            if (lastTerminal.getParent() instanceof RuleContext ruleContext) {
                int completeRule = ruleContext.getRuleIndex();

                while (!(ruleContext instanceof AxiomQueryParser.RootContext)) {
                    if (ruleContext.getRuleIndex() == AxiomQueryParser.RULE_path) {
                        positionTerminalContext[0] = AxiomQueryParser.RULE_path;
                    } else if (ruleContext.getRuleIndex() == AxiomQueryParser.RULE_filterName
                            || ruleContext.getRuleIndex() == AxiomQueryParser.RULE_filterNameAlias) {
                        positionTerminalContext[0] = AxiomQueryParser.RULE_filterName;
                    }

                    if (completeRule != -1) {
                        completeRule = traverseATN(atn.states.get(ruleContext.invokingState == -1 ? 0 : ruleContext.invokingState), positionTerminal, completeRule, expected);
                    }

                    ruleContext = ruleContext.getParent();
                }
            }

            if (lastTerminal.getSymbol().getType() == AxiomQueryParser.AND_KEYWORD
                    || lastTerminal.getSymbol().getType() == AxiomQueryParser.OR_KEYWORD) {
                expected.add(new TokenCustom(AxiomQueryParser.DOT, null));
                expected.add(new TokenCustom(AxiomQueryParser.PARENT, null));
                expected.add(new TokenCustom(AxiomQueryParser.ROUND_BRACKET_LEFT, null));
                expected.add(new TokenCustom(AxiomQueryParser.AT_SIGN, null));
                expected.add(new TokenCustom(AxiomQueryParser.IDENTIFIER, TokenCustom.IdentifierContext.PATH));
            }
        }

        return expected;
    }

    /**
     * Method traverse to ATN network of rule from invoking state of position rule context
     * and to collect following tokens to expectedTokens Set by position type token.
     * Doesn't stop in RuleStopState but continue next following rule networks.
     * Method returns rule index of complete or incomplete return -1.
     *
     * @param invokeState the state from which the transition starts
     * @param positionTerminal terminal node found by position cursor in sentence of language
     * @param processedRule the index of the rule whose traversal completed if wasn't value is -1
     * @param expectedTokens set of expected tokens
     * @return rule index of complete or incomplete return -1
     */
    private int traverseATN(@NotNull ATNState invokeState,
            TerminalNode positionTerminal,
            int processedRule,
            @NotNull Set<TokenCustom> expectedTokens
    ) {
        Stack<ATNState> states = new Stack<>();
        Stack<Integer> followingStates = new Stack<>(), passedStates = new Stack<>();
        AtomicBoolean isConsumed = new AtomicBoolean(false);
        TokenCustom.IdentifierContext identifierContext = null;
        ATNState currentState = invokeState;
        states.push(currentState);

        while (!states.isEmpty()) {
            currentState = states.pop();
            passedStates.push(currentState.stateNumber);

            if (currentState.ruleIndex == AxiomQueryParser.RULE_path) {
                identifierContext = TokenCustom.IdentifierContext.PATH;
            } else if (currentState.ruleIndex == AxiomQueryParser.RULE_filterName) {
                identifierContext = TokenCustom.IdentifierContext.FILTER_NAME;
            } else if (currentState.ruleIndex == AxiomQueryParser.RULE_matchingRule) {
                identifierContext = TokenCustom.IdentifierContext.MATCHING;
            }

            for (Transition transition : currentState.getTransitions()) {
                if (transition instanceof AtomTransition atomTransition) {
                    if (positionTerminal != null && positionTerminal.getSymbol().getType() == atomTransition.label && !isConsumed.get()) {
                        pushState(atomTransition.target, states, passedStates);
                        isConsumed.set(true);
                    } else {
                        registerExpectedTokens(atomTransition.label, identifierContext, expectedTokens);
                        isConsumed.set(false);
                    }
                } else if (transition instanceof SetTransition setTransition) {
                    setTransition.set.getIntervals().forEach(interval -> {
                        if (positionTerminal != null && intervalContainsToken(interval, positionTerminal.getSymbol().getType()) && !isConsumed.get()) {
                            pushState(setTransition.target, states, passedStates);
                            isConsumed.set(true);
                        } else if (isConsumed.get() || positionTerminal != null && positionTerminal.getSymbol().getType() == AxiomQueryParser.IDENTIFIER) {
                            for (int i = interval.a; i <= interval.b; i++) {
                                registerExpectedTokens(i, null, expectedTokens);
                            }
                            isConsumed.set(false);
                        }
                    });
                } else if (transition instanceof RuleTransition ruleTransition) {
                    if (positionTerminal != null && positionTerminal.getSymbol().getType() != AxiomQueryParser.SEP && ruleTransition.ruleIndex == AxiomQueryParser.RULE_path) {
                        states.push(ruleTransition.target);
                    }

                    if (processedRule == AxiomQueryParser.RULE_matchingRule && positionTerminal != null && positionTerminal.getSymbol().getType() == AxiomQueryParser.SQUARE_BRACKET_LEFT) {
                        states.push(ruleTransition.target);
                    } else {
                        if (processedRule == ruleTransition.ruleIndex && ruleTransition.ruleIndex != AxiomQueryParser.RULE_subfilterSpec) {
                            states.push(ruleTransition.followState);
                        } else {
                            states.push(ruleTransition.target);
                            followingStates.push(ruleTransition.followState.stateNumber);
                        }
                    }
                } else {
                    if (transition.target instanceof RuleStopState ruleStopState) {
                        if (ruleStopState.ruleIndex == invokeState.ruleIndex && states.isEmpty()) {
                            return ruleStopState.ruleIndex;
                        }

                        for (Transition followingTransition : ruleStopState.getTransitions()) {
                            if (!followingStates.isEmpty() && followingStates.peek() == followingTransition.target.stateNumber) {
                                pushState(followingTransition.target, states, passedStates);
                                followingStates.pop();
                            }
                        }
                    } else {
                        pushState(transition.target, states, passedStates);
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Generate path suggestion by item definition.
     * @param definition item definition in the place of position cursor
     * @param option option for different definition e.g ValueMetadataType definition
     * @param positionTerminal position terminal node
     * @param suggestions suggestion list for append new suggestions generate from definition
     */
    private void processingDefinitionToPathSuggestion(Definition definition, Object option, TerminalNode positionTerminal, List<Suggestion> suggestions) {
        if (definition instanceof PrismContainerDefinition<?> containerDefinition) {
            String parentPath = determineParentPath(positionTerminal, containerDefinition, itemDefinitions, option);
            containerDefinition.getDefinitions().forEach(prop -> {
                suggestions.add(new Suggestion(parentPath + prop.getItemName().getLocalPart(), prop.getDisplayName() != null ? prop.getDisplayName() : "", -1));
                if (prop instanceof PrismContainerDefinition<?> containerDefinition1) {
                    containerDefinition1.getDefinitions().forEach( o -> {
                        suggestions.add(new Suggestion(parentPath + containerDefinition1.getItemName().getLocalPart() + "/" +
                                o.getItemName().getLocalPart(), o.getDisplayName() != null ? o.getDisplayName() : "", -1
                        ));
                    });
                }
            });
        } else if (definition instanceof PrismReferenceDefinition referenceDefinition) {
            suggestions.add(new Suggestion(referenceDefinition.getItemName().getLocalPart(), referenceDefinition.getDisplayName() != null ? referenceDefinition.getDisplayName() : "", -1));
        } else if (definition instanceof ComplexTypeDefinition complexTypeDefinition) {
            complexTypeDefinition.getDefinitions().forEach(d -> {
                suggestions.add(new Suggestion(d.getItemName().getLocalPart(), d.getDisplayName() != null ? d.getDisplayName() : "", -1));
            });
        } else if (definition instanceof PrismPropertyDefinition<?>) {
//            suggestions.add(new Suggestion(propertyDefinition.getItemName().getLocalPart(), propertyDefinition.getTypeName().getLocalPart(), -1));
        }
    }

    private Suggestion suggestionFromVocabulary(TokenCustom terminal, int priority) {
        // DisplayName (or LiteralName) is escaped with single quotes, so we remove them
        var tokenValue = AxiomStrings.fromOptionallySingleQuoted(AxiomQueryParser.VOCABULARY.getDisplayName(terminal.type()));

        if (terminal.type() == AxiomQueryParser.SEP) {
            return new Suggestion(" ", "Separator", -1);
        } else {
            return new Suggestion(tokenValue, tokenValue, -1);
        }
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

    private TerminalNode getPreviousTerminal(TerminalNode terminalNode) {
        return getTerminalNode(getPreviousNode(terminalNode));
    }

    /**
     * Method append token to expected tokens list.
     * @param label
     * @param identifierContext
     * @param expected
     */
    private void registerExpectedTokens(int label, TokenCustom.IdentifierContext identifierContext, Set<TokenCustom> expected) {
        if (label != -1) {
            if (!(label == AxiomQueryParser.IDENTIFIER && identifierContext == null)) {
                expected.add(new TokenCustom(
                        label,
                        identifierContext
                ));
            }
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

    private boolean isInfraFilter(TerminalNode terminal) {
        if (terminal == null) return false;

        String infraString = "";

        if (terminal.getSymbol().getType() == AxiomQueryParser.SEP) {
            TerminalNode infraFilter = getPreviousTerminal(terminal);
            TerminalNode atSign = getPreviousTerminal(infraFilter);

            if (atSign != null) {
                infraString = atSign.getSymbol().getText() + infraFilter.getSymbol().getText();
            }

            for (Filter.Infra value : Filter.Infra.values()) {
                if (value.getName().equals(infraString)) return true;
            }
        }

        return false;
    }

    private ParseTree findInfraName(TerminalNode terminal) {
        while (terminal != null) {
            terminal =  getPreviousTerminal(terminal);
            if (terminal != null && terminal.getParent() instanceof AxiomQueryParser.InfraNameContext infraNameContext) {
                return infraNameContext;
            }
        }

        return null;
    }

    private ParseTree findFilterName(TerminalNode terminal) {
        while (terminal != null) {
            terminal = getPreviousTerminal(terminal);
            if (terminal != null && terminal.getParent() instanceof AxiomQueryParser.FilterNameContext filterNameContext) {
                return filterNameContext;
            }
        }

        return null;
    }

    /**
     * Push state to state stack sort minimum value last & check of passed states
     * @param state
     * @param states
     * @param passedStates
     */
    private void pushState(ATNState state, Stack<ATNState> states, Stack<Integer> passedStates) {
        if (!passedStates.contains(state.stateNumber)) {
            if (states.isEmpty()) {
                states.push(state);
            } else {
                ATNState lastState = states.pop();

                if (lastState.stateNumber < state.stateNumber) {
                    states.push(state);
                    states.push(lastState);
                } else {
                    states.push(lastState);
                    states.push(state);
                }
            }
        }
    }

    private boolean isAtSign(TerminalNode terminalNode) {
        return terminalNode != null && terminalNode.getSymbol().getType() == AxiomQueryParser.AT_SIGN;
    }


    private String determineParentPath(TerminalNode positionTerminal, PrismContainerDefinition<?> containerDefinition, Map<ParseTree, Definition> itemDefinitions, Object option) {
        if (option != null && option.equals(Filter.Infra.METADATA)) {
            return Filter.Infra.METADATA.getName() + "/";
        }

        if (isAtSign(positionTerminal)) {
            return "";
        }

        TerminalNode previousTerminal = getPreviousTerminal(positionTerminal);
        if (isAtSign(previousTerminal)) {
            return "";
        }

        return Objects.equals(itemDefinitions.get(findIdentifierOfDefinition(positionTerminal, AxiomQueryParser.RootContext.class)), containerDefinition)
                ? ""
                : containerDefinition.getItemName().getLocalPart() + "/";
    }
}
