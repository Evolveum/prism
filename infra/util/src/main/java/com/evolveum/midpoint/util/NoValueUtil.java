/*
 * Copyright (C) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util;

/**
 * Simulates {@link Long} behavior for natural numbers by plain `long` values by using a special
 * {@link #NONE_LONG} value. (Use for {@link Integer} values is planned, hence the name.)
 *
 * It exists to improve the performance.
 *
 * @see CanBeNone
 */
public class NoValueUtil {

    public static final long NONE_LONG = -1L;

    public static Long toNullable(@CanBeNone long value) {
        return value != NONE_LONG ? value : null;
    }

    public static @CanBeNone long fromNullable(Long value) {
        return value != null ? value : NONE_LONG;
    }

    public static long zeroIfNone(@CanBeNone long value) {
        return value != NONE_LONG ? value : 0L;
    }
}
