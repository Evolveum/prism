/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.polystring;

import static com.evolveum.midpoint.util.MiscUtil.emptyIfNull;

import java.util.regex.Pattern;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.PrismConstants;

/**
 * A normalizer for Microsoft Exchange EmailAddresses attribute consisting of `SMTP:`/`smtp:` prefix and email address.
 * It considers the case in the prefix but ignores the case in the email address.
 */
public class ExchangeEmailAddressNormalizer extends BaseStringNormalizer {

    private static final ExchangeEmailAddressNormalizer INSTANCE = new ExchangeEmailAddressNormalizer();

    @Override
    public String normalize(String orig) {
        if (orig == null) {
            return null;
        }
        @Nullable String prefix = getPrefix(orig);
        @NotNull String suffix = emptyIfNull(getSuffix(orig)).toLowerCase();
        if (prefix == null) {
            return suffix;
        } else {
            return prefix + ":" + suffix;
        }
    }

    private static String getPrefix(String a) {
        int i = a.indexOf(':');
        if (i < 0) {
            return null;
        } else {
            return a.substring(0, i);
        }
    }

    private static String getSuffix(String a) {
        int i = a.indexOf(':');
        if (i < 0) {
            return a;
        } else {
            return a.substring(i+1);
        }
    }

    @Override
    public boolean match(@Nullable String a, @Nullable String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        a = a.trim();
        b = b.trim();
        if (a.equals(b)) {
            return true;
        }
        String aPrefix = getPrefix(a);
        String aSuffix = getSuffix(a);
        String bPrefix = getPrefix(b);
        String bSuffix = getSuffix(b);
        return StringUtils.equals(aPrefix, bPrefix)
                && StringUtils.equalsIgnoreCase(aSuffix, bSuffix);
    }

    @Override
    public boolean matchRegex(String a, String regex) {
        return a != null && Pattern.matches(regex, a); // we ignore case-insensitiveness of the email address
    }

    @Override
    public @NotNull QName getName() {
        return PrismConstants.EXCHANGE_EMAIL_ADDRESS_NORMALIZER;
    }

    public static @NotNull ExchangeEmailAddressNormalizer instance() {
        return INSTANCE;
    }
}
