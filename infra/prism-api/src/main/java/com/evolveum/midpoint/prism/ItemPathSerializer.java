/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.path.ItemPath;

import org.jetbrains.annotations.NotNull;

public interface ItemPathSerializer {

    /** Serializes item path with all the namespaces explicitly listed. */
    String serializeStandalone(@NotNull ItemPath itemPath);
}
