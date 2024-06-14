package com.evolveum.midpoint.prism.impl.query.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.Nullable;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.ItemFilterContext;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParserBaseVisitor;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.TypeDefinition;
import com.evolveum.midpoint.prism.impl.marshaller.ItemPathHolder;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;

/**
 * Created by Dominik.
 */
public class AxiomQueryValidationVisitor extends AxiomQueryParserBaseVisitor<Object> {
    private final SchemaRegistry schemaRegistry;
    public final List<AxiomQueryError> errorList = new ArrayList<>();
    private TypeDefinition typeDefinition;
    private ItemDefinition<?> itemDefinition;

    public AxiomQueryValidationVisitor(@Nullable ItemDefinition<?> rootItem, PrismContext prismContext) {
        schemaRegistry = prismContext.getSchemaRegistry();
        if (rootItem != null) {
            itemDefinition = rootItem;
        }
        if (itemDefinition != null && itemDefinition.getTypeName() != null) {

            typeDefinition = schemaRegistry.findTypeDefinitionByType(itemDefinition.getTypeName());
        }
    }

    @Override
    public Object visitItemFilter(ItemFilterContext ctx) {
        if (ctx.path() != null) {
            if (ctx.path().getText().equals(".")) {
                if (ctx.filterName().getText().equals(Filter.Name.TYPE.getLocalPart())) {
                    // checking . type ObjectType
                    typeDefinition = checkType(ctx.subfilterOrValue());
                }
            } else if (ctx.path().getText().equals(Filter.Meta.TYPE.getName()) || ctx.path().getText().equals(PrismQueryLanguageParserImpl.REF_TYPE)) {
                // checking path context META @type
                typeDefinition = checkType(ctx.subfilterOrValue());
            } else if (ctx.path().getText().equals(Filter.Meta.PATH.getName()) || ctx.path().getText().equals(Filter.Meta.RELATION.getName())) {
                // checking path context META @path & @relation
                itemDefinition = checkItemPath(ctx.subfilterOrValue());
            } else {
                itemDefinition = checkItemPath(ctx.path());
            }
        }

        if (ctx.filterName() != null) {
            checkFilterName(itemDefinition, ctx.filterName());
        }

        if (ctx.filterNameAlias() != null) {
            if (!ctx.path().getText().equals(Filter.Meta.PATH.getName()) || ctx.path().getText().equals(Filter.Meta.RELATION.getName())) {
                checkFilterName(itemDefinition, ctx.filterNameAlias());
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
        } else {
            if (this.typeDefinition == null) {this.typeDefinition = typeDefinition;}
            List<TypeDefinition> objectTypes = schemaRegistry.getAllSubTypesByTypeDefinition(List.of(this.typeDefinition));

            if (!objectTypes.contains(this.typeDefinition) && !objectTypes.contains(typeDefinition)) {
                errorList.add(new AxiomQueryError(null,
                        null,
                        ctx.getStart().getLine(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                        "Bad type " + ctx.getText(),
                        null)
                );
            }
        }

        return typeDefinition;
    }

    private ItemDefinition<?> checkItemPath(ParserRuleContext ctx) {
        PrismObjectDefinition<?> objectDefinition = null;
        ItemPath itemPath = null;
        int itemPathCount = ctx.getChildCount();

        if (typeDefinition != null) {
            objectDefinition = schemaRegistry.findObjectDefinitionByCompileTimeClass((Class) typeDefinition.getCompileTimeClass());
        }

        if (objectDefinition != null) {
            for (int i = 0; i < itemPathCount; i = i + 2) {
                // catch dereference path
                if (ctx.getChild(i).getText().equals(PrismQueryLanguageParserImpl.REF_TARGET_ALIAS)) {
                    i = i + 2;
                    itemPath = ItemPathHolder.parseFromString(ctx.getChild(i).getText());
                    itemDefinition = objectDefinition.findItemDefinition(itemPath, ItemDefinition.class);
                } else {
                    itemPath = ItemPathHolder.parseFromString(ctx.getChild(i).getText());
                    itemDefinition = objectDefinition.findItemDefinition(itemPath, ItemDefinition.class);
                }

                if (itemDefinition == null) {
                    errorList.add(new AxiomQueryError(null,
                            null,
                            ctx.getStart().getLine(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                            "Path " + itemPath + " is not present in type " + objectDefinition.getTypeName().getLocalPart(),
                            null)
                    );
                } else {
                    if (i != (itemPathCount - 1)) {
                        itemDefinition = objectDefinition.findItemDefinition(itemPath);
                    } else {
                        return objectDefinition.findItemDefinition(itemPath);
                    }
                }
            }
        } else {
            errorList.add(new AxiomQueryError(null,
                    null,
                    ctx.getStart().getLine(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                    "Missing object definition",
                    null)
            );
        }

        return null;
    }

    private void checkFilterName(ItemDefinition<?> itemDefinition, ParserRuleContext ctx) {
        if (itemDefinition != null) {

            Map<String, String> filters = FilterNamesProvider.findFilterNamesByItemDefinition(itemDefinition, ctx);

            if (!filters.containsKey(ctx.getText()) && !filters.containsValue(ctx.getText())) {
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
