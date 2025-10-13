/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.polystring;

import com.evolveum.midpoint.prism.polystring.NormalizerRegistry;

/**
 * Creates {@link NormalizerRegistry} populated with standard normalizers.
 */
public class NormalizerRegistryFactory {

    public static NormalizerRegistry createRegistry() {
        NormalizerRegistryImpl registry = new NormalizerRegistryImpl();
        registry.registerNormalizer(NoOpNormalizer.instance());
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
