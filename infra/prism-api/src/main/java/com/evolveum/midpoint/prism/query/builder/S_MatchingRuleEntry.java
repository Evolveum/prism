/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query.builder;

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
}
