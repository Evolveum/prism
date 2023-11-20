/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.polystring;

import com.evolveum.midpoint.prism.normalization.Normalizer;
import com.evolveum.midpoint.prism.normalization.StringNormalizer;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Normalizer for strings, usually connected with {@link PolyString}.
 * Classes implementing this interface are able to take an original string (in the PolyString sense)
 * and return a normalized version of the string.
 *
 * Unlike the general case, this interface does not throw {@link SchemaException}.
 *
 * TODO decide on the exact name (but beware of renaming implementations, their names can be referenced from the config)
 *
 * @see PolyString
 * @see Normalizer
 *
 * @author Radovan Semancik
 */
public interface PolyStringNormalizer extends StringNormalizer {

    /** Intentionally not throwing {@link SchemaException}, unlike the method that is being overridden. */
    @Override
    String normalize(String orig);
}
