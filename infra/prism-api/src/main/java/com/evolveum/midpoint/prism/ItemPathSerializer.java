/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.path.ItemPath;

import org.jetbrains.annotations.NotNull;

public interface ItemPathSerializer {

    /** Serializes item path with all the namespaces explicitly listed. */
    String serializeStandalone(@NotNull ItemPath itemPath);
}
