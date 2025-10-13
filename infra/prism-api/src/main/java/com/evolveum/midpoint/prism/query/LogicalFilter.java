/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.query;

import java.util.List;
import java.util.function.Consumer;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.TypedItemPath;

/**
 *
 */
public interface LogicalFilter extends ObjectFilter {

    List<ObjectFilter> getConditions();

    void setConditions(List<ObjectFilter> condition);

    void addCondition(ObjectFilter condition);

    boolean contains(ObjectFilter condition);

    LogicalFilter cloneEmpty();

    //List<ObjectFilter> getClonedConditions();

    boolean isEmpty();

    @Override
    void checkConsistence(boolean requireDefinitions);

    @Override
    void accept(Visitor visitor);

    @Override
    default boolean matchesOnly(ItemPath... paths) {
        for (ObjectFilter condition : getConditions()) {
            if (!condition.matchesOnly(paths)) {
                return false;
            }
        }
        return true;
    }
    @Override
    default void collectUsedPaths(TypedItemPath base, Consumer<TypedItemPath> pathConsumer, boolean expandReferences) {
        for (var condition : getConditions()) {
            condition.collectUsedPaths(base, pathConsumer, expandReferences);
        }
    }

    @Override
    default void transformItemPaths(ItemPath parentPath, ItemDefinition<?> parentDef, FilterItemPathTransformer transformer) {
        for (var condition : getConditions()) {
            condition.transformItemPaths(parentPath, parentDef, transformer);
        }
    }
}
