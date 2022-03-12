/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugDumpable;

public interface ObjectPaging extends DebugDumpable, Serializable {

    OrderDirection getPrimaryOrderingDirection();

    ItemPath getPrimaryOrderingPath();

    ObjectOrdering getPrimaryOrdering();

    // TODO name?
    List<? extends ObjectOrdering> getOrderingInstructions();

    boolean hasOrdering();

    void setOrdering(ItemPath orderBy, OrderDirection direction);

    boolean hasCookie();

    void addOrderingInstruction(ItemPath orderBy, OrderDirection direction);

    @SuppressWarnings("NullableProblems")
    void setOrdering(ObjectOrdering... orderings);

    void setOrdering(Collection<? extends ObjectOrdering> orderings);

    Integer getOffset();

    void setOffset(Integer offset);

    Integer getMaxSize();

    void setMaxSize(Integer maxSize);

    /**
     * Returns the paging cookie. The paging cookie is used for optimization of paged searches.
     * The presence of the cookie may allow the data store to correlate queries and associate
     * them with the same server-side context. This may allow the data store to reuse the same
     * pre-computed data. We want this as the sorted and paged searches may be quite expensive.
     * It is expected that the cookie returned from the search will be passed back in the options
     * when the next page of the same search is requested.
     *
     * It is OK to initialize a search without any cookie. If the datastore utilizes a re-usable
     * context it will return a cookie in a search response.
     */
    String getCookie();

    /**
     * Sets paging cookie. The paging cookie is used for optimization of paged searches.
     * The presence of the cookie may allow the data store to correlate queries and associate
     * them with the same server-side context. This may allow the data store to reuse the same
     * pre-computed data. We want this as the sorted and paged searches may be quite expensive.
     * It is expected that the cookie returned from the search will be passed back in the options
     * when the next page of the same search is requested.
     *
     * It is OK to initialize a search without any cookie. If the datastore utilizes a re-usable
     * context it will return a cookie in a search response.
     */
    void setCookie(String cookie);

    ObjectPaging clone();

    boolean equals(Object o, boolean exact);
}
