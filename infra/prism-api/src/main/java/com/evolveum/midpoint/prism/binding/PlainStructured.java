package com.evolveum.midpoint.prism.binding;

import java.io.Serializable;

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
