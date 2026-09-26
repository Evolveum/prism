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
 * When determing the safety of a method, current implementation in midPoint looks not only at the method itself, but
 * also at the methods it overrides/implements (if any). If any of them is marked as {@link Safe}, the method is
 * considered safe as well. It inspects their annotations as well. See the warning below.
 *
 * == For types (classes and interfaces)
 *
 * Marks a type that is safe to pass into an untrusted script.
 * Does NOT mean that all methods of the type are safe. Only those methods that are marked with {@link Safe} are safe.
 *
 * When determing the safety of a class, current implementation in midPoint looks not only at the class itself, but
 * also at its supertypes and interfaces. If any of them is marked as {@link Safe}, the class is considered safe as well.
 * It inspects their annotations as well. See the warning below.
 *
 * == Warning about meta-annotations
 *
 * BEWARE: The algorithm used to determine the safety of a method or a class looks also at _annotations_ of these elements.
 * If any of these annotations is marked as {@link Safe}, the method or class is considered safe as well. This may lead to
 * unexpected results.
 *
 * Hence, NEVER use {@link Safe} as a meta-annotation for other annotations. It is not intended for that purpose.
 *
 * == Limitations
 *
 * This annotation is currently tied to its use for Velocity templates in midPoint.
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
public @interface Safe {
}
