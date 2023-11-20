/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.normalization;

/**
 * A {@link Normalizer} that operates on {@link String} instances.
 *
 * This is a special case, used because the majority of normalization is currently supported only for strings.
 * (Maybe a temporary workaround. We will see.)
 */
public interface StringNormalizer extends Normalizer<String> {
}
