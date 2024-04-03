/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.normalization;

import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.util.annotation.Experimental;

import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

/** Normalizes and matches (arbitrary) values, typically strings. */
@Experimental
public interface Normalizer<T> {

    /** Returns a normalized version of the value. */
    @Contract("null -> null; !null -> !null")
    T normalize(T orig) throws SchemaException;

    /** Matches two values. */
    boolean match(@Nullable T a, @Nullable T b) throws SchemaException;

    /** Matches a value against regular expression (if supported). */
    boolean matchRegex(T a, String regex) throws SchemaException;

    /** Returns the qualified name identifying this normalizer. */
    @NotNull QName getName();

    /** `true` if the normalizer is known to be the default one, i.e., one that does no transformations. */
    boolean isIdentity();

    /**
     * Temporary solution for untyped relation between normalizers vs. matching rules. (See the wildcard return
     * type of {@link MatchingRule#getNormalizer()}. We often need to normalize strings into {@link PolyString}s.
     */
    default String normalizeString(String orig) throws SchemaException {
        if (orig == null || isIdentity()) {
            return orig;
        } else if (this instanceof StringNormalizer stringNormalizer) {
            return stringNormalizer.normalize(orig);
        } else {
            throw new UnsupportedOperationException("Cannot normalize " + orig.getClass() + " with " + this);
        }
    }
}
