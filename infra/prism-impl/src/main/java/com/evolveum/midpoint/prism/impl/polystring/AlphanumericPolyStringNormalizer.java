/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.polystring;

import com.evolveum.midpoint.prism.PrismConstants;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.regex.Pattern;

/**
 * @author semancik
 *
 */
public class AlphanumericPolyStringNormalizer extends AbstractConfigurablePolyStringNormalizer {

    private static final AlphanumericPolyStringNormalizer INSTANCE = new AlphanumericPolyStringNormalizer();

    private static final String MALFORMED_REGEX = "[^\\w\\s\\d]";
    private static final Pattern MALFORMED_PATTERN = Pattern.compile(MALFORMED_REGEX);

    @Override
    protected String normalizeCore(String s) {
        s = removeAll(s, MALFORMED_PATTERN);
        return s;
    }

    @Override
    public @NotNull QName getName() {
        return PrismConstants.ALPHANUMERIC_POLY_STRING_NORMALIZER;
    }

    public static @NotNull AlphanumericPolyStringNormalizer instance() {
        return INSTANCE;
    }
}
