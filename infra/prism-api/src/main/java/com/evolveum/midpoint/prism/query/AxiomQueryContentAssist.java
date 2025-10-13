/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.ItemDefinition;
import org.jetbrains.annotations.Nullable;

public interface AxiomQueryContentAssist {

    ContentAssist process(@Nullable ItemDefinition<?> rootItem, String query);
    ContentAssist process(@Nullable ItemDefinition<?> rootItem, String query, int cursorPosition);
}
