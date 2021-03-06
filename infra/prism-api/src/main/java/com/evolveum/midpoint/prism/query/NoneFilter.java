/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
