/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method that is safe to be called from an untrusted script.
 *
 * - No access to system internals.
 * - No state changes, only queries.
 *
 * Limitation: The semantics is not clearly defined. Current use is to denote what can be called from Velocity templates.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Safe {
}
