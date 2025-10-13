/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

/**
 * @author semancik
 *
 */
public interface PrismContainerable<T extends Containerable> extends Itemable, ParentVisitable {

    @Override
    PrismContainerDefinition<T> getDefinition();

    Class<T> getCompileTimeClass();

    default ComplexTypeDefinition getComplexTypeDefinition() {
        PrismContainerDefinition def = getDefinition();
        return def != null ? def.getComplexTypeDefinition() : null;
    }
}
