/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.ExpressionWrapper;
import com.evolveum.midpoint.prism.path.ItemPath;

import java.util.Collection;

/**
 *  TODO create a better name for this filter
 */
public interface InOidFilter extends ObjectFilter {

    Collection<String> getOids();

    void setOids(Collection<String> oids);

    boolean isConsiderOwner();

    ExpressionWrapper getExpression();

    void setExpression(ExpressionWrapper expression);

    @Override
    InOidFilter clone();


    @Override
    default boolean matchesOnly(ItemPath... paths) {
        for (ItemPath path : paths) {
            if (ItemPath.EMPTY_PATH.equals(path, false)) {
                return true;
            }
        }
        return false;
    }

}
