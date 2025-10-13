/*
 * Copyright (C) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.NotNull;

public interface ComplexCopyable<T extends ComplexCopyable<T>> {

    // Should be renamed to use "copy ..." instead of "clone ..."
    @NotNull T cloneComplex(@NotNull CloneStrategy strategy);
}
