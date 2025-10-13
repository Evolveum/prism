/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.exception;

import org.jetbrains.annotations.Nullable;

/**
 * An exception that can provide the {@link ExceptionContext}.
 */
public interface ExceptionContextAware {

    @Nullable ExceptionContext getContext();
}
