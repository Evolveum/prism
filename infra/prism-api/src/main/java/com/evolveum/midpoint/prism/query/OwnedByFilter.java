/*
 * Copyright (C) 2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
