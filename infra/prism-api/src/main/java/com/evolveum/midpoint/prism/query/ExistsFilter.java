/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;

public interface ExistsFilter extends ObjectFilter, ItemFilter {

    ItemDefinition<?> getDefinition();

    ObjectFilter getFilter();

    void setFilter(ObjectFilter filter);

    @Override
    ExistsFilter clone();

    ExistsFilter cloneEmpty();

    @Override
    default boolean matchesOnly(ItemPath... paths) {
        for (ItemPath path : paths) {
            if (path.equals(getFullPath(), false)) {
                return true;
            }
        }
        return false;
    }
}
