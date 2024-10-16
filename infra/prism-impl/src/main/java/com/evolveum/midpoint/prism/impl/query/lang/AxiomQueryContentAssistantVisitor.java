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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * PRISM definition for root context (initial state), primary from schema context annotation
     */
    private final ItemDefinition<?> rootItemDefinition;
    /**
     * Table hash for mapping PRISM definition according AST (primary for itemFilter, filter and root context)
     */
    private final HashMap<ParseTree, Definition> itemDefinitions = new HashMap<>();
    /**
     * Error list for save semantic and syntax errors also
     */
    private final List<AxiomQueryError> errorList = new ArrayList<>();
    /**
     * Mumber of position cursor (start from 1)
     */
    private final int positionCursor;
    /**
     * Context at the location of the cursor position
     */
    private PositionContext positionContext;
    /**
     * PRISM definition variable for infra language concept
     */
    private Definition infraPathDefinition;
    /**
     * ItemComponents should be treated as item path
     */
    private boolean itemExpected;
    /**
     *  Is first item path component expected?
     */
    private boolean firstItemComponentExpected;

    public AxiomQueryContentAssistantVisitor(PrismContext prismContext, ItemDefinition<?> rootItem) {
        this(prismContext, rootItem, null, 0);
    }

    public AxiomQueryContentAssistantVisitor(PrismContext prismContext, ItemDefinition<?> rootItem, ATN atn, int positionCursor) {
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

        var parent = findItemFilterContextInTree(ctx);
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
        var filterCtx = findItemFilterContextInTree(ctx);
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
        var itemFilterContext = findItemFilterContextInTree(ctx);
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

        var itemFilterContext = findItemFilterContextInTree(ctx);
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
        var itemFilterContext = findItemFilterContextInTree(ctx);
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
        var itemFilterContext = findItemFilterContextInTree(ctx);
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

    private Definition itemFilterDefinition(ParseTree node) {
        return itemDefinitions.get(findItemFilterContextInTree(node));
    }

    @Override
    public Object visitSubfilterSpec(AxiomQueryParser.SubfilterSpecContext ctx) {
        infraPathDefinition = null;
        return super.visitSubfilterSpec(ctx);
    }

    public List<AxiomQueryError> getErrorList() {
        return errorList;
    }

    /**
     * Generate code completions suggestion for AxiomQuery language by position context.
     * @return List {@link Suggestion}
     */
    public List<Suggestion> generateSuggestions() {
        List<Suggestion> suggestions = new ArrayList<>();
        boolean isSelfPath = false;
        boolean isDereference = false;

        if (positionContext != null) {
            ParseTree node = positionContext.node().getChild(positionContext.cursorIndex());
            Definition definition = itemDefinitions.get(findIdentifierDefinition(node));
            TerminalNode terminal = getTerminalNode(node);
            TerminalNode previousTerminal = getTerminalNode(getPreviousNode(node));

            if (terminal.getSymbol().getType() == AxiomQueryParser.AT_SIGN) {
                isDereference = true;
            }

            if (previousTerminal != null && previousTerminal.getSymbol().getType() == AxiomQueryParser.AT_SIGN) {
                isDereference = true;
                definition = itemDefinitions.get(findItemFilterContextInTree(node));
            }

            if (previousTerminal != null && previousTerminal.getParent() instanceof AxiomQueryParser.SelfPathContext) {
                isSelfPath = true;
            }

            for (TokenWithCtx token : getExpectedTokenCtxByPositionCtx(atn, positionContext)) {
                if (token.index() == AxiomQueryParser.IDENTIFIER) {
                    if (token.rules() != null && token.rules().contains(AxiomQueryParser.RULE_filterName)) {
                        ParseTree itemFilter = findItemFilterContextInTree(node);
                        var itemDefinition = itemDefinitions.get(findItemFilterContextInTree(itemFilter));

                        if (itemDefinition != null) {
                            FilterProvider.findFilterByItemDefinition(itemDefinition, AxiomQueryParser.RULE_filterName).forEach((name, alias) -> {
                                suggestions.add(new Suggestion(name, alias, -1));
                            });
                        }
                    } else if (token.rules() != null && token.rules().contains(AxiomQueryParser.RULE_path)) {
                        definitionProcessingToPathSuggestion(definition, suggestions);
                    }
//                    else if (token.rules() != null && token.rules().contains(AxiomQueryParser.RULE_matchingRule)) {
//                        // generate matching paths to [... ]
//                    } else if (token.rules() != null && token.rules().contains(AxiomQueryParser.RULE_subfilterOrValue)) {
//                        // generate value for IDENTIFIER value path ???
//                    }
                } else if (token.index() == AxiomQueryParser.NOT_KEYWORD) {
                    suggestions.add(new Suggestion(Filter.Name.NOT.name().toLowerCase(), Filter.Name.NOT.name().toLowerCase(), -1));
                } else if (token.index() == AxiomQueryParser.AND_KEYWORD) {
                    suggestions.add(new Suggestion(Filter.Name.AND.name().toLowerCase(), Filter.Name.AND.name().toLowerCase(), -1));
                } else if (token.index() == AxiomQueryParser.OR_KEYWORD) {
                    suggestions.add(new Suggestion(Filter.Name.OR.name().toLowerCase(), Filter.Name.OR.name().toLowerCase(), -1));
                } else if (token.index() == AxiomQueryParser.STRING_MULTILINE ||
                        token.index() == AxiomQueryParser.STRING_DOUBLEQUOTE ||
                        token.index() == AxiomQueryParser.STRING_SINGLEQUOTE ||
                        token.index() == AxiomQueryParser.STRING_BACKTICK_TRIQOUTE ||
                        token.index() == AxiomQueryParser.STRING_BACKTICK
                ) {
                    suggestions.add(new Suggestion("'", "String value", -1));
                    suggestions.add(new Suggestion("\"", "String value", -1));
                } else {
                    if (!isDereference && !isSelfPath && (token.index() != AxiomQueryParser.EQ ||
                            token.index() != AxiomQueryParser.NOT_EQ ||
                            token.index() != AxiomQueryParser.GT ||
                            token.index() != AxiomQueryParser.GT_EQ ||
                            token.index() != AxiomQueryParser.LT ||
                            token.index() != AxiomQueryParser.LT_EQ)) {
                        suggestions.add(suggestionFromVocabulary(token, -1));
                    }
                }
            }
        }

        return suggestions;
    }

    private Suggestion suggestionFromVocabulary(TokenWithCtx token, int priority) {
        // DisplayName (or LiteralName) is escaped with single qoutes, so we remove them
        var tokenValue = AxiomStrings.fromOptionallySingleQuoted(AxiomQueryParser.VOCABULARY.getDisplayName(token.index()));
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
     * Find node which has cursor of position with index of branch.
     * @param tree
     * @return {@link PositionContext}
     */
    private PositionContext findPositionContext(ParseTree tree) {
        int count = tree.getChildCount();
        ParseTree parent;

        if (tree instanceof AxiomQueryParser.RootContext rootContext) {
            if (rootContext.filter() != null && rootContext.filter().getChildCount() > 0) {
                return new PositionContext(getChildIndexInParent(rootContext, rootContext.filter()), rootContext.filter());
            } else {
                return new PositionContext(0, tree);
            }
        }

        while (count <= 1) {
            if ((parent = tree.getParent()).getChildCount() > 1) {
                return new PositionContext(getChildIndexInParent(tree, parent), parent);
            }

            tree = tree.getParent();
            count = tree.getChildCount();
        }

        return null;
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
                        if (node.getChildCount() > 0) {
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
     * Find reference object of node which present change of definition in AST (rootContext or subFilterSpecContext).
     * @param node
     * @return reference object as identifier for {@link AxiomQueryContentAssistantVisitor#itemDefinitions}
     */
    private ParseTree findIdentifierDefinition(ParseTree node) {
        if (node == null) return null;

        while (!(node instanceof AxiomQueryParser.RootContext)) {
            if (node instanceof AxiomQueryParser.SubfilterSpecContext) break;
            node = node.getParent();
        }

        return node;
    }

    private void updateItemDefinitionForContext(ParseTree key, Definition itemDefinition) {
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
            if (terminalNode.getSymbol().getType() == AxiomQueryParser.SEP) {
                if (getPreviousNode(node) instanceof RuleContext ctx) {
                    node = ctx;
                } else {
                    node = node.getParent();
                }
            } else {
                node = terminalNode.getParent();
            }
        }

        if (node instanceof RuleContext ruleContext) {
            if (ruleContext.getRuleIndex() == AxiomQueryParser.RULE_root) {
                // initial root context (path, subFilter, negation)
                expected.addAll(findExpectedTokensInItemFilter(null));
            } else {
                findTokensWithRuleCtxInATN(atn, ruleContext, expected);
                expected.remove(new TokenWithCtx(10, null));
            }
        }

        return expected;
    }

    /**
     * The method fill the list of expected tokens with the appropriate rules context in ATN network.
     * first analyzes position context rule -> find all following rules
     * next analyzes all following rules and to collect all excepted tokens with rules ctx
     * @param atn ANTLR4 ATN network
     * @param context position context on the based on the which search following rules
     * @param expected list to fill expected tokens with rule context
     */
    private void findTokensWithRuleCtxInATN(@NotNull ATN atn,
            @NotNull RuleContext context,
            @NotNull List<TokenWithCtx> expected) {

        Stack<ATNState> states = new Stack<>(), passedStates = new Stack<>();
        TerminalNode terminal = getTerminalNode(positionContext.node().getChild(positionContext.cursorIndex()));
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
                    } else {
                        registerExpectedTokens(atomTransition.label, null, expected);
                    }
                } else if (transition instanceof SetTransition setTransition) {
                    setTransition.set.getIntervals().forEach(interval -> {
                        for (int i = interval.a; i <= interval.b; i++) {
                            registerExpectedTokens(i, null, expected);
                        }
                    });
                } else if (transition instanceof RuleTransition ruleTransition) {
                    // processing filters concepts
                    if (context.getRuleIndex() == AxiomQueryParser.RULE_filter) {
                        if (context.getRuleIndex() == ruleTransition.ruleIndex) {
                            states.push(ruleTransition.target);
                        }

                        if (context instanceof AxiomQueryParser.GenFilterContext) {
                            List<TokenWithCtx> expectedTokens = findExpectedTokensInItemFilter(findItemFilterContextInTree(context));

                            if (expectedTokens.isEmpty()) {
                                states.push(ruleTransition.followState);
                            } else {
                                expected.addAll(expectedTokens);
                                states.clear();
                                break;
                            }
                        } else if (context instanceof AxiomQueryParser.AndFilterContext ||
                                context instanceof AxiomQueryParser.OrFilterContext) {
                            if (positionContext.node().getChild(positionContext.cursorIndex()) instanceof TerminalNode terminalNode) {
                                if (terminalNode.getSymbol().getType() == AxiomQueryParser.SEP) {
                                    if (ruleTransition.ruleIndex == AxiomQueryParser.RULE_itemFilter) {
                                        expected.addAll(findExpectedTokensInItemFilter(null));
                                    }
                                }
                            }
                        }
                    } else if (context.getRuleIndex() == AxiomQueryParser.RULE_subfilterOrValue) {
                        states.push(ruleTransition.followState);
                    } else if (context.getRuleIndex() == AxiomQueryParser.RULE_negation) {
                        if (context.getParent().getRuleIndex() == AxiomQueryParser.RULE_filter && ruleTransition.ruleIndex == AxiomQueryParser.RULE_subfilterSpec) {
                            states.push(ruleTransition.target);
                        } else if (context.getParent().getRuleIndex() == AxiomQueryParser.RULE_itemFilter) {
                            expected.addAll(findExpectedTokensInItemFilter(findItemFilterContextInTree(context)));
                        }
                        states.push(ruleTransition.target);
                    } else {
                        expected.addAll(findExpectedTokensInItemFilter(findItemFilterContextInTree(context)));
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

    /**
     * Searches for all subsequent tokens in the itemFilter ATN according to the itemFilter parsing context.
     * @param itemFilter parse context itemFilter concept of AXQ language.
     * @return expected tokens with rules (currently only the token of IDENTIFIER need rules) by currently state itemFilter context.
     */
    private List<TokenWithCtx> findExpectedTokensInItemFilter(@Nullable AxiomQueryParser.ItemFilterContext itemFilter) {
        Stack<ATNState> states = new Stack<>(), passedStates = new Stack<>();
        Stack<TokenWithCtx> tokens = new Stack<>();
        Stack<Integer> rules = new Stack<>();
        ATNState nextState;
        ParseTree concept;

        // if context itemFilter is null then initial traverse expected tokens for RootContext
        if (itemFilter == null) {
            states.push(atn.states.get(0));

            while (!states.isEmpty()) {
                nextState = states.pop();
                passedStates.push(nextState);

                if (!rules.contains(nextState.ruleIndex)) {
                    rules.push(nextState.ruleIndex);
                }

                for (Transition transition : nextState.getTransitions()) {
                    if (transition instanceof AtomTransition atomTransition) {
                        if (atomTransition.label == AxiomQueryParser.SEP) {
                            states.push(atomTransition.target);
                        } else {
                            registerExpectedTokens(atomTransition.label, rules, tokens);
                        }
                    } else if (transition instanceof RuleTransition ruleTransition) {
                        states.push(ruleTransition.target);
                    } else {
                        // check looping
                        if (!passedStates.contains(transition.target) && nextState.getClass() != RuleStopState.class) {
                            states.push(transition.target);
                        }
                    }
                }
            }
        } else {
            // find cursor location in itemFilter
            TerminalNode terminal = getTerminalNode(positionContext.node().getChild(positionContext.cursorIndex()));
            int terminalType = terminal.getSymbol().getType();

            concept = findNodeOfTerminal(terminal);
            if (concept.getParent() instanceof AxiomQueryParser.FilterContext ||
                    concept.getParent() instanceof AxiomQueryParser.RootContext) {
                concept = Optional.ofNullable(getPreviousNode(concept)).orElse(concept);
                concept = findItemFilterContextInTree(concept);
                concept = concept.getChild(concept.getChildCount() - 1);
            }

            if (concept instanceof TerminalNode) {
                concept = getPreviousNode(concept);
            }

            if (concept instanceof TerminalNode) {
                concept = concept.getParent();
            }

            if (concept instanceof RuleContext lastConcept) {
                // last Concept must always be a child of itemFilter !!!
                states.push(atn.states.get(lastConcept.invokingState));

                while (!states.isEmpty()) {
                    nextState = states.pop();
                    passedStates.push(nextState);

                    if (!rules.contains(nextState.ruleIndex)) {
                        rules.push(nextState.ruleIndex);
                    }

                    for (Transition transition : nextState.getTransitions()) {
                        if (transition instanceof AtomTransition atomTransition) {
                            if (atomTransition.label == terminalType) {
                                if (terminalType == AxiomQueryParser.IDENTIFIER &&
                                        lastConcept.getRuleIndex() == AxiomQueryParser.RULE_path) {
                                    registerExpectedTokens(atomTransition.label, rules, tokens);
                                }
                                states.push(atomTransition.target);
                            } else {
                                registerExpectedTokens(atomTransition.label, rules, tokens);
                            }
                        } else if (transition instanceof SetTransition setTransition) {
                            if (setTransition.label().contains(terminalType)) {
                                states.push(setTransition.target);
                            } else {
                                setTransition.set.getIntervals().forEach(interval -> {
                                    for (int i = interval.a; i <= interval.b; i++) {
                                        registerExpectedTokens(i, null, tokens);
                                    }
                                });
                            }
                        } else if (transition instanceof RuleTransition ruleTransition) {

                            if(terminalType == AxiomQueryParser.SLASH && nextState.stateNumber == lastConcept.invokingState && lastConcept.getRuleIndex() == ruleTransition.ruleIndex) {
                                states.push(ruleTransition.target);
                            }

                            // if invoke state before has rule, then go to rule because ANTLR4 invoke state returned before rule context
                            if (lastConcept.getRuleIndex() == ruleTransition.ruleIndex) {
                                if ((ruleTransition.ruleIndex == AxiomQueryParser.RULE_path ||
                                        ruleTransition.ruleIndex == AxiomQueryParser.RULE_matchingRule) &&
                                        terminalType == AxiomQueryParser.IDENTIFIER) {
                                    states.push(ruleTransition.target);
                                    states.push(ruleTransition.followState);
                                } else {
                                    states.push(ruleTransition.followState);
                                    rules.clear();
                                }
                            } else {
                                if (!(terminalType == AxiomQueryParser.SLASH && ruleTransition.ruleIndex == AxiomQueryParser.RULE_filterNameAlias)) {
                                    states.push(ruleTransition.target);
                                }
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
        }

        return tokens;
    }

    /**
     * Method check if concept has separator or not -> other terminal token because syntax of language to possible some concepts without separator
     * (currently: path,filterNameAlias,matchesFilter, subFilter).
     * @param itemFilter
     * @return terminal token type.
     */
    private int getLastTerminalNode(AxiomQueryParser.ItemFilterContext itemFilter, PositionContext positionContext) {
        RuleContext ruleContext = itemFilter;
        int index = ruleContext.getChildCount() - 1;

        while (!(ruleContext.getChild(index) instanceof TerminalNode terminal &&
                terminal.getSymbol().getType() == AxiomQueryParser.SEP)) {

            if (ruleContext.getRuleIndex() == AxiomQueryParser.RULE_root ||
                    ruleContext.getRuleIndex() == AxiomQueryParser.RULE_filter) {
                if (ruleContext.getChild(index + 1) != null &&
                        ruleContext.getChild(index + 1) instanceof TerminalNode terminal &&
                        terminal.getSymbol().getType() == AxiomQueryParser.SEP) {
                    return AxiomQueryParser.SEP;
                } else if (ruleContext.getRuleIndex() == AxiomQueryParser.RULE_root) {
                    return getTerminalNode(positionContext.node().getChild(positionContext.cursorIndex())).getSymbol().getType();
                }
            }

            index = getChildIndexInParent(ruleContext, ruleContext.getParent());
            ruleContext = ruleContext.getParent();
        }

        return getTerminalNode(positionContext.node().getChild(positionContext.cursorIndex())).getSymbol().getType();
    }

    /**
     * Method to get the index of index node (ctx) in its parent's children list.
     * @param child
     * @param parent
     * @return
     */
    private int getChildIndexInParent(ParseTree child, ParseTree parent) {
        if (child == null || parent == null) return 0;

        for (int i = 0; i < parent.getChildCount(); i++) {
            ParseTree childAtIndex = parent.getChild(i);
            if (childAtIndex == child) {
                return i;
            }
        }

        return 0;
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
     * @param tokens
     */
    private void registerExpectedTokens(int label, Stack<Integer> rules, List<TokenWithCtx> tokens) {
        TokenWithCtx token = new TokenWithCtx(label, null);
        // currently to need rules context only for IDENTIFIER token
        if (label == AxiomQueryParser.IDENTIFIER) {
            token = token.withRules(rules);
        }

        if (label != -1 && !tokens.contains(token)) {
            tokens.add(token);
        }
    }

    /**
     * Find node of terminal in parent node.
     * @param terminalNode
     * @return node of terminal
     */
    private ParseTree findNodeOfTerminal(TerminalNode terminalNode) {
        int index = -1;
        ParseTree node = terminalNode;

        while (node.getChildCount() <= 1) {
            index = getChildIndexInParent(node, node.getParent());
            node = node.getParent();
        }

        return node.getChild(index);
    }
}
