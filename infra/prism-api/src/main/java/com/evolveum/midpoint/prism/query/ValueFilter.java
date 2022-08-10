/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query;

import java.util.List;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

public interface ValueFilter<V extends PrismValue, D extends ItemDefinition<?>>
        extends ObjectFilter, ItemFilter, Itemable {

    @NotNull
    @Override
    ItemPath getFullPath();

    @NotNull
    ItemPath getParentPath();

    @Override
    @NotNull
    ItemName getElementName();

    @Override
    @Nullable
    D getDefinition();

    void setDefinition(@Nullable D definition);

    @Nullable
    QName getMatchingRule();

    @Nullable
    QName getDeclaredMatchingRule();

    void setMatchingRule(@Nullable QName matchingRule);

    @Nullable
    List<V> getValues();

    /**
     * Returns true if there are no values (list is null or empty).
     * If false is returned, then {@link #getValues()} is definitely not null.
     */
    default boolean hasNoValue() {
        List<V> values = getValues();
        return values == null || values.isEmpty();
    }

    /**
     * Returns single value or {@code null}, throws exception if multiple values are present.
     */
    @Nullable
    V getSingleValue();

    /**
     * @param value value, has to be parent-less
     */
    void setValue(V value);

    @Nullable
    ExpressionWrapper getExpression();

    void setExpression(@Nullable ExpressionWrapper expression);

    @Nullable
    ItemPath getRightHandSidePath();

    void setRightHandSidePath(@Nullable ItemPath rightHandSidePath);

    @Nullable
    ItemDefinition<?> getRightHandSideDefinition();

    void setRightHandSideDefinition(@Nullable ItemDefinition<?> rightHandSideDefinition);

    @Override
    ItemPath getPath();

    boolean isRaw();

    // TODO revise
    @Override
    boolean match(PrismContainerValue<?> cvalue, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException;

    @Override
    ValueFilter<V, D> clone();

    @Override
    boolean equals(Object o, boolean exact);

    @Override
    void checkConsistence(boolean requireDefinitions);

    @Override
    default boolean matchesOnly(ItemPath... paths) {
        for (ItemPath path : paths) {
            if (path.equals(getFullPath(), false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true, if current filter can be rewritten to be nested inside exists filter with prefix path
     *
     * This means filter with path prefix/item will become prefix exists (item)
     *
     * @param potential exists path
     * @return true if filter can be rewritten and nested inside exists
     */
    @Experimental
    boolean canNestInsideExists(ItemPath existsPath);

    @Experimental
    ValueFilter<V, D> nested(ItemPath existsPath);
}
