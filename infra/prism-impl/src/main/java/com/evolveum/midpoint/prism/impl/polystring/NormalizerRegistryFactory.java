/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.polystring;

import com.evolveum.midpoint.prism.polystring.NormalizerRegistry;

/**
 * Creates {@link NormalizerRegistry} populated with standard normalizers.
 */
public class NormalizerRegistryFactory {

    public static NormalizerRegistry createRegistry() {
        NormalizerRegistryImpl registry = new NormalizerRegistryImpl();
        registry.registerNormalizer(DefaultNormalizer.instance());
        registry.registerNormalizer(LowercaseStringNormalizer.instance());
        registry.registerNormalizer(AlphanumericPolyStringNormalizer.instance());
        registry.registerNormalizer(Ascii7PolyStringNormalizer.instance());
        registry.registerNormalizer(PassThroughPolyStringNormalizer.instance());
        registry.registerNormalizer(DistinguishedNameNormalizer.instance());
        registry.registerNormalizer(ExchangeEmailAddressNormalizer.instance());
        registry.registerNormalizer(UuidNormalizer.instance());
        registry.registerNormalizer(XmlNormalizer.instance());
        return registry;
    }
}
