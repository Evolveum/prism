/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.delta.ItemMerger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemMergerFactory {

    @Nullable
    ItemMerger createMerger(@NotNull ItemDefinition<?> definition, @NotNull MergeStrategy strategy, @Nullable OriginMarker originMarker);
}
