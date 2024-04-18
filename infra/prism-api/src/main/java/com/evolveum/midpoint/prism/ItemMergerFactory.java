/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.delta.ItemMerger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemMergerFactory {

    @Nullable
    ItemMerger createMerger(@NotNull ItemDefinition<?> definition, @NotNull MergeStrategy strategy, @Nullable OriginMarker originMarker);
}
