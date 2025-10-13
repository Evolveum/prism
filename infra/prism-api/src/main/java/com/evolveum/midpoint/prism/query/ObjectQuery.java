/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query;

import java.io.Serializable;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.exception.SchemaException;

public interface ObjectQuery extends DebugDumpable, Serializable {

    ObjectFilter getFilter();

    void setFilter(ObjectFilter filter);

    void setPaging(ObjectPaging paging);

    ObjectPaging getPaging();

    boolean isAllowPartialResults();

    void setAllowPartialResults(boolean allowPartialResults);

    ObjectQuery clone();

    ObjectQuery cloneWithoutFilter();

    void addFilter(ObjectFilter objectFilter);

    // use when offset/maxSize is expected
    Integer getOffset();

    // use when offset/maxSize is expected
    Integer getMaxSize();

    boolean equivalent(Object o);

    boolean equals(Object o, boolean exact);

    // TODO decide what to do with these static methods

    // although we do our best to match even incomplete relations (null, unqualified), ultimately
    // it is the client's responsibility to ensure relations in object and filter are normalized (namely: null -> org:default)
    static <T extends Objectable> boolean match(
            PrismObject<T> object, ObjectFilter filter, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException {
        return filter.match(object.getValue(), matchingRuleRegistry);
    }

    // although we do our best to match even incomplete relations (null, unqualified), ultimately
    // it is the client's responsibility to ensure relations in object and filter are normalized (namely: null -> org:default)
    static boolean match(Containerable object, ObjectFilter filter, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException {
        return filter.match(object.asPrismContainerValue(), matchingRuleRegistry);
    }
}
