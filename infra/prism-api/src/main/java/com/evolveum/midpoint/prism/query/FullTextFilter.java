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
 *
 */
public interface FullTextFilter extends ObjectFilter {

    Collection<String> getValues();

    void setValues(Collection<String> values);

    ExpressionWrapper getExpression();

    void setExpression(ExpressionWrapper expression);

    @Override
    FullTextFilter clone();

    @Override
    default boolean matchesOnly(ItemPath... paths) {
        return false;
    }

}
