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

/**
 * @author semancik
 *
 */
public class Ascii7PolyStringNormalizer extends AbstractConfigurablePolyStringNormalizer {

    private static final Ascii7PolyStringNormalizer INSTANCE = new Ascii7PolyStringNormalizer();

    @Override
    public String normalizeCore(String s) {
        s = keepOnly(s, 0x20, 0x7f);
        return s;
    }

    @Override
    public @NotNull QName getName() {
        return PrismConstants.ASCII7_POLY_STRING_NORMALIZER;
    }

    public static @NotNull Ascii7PolyStringNormalizer instance() {
        return INSTANCE;
    }
}
