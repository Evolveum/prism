/*
 * Copyright (C) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;

public interface OwnedByFilter extends ObjectFilter {

    @NotNull
    ComplexTypeDefinition getType();

    @Nullable
    ItemPath getPath();

    ObjectFilter getFilter();


}
