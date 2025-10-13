/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.path.ItemPath;

/**
 *
 */
public interface NoneFilter extends ObjectFilter {

    @Override
    default boolean matchesOnly(ItemPath... paths) {
        return true;
    }
}
