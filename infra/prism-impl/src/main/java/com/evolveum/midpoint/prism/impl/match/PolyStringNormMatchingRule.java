/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.match;

import java.util.regex.Pattern;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.impl.polystring.PolyStringNormNormalizer;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.impl.polystring.NoOpNormalizer;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.normalization.Normalizer;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

/**
 * @author semancik
 */
public class PolyStringNormMatchingRule implements MatchingRule<PolyString> {

    @Override
    public QName getName() {
        return PrismConstants.POLY_STRING_NORM_MATCHING_RULE_NAME;
    }

    @Override
    public boolean supports(QName xsdType) {
        return PolyStringType.COMPLEX_TYPE.equals(xsdType);
    }

    @Override
    public @NotNull Normalizer<PolyString> getNormalizer() {
        return PolyStringNormNormalizer.instance();
    }

    @Override
    public String toString() {
        return "PolyStringNormMatchingRule{}";
    }
}
