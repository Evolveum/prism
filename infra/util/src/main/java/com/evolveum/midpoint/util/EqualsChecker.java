/*
 * Copyright (C) 2016-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

/**
 * Explicit "equals" interface for easy lambda usage.
 * This conforms to "hetero" checker, but actually limits its usability from two types to one.
 * But it also means it can be used anywhere hetero checker is allowed.
 */
@FunctionalInterface
public interface EqualsChecker<T> extends HeteroEqualsChecker<T, T> {
}
