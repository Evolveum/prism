/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.path.ItemPath;

/** To be used e.g. by query or delta builders. */
public interface ItemDefinitionResolver {

    /**
     * Tries to determine a definition for given item within given type.
     * May return null if the definition could not be found. (The query/delta builder then may try another way of finding it.)
     */
    @Nullable ItemDefinition<?> findItemDefinition(@NotNull Class<? extends Containerable> type, @NotNull ItemPath itemPath);
}
