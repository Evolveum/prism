package com.evolveum.midpoint.prism.impl.query;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.ExpressionWrapper;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.FuzzyStringMatchFilter;
import com.evolveum.midpoint.util.exception.SchemaException;

public class FuzzyStringMatchFilterImpl<T> extends PropertyValueFilterImpl<T> implements FuzzyStringMatchFilter<T> {

    @NotNull
    private FuzzyMatchingMethod matchingMethod;

    FuzzyStringMatchFilterImpl(@NotNull ItemPath path, @NotNull FuzzyMatchingMethod matchingMethod,
            @Nullable PrismPropertyDefinition<T> definition,
            @Nullable QName matchingRule, @Nullable List<PrismPropertyValue<T>> values,
            @Nullable ExpressionWrapper expression, @Nullable ItemPath rightHandSidePath,
            @Nullable ItemDefinition rightHandSideDefinition) {
        super(path, definition, matchingRule, values, expression, rightHandSidePath, rightHandSideDefinition);
        this.matchingMethod = matchingMethod;
    }

    @Override
    public FuzzyMatchingMethod getMatchingMethod() {
        return matchingMethod;
    }

    @Override
    public PropertyValueFilterImpl<T> clone() {
        var valuesClone = getValues().stream().map(PrismPropertyValue::clone).collect(Collectors.toList());
        return new FuzzyStringMatchFilterImpl<>(getFullPath(), matchingMethod, getDefinition(), getDeclaredMatchingRule(),
                valuesClone, getExpression(), getRightHandSidePath(), getRightHandSideDefinition());
    }

    @Override
    public boolean match(PrismContainerValue<?> cvalue, MatchingRuleRegistry matchingRuleRegistry)
            throws SchemaException {
        // FIXME: levenstein / similarity could be implemented in java
        throw new SchemaException("Supported only in repository.");
    }

    @Override
    protected String getFilterName() {
        if (matchingMethod == null) {
            return "fuzzyStringMatch";
        }
        return getMatchingMethod().getMethodName().getLocalPart();
    }

    public static <T> FuzzyStringMatchFilterImpl<T> create(ItemPath itemPath, PrismPropertyDefinition<T> propertyDefinition,
            FuzzyMatchingMethod method, List<PrismPropertyValue<T>> values) {
        return new FuzzyStringMatchFilterImpl<T>(itemPath, method, propertyDefinition, null, values,
                null, null, null);
    }

}
