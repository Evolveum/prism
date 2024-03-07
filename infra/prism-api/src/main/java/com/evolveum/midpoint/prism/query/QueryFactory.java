/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query;

import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.xnode.XNode;

/**
 * TODO it is still unclear if this interface will be officially supported.
 *    It is strongly advised to use QueryBuilder to create filters.
 *    This factory should be limited to create the most simple filters ... for example when the performance is critical.
 */
public interface QueryFactory {

    AllFilter createAll();

    NoneFilter createNone();

    ObjectFilter createUndefined();

    @Deprecated // please use QueryBuilder instead
    @NotNull <T> EqualFilter<T> createEqual(@NotNull ItemPath path, @Nullable PrismPropertyDefinition<T> definition,
            @Nullable QName matchingRule);

    // values
    @Deprecated // please use QueryBuilder instead
    @NotNull <T> EqualFilter<T> createEqual(@NotNull ItemPath path, @Nullable PrismPropertyDefinition<T> definition,
            @Nullable QName matchingRule, Object... values);

    // expression-related
    @Deprecated // please use QueryBuilder instead
    @NotNull <T> EqualFilter<T> createEqual(@NotNull ItemPath path, @Nullable PrismPropertyDefinition<T> definition,
            @Nullable QName matchingRule, @NotNull ExpressionWrapper expression);

    // right-side-related; right side can be supplied later (therefore it's nullable)
    @Deprecated // please use QueryBuilder instead
    @NotNull <T> EqualFilter<T> createEqual(@NotNull ItemPath path, PrismPropertyDefinition<T> definition,
            QName matchingRule, @NotNull ItemPath rightSidePath, ItemDefinition<?> rightSideDefinition);

    @NotNull
    @Deprecated
        // please use QueryBuilder instead
    RefFilter createReferenceEqual(ItemPath path, PrismReferenceDefinition definition,
            Collection<PrismReferenceValue> values);

    @NotNull
    @Deprecated
        // please use QueryBuilder instead
    RefFilter createReferenceEqual(ItemPath path, PrismReferenceDefinition definition, ExpressionWrapper expression);

    // empty (can be filled-in later)
    @NotNull
    @Deprecated
    // please use QueryBuilder instead
    <T> GreaterFilter<T> createGreater(@NotNull ItemPath path, PrismPropertyDefinition<T> definition, boolean equals);

    // value
    @NotNull
    @Deprecated
    // please use QueryBuilder instead
    <T> GreaterFilter<T> createGreater(@NotNull ItemPath path, PrismPropertyDefinition<T> definition,
            QName matchingRule, Object value, boolean equals);

    // expression-related
    @NotNull
    @Deprecated
    // please use QueryBuilder instead
    <T> GreaterFilter<T> createGreater(@NotNull ItemPath path, PrismPropertyDefinition<T> definition, QName matchingRule,
            @NotNull ExpressionWrapper wrapper, boolean equals);

    // right-side-related
    @NotNull
    @Deprecated
    // please use QueryBuilder instead
    <T> GreaterFilter<T> createGreater(@NotNull ItemPath path, PrismPropertyDefinition<T> definition, QName matchingRule,
            @NotNull ItemPath rightSidePath, ItemDefinition<?> rightSideDefinition, boolean equals);

    // empty (can be filled-in later)
    @NotNull
    @Deprecated
    // please use QueryBuilder instead
    <T> LessFilter<T> createLess(@NotNull ItemPath path, PrismPropertyDefinition<T> definition, boolean equals);

    // value
    @NotNull
    @Deprecated
    // please use QueryBuilder instead
    <T> LessFilter<T> createLess(@NotNull ItemPath path, PrismPropertyDefinition<T> definition,
            QName matchingRule, Object value, boolean equals);

    // expression-related
    @NotNull
    @Deprecated
    // please use QueryBuilder instead
    <T> LessFilter<T> createLess(@NotNull ItemPath path, PrismPropertyDefinition<T> definition, QName matchingRule,
            @NotNull ExpressionWrapper expressionWrapper, boolean equals);

