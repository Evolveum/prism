/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.polystring;

import com.evolveum.midpoint.prism.normalization.Normalizer;
import com.evolveum.midpoint.prism.path.NameKeyedMap;
import com.evolveum.midpoint.prism.polystring.NormalizerRegistry;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

public class NormalizerRegistryImpl implements NormalizerRegistry {

    @NotNull private final NameKeyedMap<QName, Normalizer<?>> normalizers = new NameKeyedMap<>();

    @Override
    public @NotNull Normalizer<?> getNormalizer(@NotNull QName name) {
        return normalizers.get(name);
    }

    @Override
    public void registerNormalizer(@NotNull Normalizer<?> normalizer) {
        normalizers.put(normalizer.getName(), normalizer);
    }
}
