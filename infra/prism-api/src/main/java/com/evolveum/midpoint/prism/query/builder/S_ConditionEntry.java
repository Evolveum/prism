/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query.builder;

import java.util.Collection;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.ExpressionWrapper;
import com.evolveum.midpoint.prism.PrismProperty;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.query.FuzzyStringMatchFilter;
import com.evolveum.midpoint.prism.query.FuzzyStringMatchFilter.FuzzyMatchingMethod;
import com.evolveum.midpoint.prism.query.FuzzyStringMatchFilter.Similarity;
import com.evolveum.midpoint.prism.query.RefFilter;

/**
 * See the grammar in Javadoc for {@code QueryBuilder}.
 */
public interface S_ConditionEntry {

    /**
     * See https://docs.evolveum.com/midpoint/reference/concepts/query/query-api/[Query API] docs
     * about support of multiple values (generally good in the new Native repository with IN semantics).
     * For multi-value properties the semantics is ANY IN (non-empty intersection is a match).
     */
    S_MatchingRuleEntry eq(Object... values);

    // TODO implement something like itemAs(property) to copy the property definition, path, and values into filter
    <T> S_MatchingRuleEntry eq(PrismProperty<T> property);

    S_RightHandItemEntry eq();
    S_MatchingRuleEntry eqPoly(String orig, String norm);
    S_MatchingRuleEntry eqPoly(String orig);
    S_MatchingRuleEntry gt(Object value);
    S_RightHandItemEntry gt();
    S_MatchingRuleEntry ge(Object value);
    S_RightHandItemEntry ge();
    S_MatchingRuleEntry lt(Object value);
    S_RightHandItemEntry lt();
    S_MatchingRuleEntry le(Object value);
    S_RightHandItemEntry le();
    S_MatchingRuleEntry startsWith(Object value);
    S_MatchingRuleEntry startsWithPoly(String orig, String norm);
    S_MatchingRuleEntry startsWithPoly(String orig);
    S_MatchingRuleEntry endsWith(Object value);
    S_MatchingRuleEntry endsWithPoly(String orig, String norm);
    S_MatchingRuleEntry endsWithPoly(String orig);
    S_MatchingRuleEntry contains(Object value);
    S_MatchingRuleEntry containsPoly(String orig, String norm);
    S_MatchingRuleEntry containsPoly(String orig);
    S_FilterExit refRelation(QName... relations);
    S_FilterExit refType(QName... targetTypeName);
    S_FilterExit ref(PrismReferenceValue... value);
    S_FilterExit ref(Collection<PrismReferenceValue> values);
    S_FilterExit ref(Collection<PrismReferenceValue> values, boolean nullTypeAsAny);                          // beware, nullTypeAsAny=false is supported only by built-in match(..) method
    S_FilterExit ref(Collection<PrismReferenceValue> values, boolean nullOidAsAny, boolean nullTypeAsAny);    // beware, nullTypeAsAny=false and nullOidAsAny=false are supported only by built-in match(..) method
    S_FilterExit ref(ExpressionWrapper expression);
    S_FilterExit ref(RefFilter filter);

    /**
     * Creates filter matching any of provided OIDs; works like oid is Any with no/null OID.
     */
    S_FilterExit ref(String... oid);

    /**
     * Creates filter matching oid and/or targetTypeName, any of them optional.
     * If both are null the result is the same as {@link #isNull()} (null ref OID matches).
     */
    default S_FilterExit ref(@Nullable String oid, @Nullable QName targetTypeName) {
        return ref(oid, targetTypeName, null);
    }

    S_FilterExit ref(@Nullable String oid, @Nullable QName targetTypeName, @Nullable QName relation);
    S_FilterExit isNull();

    default S_FilterExit fuzzyString(String value, FuzzyMatchingMethod method) {
        return fuzzyString(value).method(method);
    }

    FuzzyStringBuilder fuzzyString(String... values);

    interface FuzzyStringBuilder {
        FuzzyStringBuilder value(String value);

        S_FilterExit method(FuzzyMatchingMethod method);

        default S_FilterExit levenstein(int threshold, boolean inclusive) {
            return method(FuzzyStringMatchFilter.levenstein(threshold, inclusive));
        }

        default S_FilterExit similarity(float threshold, boolean inclusive) {
            return method(FuzzyStringMatchFilter.similarity(threshold, inclusive));
        }

    }
}
