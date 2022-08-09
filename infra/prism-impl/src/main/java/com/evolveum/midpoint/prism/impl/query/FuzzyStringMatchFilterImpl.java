package com.evolveum.midpoint.prism.impl.query;

import java.util.List;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.FuzzyStringMatchFilter;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

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
    public boolean match(PrismContainerValue<?> cvalue, MatchingRuleRegistry matchingRuleRegistry)
            throws SchemaException {
        // FIXME: levenshtein / similarity could be implemented in java
        throw new SchemaException("Supported only in repository.");
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
