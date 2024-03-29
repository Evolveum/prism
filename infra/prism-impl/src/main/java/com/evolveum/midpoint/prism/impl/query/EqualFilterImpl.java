/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.EqualFilter;
import com.evolveum.midpoint.prism.query.ValueFilter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.prism.xml.ns._public.types_3.RawType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;

import static com.evolveum.midpoint.util.MiscUtil.emptyIfNull;

public class EqualFilterImpl<T> extends PropertyValueFilterImpl<T> implements EqualFilter<T> {
    private static final long serialVersionUID = 3284478412180258355L;

    /*
     *  The pattern for factory methods and constructors signatures is:
     *   - path and definition
     *   - matching rule (if applicable)
     *   - values (incl. prismContext if needed)
     *   - expressionWrapper
     *   - right hand things
     *   - filter-specific flags (equal, anchors)
     *
     *  Ordering of methods:
     *   - constructor
     *   - factory methods: [null], value(s), expression, right-side
     *   - match
     *   - equals
     *
     *  Parent for prism values is set in the appropriate constructor; so there's no need to do that at other places.
     *
     *  Normalization of "Object"-typed values is done in anyArrayToXXX and anyValueToXXX methods. This includes cloning
     *  of values that have a parent (note that we recompute the PolyString values as part of conversion process; if that's
     *  a problem for the client, it has to do cloning itself).
     *
     *  Please respect these conventions in order to make these classes understandable and maintainable.
     */

    public EqualFilterImpl(@NotNull ItemPath path, @Nullable PrismPropertyDefinition<T> definition,
            @Nullable QName matchingRule,
            @Nullable List<PrismPropertyValue<T>> prismPropertyValues,
            @Nullable ExpressionWrapper expression, @Nullable ItemPath rightHandSidePath,
            @Nullable ItemDefinition<?> rightHandSideDefinition) {
        super(path, definition, matchingRule, prismPropertyValues, expression, rightHandSidePath, rightHandSideDefinition);
    }

    // factory methods

    // empty (different from values as it generates filter with null 'values' attribute)
    @NotNull
    public static <T> EqualFilter<T> createEqual(@NotNull ItemPath path, @Nullable PrismPropertyDefinition<T> definition,
            @Nullable QName matchingRule) {
        return new EqualFilterImpl<>(path, definition, matchingRule, null, null, null, null);
    }

    // values
    @NotNull
    public static <T> EqualFilter<T> createEqual(@NotNull ItemPath path, @Nullable PrismPropertyDefinition<T> definition,
            @Nullable QName matchingRule, Object... values) {
        List<PrismPropertyValue<T>> propertyValues = anyArrayToPropertyValueList(values);
        return new EqualFilterImpl<>(path, definition, matchingRule, propertyValues, null, null, null);
    }

    // expression-related
    @NotNull
    public static <T> EqualFilter<T> createEqual(@NotNull ItemPath path, @Nullable PrismPropertyDefinition<T> definition,
            @Nullable QName matchingRule, @NotNull ExpressionWrapper expression) {
        return new EqualFilterImpl<>(path, definition, matchingRule, null, expression, null, null);
    }

    // right-side-related; right side can be supplied later (therefore it's nullable)
    @NotNull
    public static <T> EqualFilter<T> createEqual(@NotNull ItemPath propertyPath, PrismPropertyDefinition<T> propertyDefinition,
            QName matchingRule, @NotNull ItemPath rightSidePath, ItemDefinition<?> rightSideDefinition) {
        return new EqualFilterImpl<>(propertyPath, propertyDefinition, matchingRule, null, null, rightSidePath, rightSideDefinition);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public EqualFilterImpl<T> clone() {
        return new EqualFilterImpl<>(getFullPath(), getDefinition(), getMatchingRule(), getClonedValues(),
                getExpression(), getRightHandSidePath(), getRightHandSideDefinition());
    }

    @Override
    public ValueFilter<PrismPropertyValue<T>, PrismPropertyDefinition<T>> nested(ItemPath prefix) {
        return new EqualFilterImpl<>(getFullPath().rest(prefix.size()), getDefinition(), getMatchingRule(), getClonedValues(),
                getExpression(), getRightHandSidePath(), getRightHandSideDefinition());
    }

    @Override
    protected String getFilterName() {
        return "EQUAL";
    }

    @Override
    public boolean match(PrismContainerValue<?> objectValue, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException {
        Collection<PrismValue> objectItemValues = getObjectItemValues(objectValue);
        Collection<? extends PrismValue> filterValues = emptyIfNull(getValues());
        if (objectItemValues.isEmpty()) {
            return filterValues.isEmpty();
        }
        MatchingRule<?> matchingRule = getMatchingRuleFromRegistry(matchingRuleRegistry);
        for (PrismValue filterItemValue : filterValues) {
            checkPrismPropertyValue(filterItemValue);
            for (PrismValue objectItemValue : objectItemValues) {
                checkPrismPropertyValue(objectItemValue);
                if (matches((PrismPropertyValue<?>) filterItemValue, (PrismPropertyValue<?>) objectItemValue, matchingRule)) {
                    return true;
                }
            }
        }
        return false;
    }

    private <T1> boolean matches(PrismPropertyValue<?> filterValue, PrismPropertyValue<?> objectValue, MatchingRule<T1> matchingRule) {
        Object filterRealValue = filterValue.getRealValue();
        Object objectRealValue = objectValue.getRealValue();
        try {
            if (!(objectRealValue instanceof RawType)) {
                //noinspection unchecked
                return matchingRule.match((T1) filterRealValue, (T1) objectRealValue);
            } else {
                PrismPropertyDefinition<?> definition = getDefinition();
                if (definition != null) {
                    // We clone here to avoid modifying original data structure.
                    Object parsedObjectRealValue = ((RawType) objectRealValue).clone().getParsedRealValue(definition, definition.getItemName());
                    //noinspection unchecked
                    return matchingRule.match((T1) filterRealValue, (T1) parsedObjectRealValue);
                } else {
                    throw new IllegalStateException("Couldn't compare raw value with definition-less filter value: " + filterRealValue);
                }
            }
        } catch (SchemaException e) {
            throw new SystemException("Schema exception while comparing objects: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object obj, boolean exact) {
        return obj instanceof EqualFilter && super.equals(obj, exact);
    }

}

