/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
