package com.evolveum.midpoint.prism.impl.binding;

public interface StructuredEqualsStrategy {

    StructuredEqualsStrategy DEFAULT = null;

    boolean equals(AbstractPlainStructured left, Object other);

}
