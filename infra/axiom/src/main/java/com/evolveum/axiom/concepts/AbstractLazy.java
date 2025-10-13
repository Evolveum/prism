/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.concepts;

public abstract class AbstractLazy<T> {

    private volatile Object value;

    AbstractLazy(Object supplier) {
        value = supplier;
    }

    T unwrap() {
        Object val = this.value;
        if (val instanceof Lazy.Supplier<?>) {
            //noinspection unchecked
            this.value = ((Lazy.Supplier<T>) val).get();
            return unwrap();
        }
        //noinspection unchecked
        return (T) val;
    }

    public void set(Object value) {
        this.value = value;
    }

    public boolean isUnwrapped() {
        return !(value instanceof Lazy.Supplier<?>);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
