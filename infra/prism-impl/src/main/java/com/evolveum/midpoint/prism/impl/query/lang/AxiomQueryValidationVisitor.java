package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.*;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParserBaseVisitor;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.marshaller.ItemPathHolder;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik.
 */
public class AxiomQueryValidationVisitor extends AxiomQueryParserBaseVisitor<Object> {
    private final SchemaRegistry schemaRegistry;
    public final List<AxiomQueryError> errorList = new ArrayList<>();
    private TypeDefinition typeDefinition;
    private ItemDefinition<?> itemDefinition;

    public AxiomQueryValidationVisitor(PrismContext prismContext) {
        schemaRegistry = prismContext.getSchemaRegistry();
    }

    // TODO referenceBy  &  match
    @Override
    public Object visitItemFilter(ItemFilterContext ctx) {
        if (ctx.path() != null) {
            if (ctx.path().getText().equals(".")){
                if (ctx.filterName().getText().equals(FilterNames.TYPE.getLocalPart())) {
                    // checking . type ObjectType
                    this.typeDefinition = checkType(ctx.subfilterOrValue());
                }
            } else if (ctx.path().getText().equals(FilterNames.META_TYPE) || ctx.path().getText().equals(PrismQueryLanguageParserImpl.REF_TYPE)) {
                // checking path context META @type
                this.typeDefinition = checkType(ctx.subfilterOrValue());
            } else if (ctx.path().getText().equals(FilterNames.META_PATH) || ctx.path().getText().equals(FilterNames.META_RELATION)) {
                // checking path context META @path & @relation
                this.itemDefinition = checkItemPath(this.typeDefinition, ctx.subfilterOrValue());
            } else {
                this.itemDefinition = checkItemPath(this.typeDefinition, ctx.path());
            }
        }

        if (ctx.filterName() != null) {
            checkFilterName(this.itemDefinition, ctx.filterName());
        }

        if (ctx.filterNameAlias() != null) {
            if (!ctx.path().getText().equals(FilterNames.META_PATH) || ctx.path().getText().equals(FilterNames.META_RELATION)) {
                checkFilterName(this.itemDefinition, ctx.filterNameAlias());
            }
        }

        if (ctx.subfilterOrValue() != null) {
            if (ctx.subfilterOrValue().singleValue() != null) {
                //  TODO value checking can be
            }
        }

        return super.visitItemFilter(ctx);
    }

    private TypeDefinition checkType(ParserRuleContext ctx) {
        TypeDefinition typeDefinition = schemaRegistry.findTypeDefinitionByType(new QName(ctx.getText()));

        if (typeDefinition == null) {
            errorList.add(new AxiomQueryError(null,
                    null,
                    ctx.getStart().getLine(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                    "Does not exist type " + ctx.getText(),
                    null)
            );
        }

        return typeDefinition;
    }

    private ItemDefinition<?> checkItemPath(@Nullable TypeDefinition type, ParserRuleContext ctx) {
        if (type != null) {
            ItemDefinition<?> itemDefinition = schemaRegistry.findObjectDefinitionByCompileTimeClass((Class) type.getCompileTimeClass());
            ItemPath itemPath = null;
            int itemPathCount = ctx.getChildCount();

            for (int i = 0; i < itemPathCount; i = i + 2) {
                itemPath = ItemPathHolder.parseFromString(ctx.getChild(i).getText());

                if (itemDefinition.findItemDefinition(itemPath, ItemDefinition.class) == null) {
                    errorList.add(new AxiomQueryError(null,
                            null,
                            ctx.getStart().getLine(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                            "Path " + itemPath + " is not present in type " + itemDefinition.getTypeName().getLocalPart(),
                            null)
                    );
                } else {
                    if (i != (itemPathCount - 1)) {
                        itemDefinition = itemDefinition.findItemDefinition(itemPath, ItemDefinition.class);
                    } else {
                        return itemDefinition.findItemDefinition(itemPath, ItemDefinition.class);
                    }
                }
            }
        } else {
            errorList.add(new AxiomQueryError(null,
                    null,
                    ctx.getStart().getLine(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                    "Missing type definition",
                    null)
            );
        }

        return null;
    }

    private void checkFilterName(ItemDefinition<?> itemDefinition, ParserRuleContext ctx) {
        if (itemDefinition != null) {
            if (!FilterNamesProvider.findFilterNamesByItemDefinition(itemDefinition, ctx).contains(ctx.getText())) {
                errorList.add(new AxiomQueryError(null,
                        null,
                        ctx.getStart().getLine(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                        "Filter " + ctx.getText() + " is not supported for path " + itemDefinition.getItemName(),
                        null)
                );
            }
        }
    }
}
