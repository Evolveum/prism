/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
