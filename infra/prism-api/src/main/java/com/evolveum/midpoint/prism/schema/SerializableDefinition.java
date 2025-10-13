/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

import com.evolveum.midpoint.prism.DisplayHint;

import java.util.Collection;
import java.util.List;

/** Any definition (type, item) that can be serialized. */
public interface SerializableDefinition {

    String getDisplayName();
    Integer getDisplayOrder();
    String getHelp();
    boolean isEmphasized();
    DisplayHint getDisplayHint();
    String getDocumentation();

    default Collection<? extends DefinitionFeature<?, ?, ?, ?>> getExtraFeaturesToSerialize() {
        return List.of();
    }
}
