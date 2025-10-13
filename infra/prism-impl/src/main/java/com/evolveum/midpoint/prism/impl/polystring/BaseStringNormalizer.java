/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
