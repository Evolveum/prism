/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.schemaContext;

import java.io.Serializable;

import com.evolveum.midpoint.prism.ItemDefinition;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the semantic information related to a given prism value.
 *
 * Currently it is aimed to provide definition of objects (or smaller items) to be targeted by filters present within given
 * prism value. For detailed motivation and how-to-use information, see
 * https://docs.evolveum.com/midpoint/devel/schema-context-annotations/.
 */
public interface SchemaContext extends Serializable {

    /**
     * The definition of an object (or a smaller item, e.g. an assignment) that is targeted by filter or filters present
     * withing prism value to which this schema context belong.
     */
    @Nullable ItemDefinition<?> getItemDefinition();

}
