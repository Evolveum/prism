/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.polystring;

import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.normalization.Normalizer;

import com.evolveum.midpoint.util.MiscUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

/** The registry of normalizers. Currently pretty much unused, as these are accessed by {@link MatchingRule}s. */
public interface NormalizerRegistry {

    <T> @Nullable Normalizer<T> getNormalizer(@NotNull QName name);

    default <T> @NotNull Normalizer<T> getNormalizerRequired(@NotNull QName name) {
        return MiscUtil.argNonNull(
                getNormalizer(name),
                "Unknown normalizer: %s", name);
    }

    void registerNormalizer(@NotNull Normalizer<?> normalizer);
}
