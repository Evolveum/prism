/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.prism.xml.ns._public.types_3.RawType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.FuzzyStringMatchFilter;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

import static com.evolveum.midpoint.util.MiscUtil.argCheck;

public class FuzzyStringMatchFilterImpl<T> extends PropertyValueFilterImpl<T> implements FuzzyStringMatchFilter<T> {

    @NotNull private final FuzzyMatchingMethod matchingMethod;

    private FuzzyStringMatchFilterImpl(@NotNull ItemPath path, @NotNull FuzzyMatchingMethod matchingMethod,
            @Nullable PrismPropertyDefinition<T> definition,
            @Nullable QName matchingRule, @Nullable List<PrismPropertyValue<T>> values,
            @Nullable ExpressionWrapper expression, @Nullable ItemPath rightHandSidePath,
            @Nullable ItemDefinition<?> rightHandSideDefinition) {
        super(path, definition, matchingRule, values, expression, rightHandSidePath, rightHandSideDefinition);
        this.matchingMethod = matchingMethod;
    }

    @Override
    public @NotNull FuzzyMatchingMethod getMatchingMethod() {
        return matchingMethod;
    }

    @Override
    public PropertyValueFilterImpl<T> clone() {
        List<PrismPropertyValue<T>> values = getValues();
        var valuesClone =
                values != null ? values.stream().map(PrismPropertyValue::clone).collect(Collectors.toList()) : null;
        return new FuzzyStringMatchFilterImpl<>(getFullPath(), matchingMethod, getDefinition(), getDeclaredMatchingRule(),
                valuesClone, getExpression(), getRightHandSidePath(), getRightHandSideDefinition());
    }

    @Override
    public boolean match(PrismContainerValue<?> cValue, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException {
        Collection<PrismValue> objectItemValues = getObjectItemValues(cValue);
        PrismPropertyValue<T> filterPropValue = getSingleValue();
        if (filterPropValue == null) {
            // TODO Is this OK? What does PostgreSQL in such a case?
            return objectItemValues.isEmpty();
        }
        for (PrismValue objectItemValue : objectItemValues) {
            checkPrismPropertyValue(objectItemValue);
            if (matches(filterPropValue, (PrismPropertyValue<?>) objectItemValue)) {
                return true;
            }
        }
        return false;
    }

    // TODO deduplicate with other similar methods
    private boolean matches(PrismPropertyValue<T> filterValue, PrismPropertyValue<?> objectValue) {
        Object filterRealValue = filterValue.getRealValue();
        Object objectRealValue = objectValue.getRealValue();
        try {
            if (!(objectRealValue instanceof RawType)) {
                return matches(filterRealValue, objectRealValue);
            } else {
                PrismPropertyDefinition<?> definition = getDefinition();
                if (definition != null) {
                    // We clone here to avoid modifying original data structure.
                    Object parsedObjectRealValue = ((RawType) objectRealValue).clone()
                            .getParsedRealValue(definition, definition.getItemName());
                    return matches(filterRealValue, parsedObjectRealValue);
                } else {
                    throw new IllegalStateException(
                            "Couldn't compare raw value with definition-less filter value: " + filterRealValue);
                }
            }
        } catch (SchemaException e) {
            throw new SystemException("Schema exception while comparing objects: " + e.getMessage(), e);
        }
    }

    private boolean matches(Object filterValue, Object objectValue) {
        // TODO is this treatment ok?
        argCheck(filterValue != null, "Filter real value must not be null in %s", this);
        argCheck(objectValue != null, "Object real value must not be null in %s", this);
        String filterStringValue = String.valueOf(filterValue);
        String objectStringValue = String.valueOf(objectValue);
        return matchingMethod.matches(filterStringValue, objectStringValue);
    }

    @Override
    protected String getFilterName() {
        return getMatchingMethod().getMethodName().getLocalPart();
    }

    @Override
    protected void debugDump(int indent, StringBuilder sb) {
        super.debugDump(indent, sb);
        sb.append("\n");
        DebugUtil.debugDumpWithLabel(sb, "METHOD", String.valueOf(matchingMethod), indent + 1);
    }

    public static <T> FuzzyStringMatchFilterImpl<T> create(ItemPath itemPath, PrismPropertyDefinition<T> propertyDefinition,
            FuzzyMatchingMethod method, List<PrismPropertyValue<T>> values) {
        return new FuzzyStringMatchFilterImpl<>(itemPath, method, propertyDefinition, null, values,
                null, null, null);
    }

}
