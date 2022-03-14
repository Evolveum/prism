/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.binding;

import java.io.Serializable;

/**
 * PlainStructured object is "bean-like" value object, which contains
 * set of getters / setters generated from schema.
 *
 */
public interface PlainStructured extends Serializable, Cloneable {

    boolean equals(Object other, StructuredEqualsStrategy strategy);

    int hashCode(StructuredHashCodeStrategy strategy);

    PlainStructured clone();

    interface WithoutStrategy extends PlainStructured {

        @Override
        default boolean equals(Object other, StructuredEqualsStrategy strategy) {
            return equals(other);
        }

        @Override
        default int hashCode(StructuredHashCodeStrategy strategy) {
            return hashCode();
        }
    }
}
