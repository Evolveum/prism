/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.query;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.path.ItemPath;

import javax.xml.namespace.QName;

/**
 *
 */
public interface TypeFilter extends ObjectFilter {

    @NotNull
    QName getType();

    ObjectFilter getFilter();

    void setFilter(ObjectFilter filter);

    @Override
    TypeFilter clone();

    TypeFilter cloneEmpty();

    @Override
    default boolean matchesOnly(ItemPath... paths) {
        return  getFilter() == null || getFilter().matchesOnly(paths);
    }

}
