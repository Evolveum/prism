/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.ExpressionWrapper;

import org.jetbrains.annotations.Nullable;

/** Interface for filters that can have an expression on the right side. */
public interface ExpressionAware {

    @Nullable ExpressionWrapper getExpression();

    void setExpression(@Nullable ExpressionWrapper expression);
}
