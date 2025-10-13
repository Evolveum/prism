/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.polystring;

import com.evolveum.prism.xml.ns._public.types_3.PolyStringNormalizerConfigurationType;

/**
 * @author semancik
 *
 */
public interface ConfigurableNormalizer {

    void configure(PolyStringNormalizerConfigurationType configuration);

}
