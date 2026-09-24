/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * == For methods
 *
 * Marks a method that is safe to be called from an untrusted script.
 *
 * - No access to system internals.
 * - No state changes, only queries.
 *
 * BEWARE: Current implementation in midPoint looks only at methods' implementations.
 * So marking a method as {@link Safe} on an interface may signal an intent, but by itself does not make it safe.
 * The implementation must be marked as {@link Safe} as well.
 *
 * == For types (classes and interfaces)
 *
 * Marks a type that is safe to pass into an untrusted script.
 * Does NOT mean that all methods of the type are safe. Only those methods that are marked with {@link Safe} are safe.
 *
 * BEWARE: Current implementation in midPoint looks not only at supertypes and interfaces of a class when determining
 * if it's safe; it considers also _annotations_ of the class. So if any of them is marked as @Safe, the class will be
 * considered safe as well. This is not a problem, but it is something to be aware of.
 *
 * So, DO NOT mark any annotation as @Safe unless you really mean it.
 *
 * == Limitations
 *
 * The semantics of this annotation is not clearly defined. Current use is to denote what can be called from Velocity templates.
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
public @interface Safe {
}
