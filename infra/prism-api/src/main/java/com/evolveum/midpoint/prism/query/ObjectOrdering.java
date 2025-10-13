/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query;

import java.io.Serializable;

import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.path.ItemPath;

/**
 * Describes definition of object ordering.
 */
public interface ObjectOrdering extends Serializable {

    /**
     * Item path by which to order - should not be null or empty.
     */
    ItemPath getOrderBy();

    /**
     * Ordering direction, can return null, which should be interpreted as ascending.
     */
    @Nullable OrderDirection getDirection();

    boolean equals(Object o, boolean exact);
}
