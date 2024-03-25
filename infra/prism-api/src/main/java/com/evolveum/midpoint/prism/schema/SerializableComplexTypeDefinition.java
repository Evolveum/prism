/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.schema;

import java.util.Collection;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Any complex type definition (or analogous structure) that can be serialized into XSD.
 *
 * Note that "serializable simple type definition" does not exist yet. We don't need these for now.
 * (We serialize mainly resource and connector configurations that don't use them. But this may change
 * with the addition of archetype schema editing feature.)
 */
public interface SerializableComplexTypeDefinition extends SerializableDefinition {

    @NotNull QName getTypeName();
    @Nullable QName getSuperType();
    @NotNull Collection<? extends SerializableItemDefinition> getDefinitionsToSerialize();
    boolean isXsdAnyMarker();
    boolean isObjectMarker();
    boolean isContainerMarker();
}
