/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.polystring;

import com.evolveum.midpoint.prism.normalization.StringNormalizer;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.util.exception.SchemaException;

public abstract class BaseStringNormalizer implements StringNormalizer {

    @Override
    public PolyString poly(String orig) throws SchemaException {
        return orig != null ?
                new PolyString(orig, normalize(orig)) :
                null;
    }

    @Override
    public boolean isIdentity() {
        return false;
    }

    @Override
    public String toString() {
        // Useful for simple, singleton implementation. Overridden e.g. in configurable ones.
        return getClass().getSimpleName();
    }
}
