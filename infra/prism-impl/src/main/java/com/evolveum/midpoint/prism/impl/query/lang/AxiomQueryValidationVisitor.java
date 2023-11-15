package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.*;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParserBaseVisitor;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.marshaller.ItemPathHolder;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik.
 */
public class AxiomQueryValidationVisitor extends AxiomQueryParserBaseVisitor<Object> {
    private PrismContext context;
    public final List<AxiomQueryError> errorList = new ArrayList<>();

    private Class userType;

    private Class<?> type;

    private ItemDefinition<?> itemDefinition;

    private final SchemaRegistry schemaRegistry = PrismContext.get().getSchemaRegistry();

    public AxiomQueryValidationVisitor(PrismContext prismContext, Class userType) {
        this.context = prismContext;
        this.userType = userType;
    }

    @Override
    public Object visitItemFilter(ItemFilterContext ctx) {
        if (ctx.path() != null) {
            if (ctx.filterName() != null) {
                // checking . type ObjectType
                if (ctx.filterName().getText().equals(FilterNames.TYPE.getLocalPart())) {
                    if (ctx.subfilterOrValue() != null) {
                        this.type = checkType(ctx.subfilterOrValue().getText());
                    }
                }
            }

            if (ctx.path().getText().equals(FilterNames.META_TYPE)) {
                // checking path context META @type
                this.type = checkType(ctx.subfilterOrValue().getText());
            } else if (ctx.path().getText().equals(FilterNames.META_PATH)) {
                // checking path context META @path
                this.itemDefinition = checkItemPath(this.type, ctx.subfilterOrValue().getText());
            } else if (!ctx.path().getText().equals(".")) {
                this.itemDefinition = checkItemPath(this.type, ctx.path().getText());
            }
        }

        if (ctx.filterName() != null) {
            checkFilterName(this.itemDefinition, ctx.filterName());
        }

        if (ctx.filterNameAlias() != null) {
            checkFilterName(this.itemDefinition, ctx.filterNameAlias());
        }

        return super.visitItemFilter(ctx);
    }

    private Class<?> checkType(String type) {
//        if (schemaRegistry.findTypeDefinitionByType(new QName(type)) == null) {
//            errorList.add(new AxiomQueryError(null,
//                    null,
//                    0, 0,
//                    "Does not existing type " + type,
//                    null)
//            );
//        } else {
//            this.type = schemaRegistry.findTypeDefinitionByType(new QName(type));
//        }
        return null;
    }

    private ItemDefinition<?> checkItemPath(@Nullable Class<?> type, String path) {
        ItemDefinition<?> itemDefinition = schemaRegistry.findObjectDefinitionByCompileTimeClass(this.userType);
        ItemPath itemPath = ItemPathHolder.parseFromString(path);

        if (itemDefinition.findItemDefinition(itemPath, ItemDefinition.class) == null) {
            errorList.add(new AxiomQueryError(null,
                    null,
                    0, 0,
                    "Path " + path + " is not present in type " + this.userType.getSimpleName(),
                    null)
            );
        }

        return itemDefinition.findItemDefinition(itemPath, ItemDefinition.class);
    }

    private void checkFilterName(ItemDefinition<?> itemDefinition, Object ctx) {
        if (itemDefinition != null) {
            if (ctx instanceof FilterNameContext filterNameContext) {
                if (!FilterNamesProvider.findFilterNamesByItemDefinition(itemDefinition, filterNameContext).contains(filterNameContext.getText())) {
                    errorList.add(new AxiomQueryError(null,
                            null,
                            0, 0,
                            "Filter name " + filterNameContext.getText() + " is not supported for path " + itemDefinition.getItemName(),
                            null)
                    );
                }
            }

            if (ctx instanceof FilterNameAliasContext filterNameAliasContext) {
                if (!FilterNamesProvider.findFilterNamesByItemDefinition(itemDefinition, filterNameAliasContext).contains(filterNameAliasContext.getText())) {
                    errorList.add(new AxiomQueryError(null,
                            null,
                            0, 0,
                            "Filter name alias " + filterNameAliasContext.getText() + " is not supported for path " + itemDefinition.getItemName(),
                            null)
                    );
                }
            }
        }
    }

    private ParseTree getLastNode(RuleNode currentPosition) {
        while (currentPosition.getRuleContext().getRuleIndex() != AxiomQueryParser.RULE_itemFilter) {
            currentPosition = (RuleNode) currentPosition.getParent();
            return currentPosition.getChild(currentPosition.getChildCount() - 1);
        }
        return null;
    }

    private ParseTree getFirstNode(RuleNode currentPosition) {
        while (currentPosition.getRuleContext().getRuleIndex() != AxiomQueryParser.RULE_itemFilter) {
            currentPosition = (RuleNode) currentPosition.getParent();
            return currentPosition.getChild(0);
        }
        return null;
    }
}
