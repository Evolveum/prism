/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.polystring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.PrismConstants;

/** Normalizer for UUID values. */
public class UuidNormalizer extends BaseStringNormalizer {

    private static final UuidNormalizer INSTANCE = new UuidNormalizer();

    @Override
    public String normalize(String orig) {
        if (orig == null) {
            return null;
        }
        return orig.toLowerCase().trim();
    }

    @Override
    public boolean match(@Nullable String a, @Nullable String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return StringUtils.equalsIgnoreCase(a.trim(), b.trim());
    }

    @Override
    public boolean matchRegex(String a, String regex) {
        if (a == null) {
            return false;
        }

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(a.trim());
        return matcher.matches();
    }

    @Override
    public @NotNull QName getName() {
        return PrismConstants.UUID_NORMALIZER;
    }

    public static @NotNull UuidNormalizer instance() {
        return INSTANCE;
    }
}
