/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

/** Any container definition that can be serialized. */
public interface SerializableContainerDefinition extends SerializableItemDefinition {

    SerializableComplexTypeDefinition getComplexTypeDefinitionToSerialize();
}
