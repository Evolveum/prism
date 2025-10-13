/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.polystring;

import com.evolveum.midpoint.prism.PrismConstants;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

/**
 * @author semancik
 */
public class PassThroughPolyStringNormalizer extends AbstractConfigurablePolyStringNormalizer {

    private static final PassThroughPolyStringNormalizer INSTANCE = new PassThroughPolyStringNormalizer();

    @Override
    protected String normalizeCore(String s) {
        return s;
    }

    @Override
    public @NotNull QName getName() {
        return PrismConstants.PASSTHROUGH_POLY_STRING_NORMALIZER;
    }

    public static @NotNull PassThroughPolyStringNormalizer instance() {
        return INSTANCE;
    }
}