    // right-side-related
    @NotNull
    @Deprecated
    // please use QueryBuilder instead
    <T> LessFilter<T> createLess(@NotNull ItemPath path, PrismPropertyDefinition<T> definition,
            QName matchingRule, @NotNull ItemPath rightSidePath, ItemDefinition<?> rightSideDefinition, boolean equals);

    @NotNull
    AndFilter createAnd(ObjectFilter... conditions);

    @NotNull
    AndFilter createAnd(List<ObjectFilter> conditions);

    @NotNull
    default ObjectFilter createAndOptimized(List<ObjectFilter> conditions) {
        List<ObjectFilter> nonTrivialConjuncts = conditions.stream()
                .filter(c -> c != null && !(c instanceof AllFilter))
                .toList();
        if (nonTrivialConjuncts.isEmpty()) {
            return createAll();
        } else if (nonTrivialConjuncts.size() == 1) {
            return nonTrivialConjuncts.get(0);
        } else {
            return createAnd(nonTrivialConjuncts);
        }
    }

    @NotNull
    default ObjectFilter createAndOptimized(ObjectFilter... conditions) {
        return createAndOptimized(List.of(conditions));
    }

    @NotNull
    OrFilter createOr(ObjectFilter... conditions);

    @NotNull
    OrFilter createOr(List<ObjectFilter> conditions);

    @NotNull
    default ObjectFilter createOrOptimized(List<ObjectFilter> conditions) {
        if (conditions.isEmpty()) {
            return createNone();
        } else if (conditions.size() == 1) {
            return conditions.get(0);
        } else {
            return createOr(conditions);
        }
    }

    @NotNull
    NotFilter createNot(ObjectFilter inner);

    @Deprecated
        // please use QueryBuilder instead
    <C extends Containerable> ExistsFilter createExists(ItemPath path, Class<C> containerType, ObjectFilter inner);

    @NotNull
    @Deprecated
        // please use QueryBuilder instead
    InOidFilter createInOid(Collection<String> oids);

    @NotNull
    @Deprecated
        // please use QueryBuilder instead
    InOidFilter createInOid(String... oids);

    @NotNull
    @Deprecated
        // please use QueryBuilder instead
    InOidFilter createOwnerHasOidIn(Collection<String> oids);

    @NotNull
    @Deprecated
        // please use QueryBuilder instead
    InOidFilter createOwnerHasOidIn(String... oids);

    @NotNull
    @Deprecated
        // please use QueryBuilder instead
    OrgFilter createOrg(PrismReferenceValue baseOrgRef, OrgFilter.Scope scope);

    @NotNull
    @Deprecated
        // please use QueryBuilder instead
    OrgFilter createOrg(String baseOrgOid, OrgFilter.Scope scope);

    @NotNull
    @Deprecated
        // please use QueryBuilder instead
    OrgFilter createRootOrg();

    @NotNull
    TypeFilter createType(QName type, ObjectFilter filter);

    @NotNull
    ObjectOrdering createOrdering(ItemPath orderBy, OrderDirection direction);

    @NotNull
    ObjectPaging createPaging(Integer offset, Integer maxSize);

    @NotNull
    ObjectPaging createPaging(Integer offset, Integer maxSize, ItemPath orderBy, OrderDirection direction);

    @NotNull
    ObjectPaging createPaging(Integer offset, Integer maxSize, List<ObjectOrdering> orderings);

    @NotNull
    ObjectPaging createPaging(ItemPath orderBy, OrderDirection direction);

    @NotNull
    ObjectPaging createPaging();

    @NotNull
    ObjectQuery createQuery();

    @NotNull
    ObjectQuery createQuery(ObjectFilter filter);

    @NotNull
    ObjectQuery createQuery(XNode condition, ObjectFilter filter);

    @NotNull
    ObjectQuery createQuery(ObjectPaging paging);

    @NotNull
    ObjectQuery createQuery(ObjectFilter filter, ObjectPaging paging);
}
