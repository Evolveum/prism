/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.polystring;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Matchable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.normalization.Normalizer;
import com.evolveum.midpoint.util.exception.SchemaException;

/** The default ("no-op") normalizer that preserves the original value unchanged. */
public class DefaultNormalizer implements Normalizer<Object> {

    private static final DefaultNormalizer INSTANCE = new DefaultNormalizer();

    @Override
    public Object normalize(Object orig) throws SchemaException {
        return orig;
    }

    @Override
    public boolean match(@Nullable Object a, @Nullable Object b) throws SchemaException {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a instanceof Matchable && b instanceof Matchable) {
            //noinspection unchecked,rawtypes
            return ((Matchable) a).match((Matchable) b);
        }
        if (a instanceof byte[] && b instanceof byte[]) {
            return Arrays.equals((byte[]) a, (byte[]) b);
        }
        // Just use plain java equals() method
        return a.equals(b);
    }

    @Override
    public boolean matchRegex(Object a, String regex) {
        if (a instanceof Matchable<?> matchable) {
            return matchable.matches(regex);
        } else {
            return Pattern.matches(regex, String.valueOf(a));
        }
    }

    @Override
    public @NotNull QName getName() {
        return PrismConstants.DEFAULT_NORMALIZER;
    }

    @Override
    public boolean isIdentity() {
        return true;
    }

    public static @NotNull DefaultNormalizer instance() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " (no-op)";
    }
}
