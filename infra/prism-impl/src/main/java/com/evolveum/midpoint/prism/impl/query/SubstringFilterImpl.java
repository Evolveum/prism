/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.SubstringFilter;
import com.evolveum.midpoint.prism.query.ValueFilter;
import com.evolveum.midpoint.util.exception.SchemaException;

public final class SubstringFilterImpl<T> extends PropertyValueFilterImpl<T>
        implements SubstringFilter<T> {

    private final boolean anchorStart;
    private final boolean anchorEnd;

    private SubstringFilterImpl(@NotNull ItemPath path, @Nullable PrismPropertyDefinition<T> definition,
            @Nullable QName matchingRule,
            @Nullable List<PrismPropertyValue<T>> prismPropertyValues,
            @Nullable ExpressionWrapper expression, boolean anchorStart, boolean anchorEnd) {
        super(path, definition, matchingRule, prismPropertyValues, expression, null, null);
        this.anchorStart = anchorStart;
        this.anchorEnd = anchorEnd;
    }

    /**
     * Creates a substring filter.
     *
     * @param itemDefinition TODO about nullability
     * @param anyValue real value or prism property value; TODO about nullability
     */
    public static <T> SubstringFilter<T> createSubstring(
            @NotNull ItemPath path, @Nullable PrismPropertyDefinition<T> itemDefinition,
            @Nullable QName matchingRule, Object anyValue,
            boolean anchorStart, boolean anchorEnd) {
        List<PrismPropertyValue<T>> values = anyValueToPropertyValueList(anyValue);
        return new SubstringFilterImpl<>(path, itemDefinition, matchingRule, values, null, anchorStart, anchorEnd);
    }

    public static <T> SubstringFilter<T> createSubstring(
            @NotNull ItemPath path, @Nullable PrismPropertyDefinition<T> itemDefinition,
            @Nullable QName matchingRule, ExpressionWrapper expressionWrapper,
            boolean anchorStart, boolean anchorEnd) {
        return new SubstringFilterImpl<>(path, itemDefinition, matchingRule, null, expressionWrapper, anchorStart, anchorEnd);
    }

    @Override
    public boolean isAnchorStart() {
        return anchorStart;
    }

    @Override
    public boolean isAnchorEnd() {
        return anchorEnd;
    }

    @Override
    public SubstringFilterImpl<T> clone() {
        return new SubstringFilterImpl<>(getFullPath(), getDefinition(), getMatchingRule(), getClonedValues(),
                getExpression(), anchorStart, anchorEnd);
    }

    @Override
    protected String getFilterName() {
        return "SUBSTRING("
                + (anchorStart ? "S" : "")
                + (anchorStart && anchorEnd ? "," : "")
                + (anchorEnd ? "E" : "")
                + ")";
    }

    @Override
    public boolean match(PrismContainerValue<?> containerValue, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException {
        Collection<PrismValue> objectItemValues = getObjectItemValues(containerValue);

        MatchingRule<Object> matching = getMatchingRuleFromRegistry(matchingRuleRegistry);

        for (Object val : objectItemValues) {
            if (val instanceof PrismPropertyValue) {
                Object value = ((PrismPropertyValue<?>) val).getValue();
                for (Object o : toRealValues()) {
                    if (o == null) {
                        continue;            // shouldn't occur
                    }
                    StringBuilder sb = new StringBuilder();
                    if (!anchorStart) {
                        sb.append(".*");
                    }
                    sb.append(Pattern.quote(o.toString()));
                    if (!anchorEnd) {
                        sb.append(".*");
                    }
                    if (matching.matchRegex(value, sb.toString())) {
                        return true;
                    }
                }
            }
            if (val instanceof PrismReferenceValue) {
                throw new UnsupportedOperationException(
                        "matching substring on the prism reference value not supported yet");
            }
        }

        return false;
    }

    private Set<T> toRealValues() {
        return PrismValueCollectionsUtil.getRealValuesOfCollection(getValues());
    }

    @Override
    public boolean equals(Object o, boolean exact) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SubstringFilterImpl<?> that = (SubstringFilterImpl<?>) o;
        return super.equals(o, exact)
                && anchorStart == that.anchorStart
                && anchorEnd == that.anchorEnd;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return equals(o, true);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), anchorStart, anchorEnd);
    }

    @Override
    public ValueFilter<PrismPropertyValue<T>, PrismPropertyDefinition<T>> nested(ItemPath existsPath) {
        return new SubstringFilterImpl<>(getFullPath().rest(existsPath.size()), getDefinition(), getMatchingRule(), getClonedValues(),
                getExpression(), anchorStart, anchorEnd);
    }
}
