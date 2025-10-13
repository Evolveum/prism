/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.polystring;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.polystring.PolyStringNormalizer;

import com.evolveum.midpoint.util.exception.SchemaException;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A normalizer that converts all string to lowercase (and compares strings in a case-insensitive way). */
public class LowercaseStringNormalizer extends BaseStringNormalizer implements PolyStringNormalizer {

    private static final LowercaseStringNormalizer INSTANCE = new LowercaseStringNormalizer();

    @Override
    public String normalize(String orig) {
        return StringUtils.lowerCase(orig);
    }

    @Override
    public boolean match(@Nullable String a, @Nullable String b) throws SchemaException {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return StringUtils.equalsIgnoreCase(a, b);
    }

    @Override
    public boolean matchRegex(String a, String regex) {
        if (a == null) {
            return false;
        }

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(a);
        return matcher.matches();
    }

    @Override
    public @NotNull QName getName() {
        return PrismConstants.LOWERCASE_STRING_NORMALIZER;
    }

    public static @NotNull LowercaseStringNormalizer instance() {
        return INSTANCE;
    }
}
