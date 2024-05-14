/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
