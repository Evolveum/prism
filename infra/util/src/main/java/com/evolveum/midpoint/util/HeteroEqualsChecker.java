/*
 * Copyright (C) 2016-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.util;

import java.util.function.BiPredicate;

/**
 * Represents equality check, possibly between two different types.
 * See {@link EqualsChecker} for the same types for both tested objects.
 */
@FunctionalInterface
public interface HeteroEqualsChecker<A, B> extends BiPredicate<A, B> {
}
