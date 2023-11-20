/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.match;

import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

/** Registry for matching rules. */
public interface MatchingRuleRegistry {

    /**
     * Returns the matching rule by its name; checking its applicability to given data type (if type name is provided).
     *
     * The `null` rule name means "default rule". The `null` type name means "no type checking".
     */
    <T> @NotNull MatchingRule<T> getMatchingRule(@Nullable QName ruleName, @Nullable QName typeQName) throws SchemaException;
}
