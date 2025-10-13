/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;

import org.jetbrains.annotations.Nullable;

public interface FilterItemPathTransformer {

    /**
     *
     * @param parentPath Item Path of parent filter, to which paths are relative  in filter
     * @param parentDefinition Definition of parent filter.
     * @param filter actual filter to be transformed.
     * @return null, if item path should be unchanged, otherwise new item path
     */
    @Nullable ItemPath transform(ItemPath parentPath, ItemDefinition<?> parentDefinition, ItemFilter filter);



}
