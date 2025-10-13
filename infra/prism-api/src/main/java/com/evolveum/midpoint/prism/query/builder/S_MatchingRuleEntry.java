/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query.builder;

import static com.evolveum.midpoint.prism.PrismConstants.NS_MATCHING_RULE;

import javax.xml.namespace.QName;

/**
 * See the grammar in Javadoc for {@code QueryBuilder}.
 */
public interface S_MatchingRuleEntry extends S_FilterExit {
    S_FilterExit matchingOrig();
    S_FilterExit matchingNorm();
    S_FilterExit matchingStrict();
    S_FilterExit matchingCaseIgnore();
    S_FilterExit matching(QName matchingRuleName);

    default S_FilterExit matching(String matchingRuleNameLocalPart) {
        return matching(new QName(NS_MATCHING_RULE, matchingRuleNameLocalPart));
    }
}
