/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.schema;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

/** Any schema that can be serialized into XSD. */
public interface SerializableSchema {

    /** All top-level definitions (types, items) are in this namespace. */
    @NotNull String getNamespace();

    @NotNull Collection<? extends SerializableDefinition> getDefinitionsToSerialize();
}
