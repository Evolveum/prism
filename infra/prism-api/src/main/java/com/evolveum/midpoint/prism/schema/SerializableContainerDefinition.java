/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.schema;

/** Any container definition that can be serialized. */
public interface SerializableContainerDefinition extends SerializableItemDefinition {

    SerializableComplexTypeDefinition getComplexTypeDefinitionToSerialize();
}
