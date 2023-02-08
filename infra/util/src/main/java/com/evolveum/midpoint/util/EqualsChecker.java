/*
 * Copyright (C) 2016-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
