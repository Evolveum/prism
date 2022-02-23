package com.evolveum.midpoint.prism.impl.binding;

public abstract class AbstractPlainStructured implements Cloneable {

    @Override
    public boolean equals(Object obj) {
        return equals(obj, StructuredEqualsStrategy.DEFAULT);
    }

    public boolean equals(Object other, StructuredEqualsStrategy strategy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public int hashCode(StructuredHashCodeStrategy strategy) {
        throw new UnsupportedOperationException();
    }
}
