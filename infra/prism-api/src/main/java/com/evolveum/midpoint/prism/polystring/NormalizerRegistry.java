/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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

    @Nullable Normalizer<?> getNormalizer(@NotNull QName name);

    default @NotNull Normalizer<?> getNormalizerRequired(@NotNull QName name) {
        return MiscUtil.argNonNull(
                getNormalizer(name),
                "Unknown normalizer: %s", name);
    }

    void registerNormalizer(@NotNull Normalizer<?> normalizer);
}
