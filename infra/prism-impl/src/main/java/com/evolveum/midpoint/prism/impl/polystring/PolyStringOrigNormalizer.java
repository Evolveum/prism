/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.polystring;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.normalization.Normalizer;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Special normalizer that keeps only the `orig` value of {@link PolyString} instances.
 *
 * It has no particular value besides making sure that {@link MatchingRule#normalize(Object)} contract holds.
 */
public class PolyStringOrigNormalizer implements Normalizer<PolyString> {

    private static final PolyStringOrigNormalizer INSTANCE = new PolyStringOrigNormalizer();

    @Override
    public PolyString normalize(PolyString orig) throws SchemaException {
        // Note that this will fail of the original value has no `orig` value.
        return new PolyString(orig.getOrig());
    }

    @Override
    public boolean match(@Nullable PolyString a, @Nullable PolyString b) throws SchemaException {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return Objects.equals(a.getOrig(), b.getOrig());
    }

    @Override
    public boolean matchRegex(PolyString a, String regex) {
        return a != null
                && a.getOrig() != null
                && Pattern.matches(regex, a.getNorm());
    }

    @Override
    public @NotNull QName getName() {
        return PrismConstants.POLY_STRING_ORIG_NORMALIZER;
    }

    @Override
    public boolean isIdentity() {
        return false;
    }

    public static <T> @NotNull PolyStringOrigNormalizer instance() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
