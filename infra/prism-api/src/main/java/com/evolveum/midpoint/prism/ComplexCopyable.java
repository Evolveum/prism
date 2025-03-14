/*
 * Copyright (C) 2010-2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.NotNull;

public interface ComplexCopyable<T extends ComplexCopyable<T>> {

    // Should be renamed to use "copy ..." instead of "clone ..."
    @NotNull T cloneComplex(@NotNull CloneStrategy strategy);
}
