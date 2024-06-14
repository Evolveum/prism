package com.evolveum.midpoint.prism.impl.query.lang;

import static com.evolveum.midpoint.prism.impl.query.lang.PrismQueryLanguageParserImpl.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParserBaseVisitor;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.TypeDefinition;
import com.evolveum.midpoint.prism.impl.PrismContainerDefinitionImpl;
import com.evolveum.midpoint.prism.impl.marshaller.ItemPathHolder;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;

/**
 * Created by Dominik.
 */
public class AxiomQueryCompletionVisitor extends AxiomQueryParserBaseVisitor<Object> {

    private final SchemaRegistry schemaRegistry;

    private ParseTree lastSeparator = null;

    private QName lastType = null;

    public AxiomQueryCompletionVisitor(ItemDefinition<?> rootDef, PrismContext prismContext) {
        schemaRegistry = prismContext.getSchemaRegistry();
        if (rootDef != null) {
            lastType = rootDef.getTypeName();
        }
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == AxiomQueryParser.SEP) {
            lastSeparator = node;
            return null;
        }

        if (node.getSymbol().getType() == AxiomQueryParser.EOF) {
            lastSeparator = node;
            return null;
        }

        return null;
    }

    @Override
    public Object visitErrorNode(ErrorNode node) {
        return super.visitErrorNode(node);
    }

    @Override
    public Object visitItemComponent(AxiomQueryParser.ItemComponentContext ctx) {
        // FIXME: Is this correct? This is actually also executed for item paths
        QName maybeQName = new QName(ctx.getText());
        var maybeType = schemaRegistry.findTypeDefinitionByType(maybeQName);
        if (maybeType != null) {
            lastType = maybeType.getTypeName();
        }
        return super.visitItemComponent(ctx);
    }

    public Map<String, String> generateSuggestion() {
        Map<String, String> suggestions = new HashMap<>();
        final ParseTree lastNode = getLastNode(lastSeparator);

        if (lastNode instanceof AxiomQueryParser.ItemPathComponentContext ctx) {
            suggestions = getFilters(lastNode.getText());
            suggestions.put(Filter.Name.NOT.getLocalPart(), null);
        } else if (lastNode instanceof AxiomQueryParser.SelfPathContext ctx) {
            // TODO solve SelfPathContext
        } else if (lastNode instanceof AxiomQueryParser.FilterNameContext ctx) {
            // TODO maybe to add suggestion for value
            // value for @type || . type
            if (findNode(ctx).getChild(0).getText().equals(Filter.Meta.TYPE.getName()) || ctx.getText().equals(Filter.Name.TYPE.getLocalPart())) {
                TypeDefinition typeDefinition = schemaRegistry.findTypeDefinitionByType(defineObjectType());
                suggestions = schemaRegistry.getAllSubTypesByTypeDefinition(List.of(typeDefinition)).stream()
                        .collect(Collectors.toMap(item -> item.getTypeName().getLocalPart(), item -> item.getTypeName().getLocalPart(), (existing, replacement) -> existing));
            }

            // value for @path
            if (findNode(ctx).getChild(0).getText().equals(Filter.Meta.PATH.getName()) || ctx.getText().equals(LPAR)) {
                suggestions = getAllPath();
            }
            // value for @relation
            if (ctx.getText().equals(Filter.Meta.RELATION.getName())) {
                // TODO add value for @relation to suggestions list
            }

            if (ctx.getText().equals(Filter.Name.MATCHES.getLocalPart()) || ctx.getText().equals(Filter.Name.REFERENCED_BY.getLocalPart()) || ctx.getText().equals(Filter.Name.OWNED_BY.getLocalPart())) {
                suggestions.put(LPAR, null);
            }
        } else if (lastNode instanceof AxiomQueryParser.GenFilterContext ctx) {
            if (ctx.getText().equals(DOT)) {
                suggestions = getFilters(lastNode.getText());
            } else {
                suggestions = getFilters(lastNode.getText());
                suggestions.put(Filter.Name.NOT.getLocalPart(), null);
            }
        } else if (lastNode instanceof AxiomQueryParser.DescendantPathContext ctx) {
            // TODO solve DescendantPathContext
        } else if (lastNode instanceof AxiomQueryParser.SubfilterOrValueContext ctx) {
            if (ctx.getText().equals(LPAR)) {
                if (ctx.getText().equals(REF_TARGET_ALIAS)) {
                    // TODO ( -> @type, @path, @relation
                } else {
                    suggestions = getAllPath();
                }
            }

            suggestions.put(Filter.Name.AND.getLocalPart(), null);
            suggestions.put(Filter.Name.OR.getLocalPart(), null);
        } else if (lastNode instanceof TerminalNode ctx) {
            if (ctx.getSymbol().getType() == AxiomQueryParser.SEP || ctx.getSymbol().getType() == AxiomQueryParser.AND_KEYWORD || ctx.getSymbol().getType() == AxiomQueryParser.OR_KEYWORD) {
                suggestions = getAllPath();
                suggestions.put(DOT, null);
            }

            if (ctx.getSymbol().getType() == AxiomQueryParser.SLASH) {
                ParseTree pathAfterSlash = getPreviousToken(ctx.getParent());
                if (pathAfterSlash != null) {
                    suggestions = getPathAfterSlash(pathAfterSlash.getText());
                }
            }
        } else if (lastNode instanceof AxiomQueryParser.FilterContext) {
            suggestions = getAllPath();
            suggestions.put(DOT, null);
        } else if (lastNode instanceof ErrorNode ctx) {
            // TODO solve Error token
        }

        return suggestions;
    }

    private ParseTree getLastNode(ParseTree node) {
        int separatorIndex = -1;

        if (node == null) {
            return null;
        }

        ParseTree lastSeparatorParent = node.getParent();

        for (int i = 0; i < lastSeparatorParent.getChildCount(); i++) {
            if (lastSeparatorParent.getChild(i) instanceof TerminalNode terminalNode && terminalNode.getSymbol().getType() == AxiomQueryParser.SEP) {
                separatorIndex = i;
            }
        }

        if (separatorIndex > 0) {
            separatorIndex = separatorIndex - 1;
        }
        if (separatorIndex == -1) {
            separatorIndex = 0;
        }

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

    private ParseTree getNextToken(@NotNull ParseTree ctx) {
        int count = ctx.getChildCount() - 1;
        return count >= 1 ? ctx.getChild(count + 1) : null;
    }

    private ParseTree getPreviousToken(@NotNull ParseTree ctx) {
        int count = ctx.getChildCount() - 1;
        return count >= 1 ? ctx.getChild(count - 1) : null;
    }

    private Map<String, String> getAllPath() {
        Map<String, String> suggestionPaths = new HashMap<>();
        TypeDefinition typeDefinition = schemaRegistry.findTypeDefinitionByType(defineObjectType());
        PrismObjectDefinition<?> objectDefinition = schemaRegistry.findObjectDefinitionByCompileTimeClass((Class) typeDefinition.getCompileTimeClass());

        for (ItemName itemName : objectDefinition.getItemNames()) {
            if (getPathAfterSlash(itemName.getLocalPart()) != null) {
                getPathAfterSlash(itemName.getLocalPart()).forEach((key, value) -> {
                    suggestionPaths.put(itemName.getLocalPart() + "/" + value, itemName.getLocalPart() + "/" + value);
                });
            }

            suggestionPaths.put(itemName.getLocalPart(), itemName.getLocalPart());
        }

        return suggestionPaths;
    }

    private Map<String, String> getFilters(@NotNull String stringItemPath) {
        ItemPath itemPath = ItemPathHolder.parseFromString(stringItemPath);
        TypeDefinition typeDefinition = schemaRegistry.findTypeDefinitionByType(defineObjectType());
        PrismObjectDefinition<?> objectDefinition = schemaRegistry.findObjectDefinitionByCompileTimeClass((Class) typeDefinition.getCompileTimeClass());
        ItemDefinition<?> itemDefinition = objectDefinition.findItemDefinition(itemPath, ItemDefinition.class);
        return FilterNamesProvider.findFilterNamesByItemDefinition(itemDefinition, new AxiomQueryParser.FilterContext());
    }

    private Map<String, String> getPathAfterSlash(@NotNull String path) {
        if (schemaRegistry.findContainerDefinitionByType(lastType) instanceof PrismContainerDefinitionImpl<?> containerValue) {
            ItemName itemName = new ItemName(containerValue.getItemName().getNamespaceURI(), path);
            try {
                return containerValue.findContainerDefinition(itemName).getItemNames()
                        .stream().collect(Collectors.toMap(ItemName::getLocalPart, ItemName::getLocalPart));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        return null;
    }

    // remove after implementing schemaContext annotation
    private QName defineObjectType() {
        if (lastType == null) {
            return new QName("UserType");
        }
        return lastType;
    }
}
