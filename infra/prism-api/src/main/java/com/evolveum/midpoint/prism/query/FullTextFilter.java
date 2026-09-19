/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.path.ItemPath;

import java.util.Collection;

public interface FullTextFilter extends ObjectFilter, ExpressionAware {

    Collection<String> getValues();

    void setValues(Collection<String> values);

    @Override
    FullTextFilter clone();

    @Override
    default boolean matchesOnly(ItemPath... paths) {
        return false;
    }

}
