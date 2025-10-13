/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
