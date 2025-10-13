/*
 * Copyright (C) 2016-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
