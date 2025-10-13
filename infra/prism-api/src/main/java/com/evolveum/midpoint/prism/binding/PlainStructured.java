/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
