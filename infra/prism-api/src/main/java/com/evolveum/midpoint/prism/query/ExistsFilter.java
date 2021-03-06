/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
