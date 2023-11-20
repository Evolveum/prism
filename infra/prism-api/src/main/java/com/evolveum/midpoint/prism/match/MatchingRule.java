/*
 * Copyright (c) 2010-2014 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.match;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.normalization.Normalizer;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Interface for generic matching rules. The responsibility of a matching rule is to decide if
 * two objects of the same type match. This may seem a simple thing to do but the details may get
 * quite complex. E.g. comparing string in case sensitive or insensitive manner, comparing PolyStrings, etc.
 *
 * @author Radovan Semancik
 *
 */
public interface MatchingRule<T> {

    /**
     * QName that identifies the rule. This QName may be used to refer to this specific matching rule,
     * it is an matching rule identifier.
     */
    QName getName();

    /**
     * Returns true if the rule can be applied to the specified XSD type.
     */
    boolean supports(QName xsdType);

    /**
     * Matches two objects.
     */
    default boolean match(T a, T b) throws SchemaException {
        //noinspection unchecked
        return ((Normalizer<T>) getNormalizer()).match(a, b);
    }

    /**
     * Matches value against given regex.
     */
    default boolean matchRegex(T a, String regex) throws SchemaException {
        //noinspection unchecked
        return ((Normalizer<T>) getNormalizer()).matchRegex(a, regex);
    }

    /**
     * Returns a normalized version of the value.
     * For normalized version the following holds:
     * if A matches B then normalize(A) == normalize(B)
     *
     * FIXME Currently, the implementation of PolyStringNormMatchingRule does not fulfill this contract.
     *  The main problem is that this method returns a value of the same type as it was called with.
     *  It may not be appropriate for holding the normalized value. In the case of PolyString norm, we should
     *  rather return String instead of PolyString. A similar case could be with comparing e.g. IP addresses
     *  where an array or a list of numbers would be appropriate as the normalized representation.
     *  (Note: In midPoint provisioning module, we rely on the method returning an object with the same type.
     *  But currently, all values used there should be of String type. When introducing other types of attributes,
     *  this question will need to be resolved.)
     */
    default T normalize(T original) throws SchemaException {
        //noinspection unchecked
        return ((Normalizer<T>) getNormalizer()).normalize(original);
    }

    /**
     * Returns the normalizer corresponding to this rule.
     */
    @NotNull Normalizer<?> getNormalizer();
}
