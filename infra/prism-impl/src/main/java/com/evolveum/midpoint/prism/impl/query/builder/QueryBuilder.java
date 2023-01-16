/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.query.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.delta.builder.DeltaBuilder;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.builder.S_FilterEntryOrEmpty;
import com.evolveum.midpoint.prism.util.DefinitionUtil;

/**
 * Here is the language structure:
 * <pre>
 * Query ::= Filter? ('ASC(path)' | 'DESC(path)')* 'OFFSET(n)'? 'MAX-SIZE(n)'?
 *
 * Filter ::= 'ALL'
 * | 'NONE'
 * | 'UNDEFINED'
 * | 'NOT' Filter
 * | Filter ('AND'|'OR') Filter
 * | 'BLOCK' Filter? 'END-BLOCK'
 * | 'TYPE(type)' Filter?
 * | 'EXISTS(path)' Filter?
 * | 'REF(path, values*?)' Filter?
 * | 'REFERENCED-BY(class, path, relation?)' Filter?
 * | 'OWNED-BY(class, path?)' Filter?
 * | 'FULLTEXT(value)'
 * | 'ITEM(path)' ValueComparisonCondition
 * | 'ITEM(path)' ItemComparisonCondition 'ITEM(path)'
 * | 'ITEM(path)' 'IS-NULL'
 * | 'ID(values)'
 * | 'OWNER-ID(values)'
 * | OrgFilter
 *
 * ValueComparisonCondition ::= 'EQ(value)' | 'GT(value)' | 'GE(value)' | 'LT(value)' | 'LE(value)'
 * | 'STARTS-WITH(value)' | 'ENDS-WITH(value)' | 'CONTAINS(value)'
 * | 'REF(value)'
 * | 'ORG(value)'
 *
 * ItemComparisonCondition ::= 'EQ' | 'GT' | 'GE' | 'LT' | 'LE'
 *
 * OrgFilter ::= 'IS-ROOT'
 * | 'IS-DIRECT-CHILD-OF(value)'
 * | 'IS-CHILD-OF(value)'
 * | 'IS-PARENT-OF(value)'
 * | 'IS-IN-SCOPE-OF(value, scope)'
 * </pre>
 *
 * It can be visualized e.g. using https://www.bottlecaps.de/rr/ui
 * <p/>
 * Individual keywords ('AND', 'OR', 'BLOCK', ...) are mapped to methods.
 * Connections between these keywords are mapped to interfaces.
 * It can be viewed as interfaces = states, keywords = transitions. (Or vice versa, but this is more natural.)
 * The interfaces have names starting with S_ (for "state").
 * <p/>
 * Interfaces are implemented by classes that aggregate state of the query being created.
 * This is quite hacked for now... to be implemented more seriously.
 */
public final class QueryBuilder {

    @NotNull private final Class<?> queryClass;
    @NotNull private final PrismContext prismContext;
    @Nullable private final ItemDefinitionResolver itemDefinitionResolver;

    private QueryBuilder(
            @NotNull Class<?> queryClass,
            @NotNull PrismContext prismContext,
            @Nullable ItemDefinitionResolver itemDefinitionResolver) {
        this.queryClass = queryClass;
        this.prismContext = prismContext;
        this.itemDefinitionResolver = itemDefinitionResolver;
    }

    @NotNull Class<?> getQueryClass() {
        return queryClass;
    }

    public @NotNull PrismContext getPrismContext() {
        return prismContext;
    }

    public static S_FilterEntryOrEmpty queryFor(
            Class<? extends Containerable> queryClass,
            PrismContext prismContext,
            ItemDefinitionResolver itemDefinitionResolver) {
        return R_Filter.create(
                new QueryBuilder(queryClass, prismContext, itemDefinitionResolver));
    }

    public static S_FilterEntryOrEmpty queryFor(
            Class<? extends Containerable> queryClass,
            PrismContext prismContext) {
        return queryFor(queryClass, prismContext, null);
    }

    public static S_FilterEntryOrEmpty queryForReferenceOwnedBy(
            Class<? extends Containerable> ownerClass, ItemPath referencePath, PrismContext prismContext) {
        PrismReferenceDefinition refDefinition =
                DefinitionUtil.findItemDefinitionMandatory(
                        prismContext, ownerClass, referencePath, PrismReferenceDefinition.class);
        return R_Filter
                .create(
                        new QueryBuilder(Referencable.class, prismContext, null),
                        refDefinition)
                .ownedBy(ownerClass, referencePath);
    }

    /** See {@link DeltaBuilder#findItemDefinition(ItemPath, Class)}. */
    <ID extends ItemDefinition<?>> @NotNull ID findItemDefinition(
            @NotNull Class<? extends Containerable> currentClass,
            @NotNull ItemPath itemPath,
            @NotNull Class<ID> type) {
        if (itemDefinitionResolver != null) {
            ID definition = DefinitionUtil.findItemDefinition(itemDefinitionResolver, currentClass, itemPath, type);
            if (definition != null) {
                return definition;
            }
        }
        return DefinitionUtil.findItemDefinitionMandatory(prismContext, currentClass, itemPath, type);
    }
}
