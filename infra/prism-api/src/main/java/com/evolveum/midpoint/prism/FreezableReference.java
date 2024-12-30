/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.annotation.Experimental;

import java.io.Serializable;
import java.util.Objects;

/**
 * A reference that can be made immutable.
 *
 * The expected use of this class is to be a `final` field in a class.
 *
 * @see FreezableList
 *
 * @param <T> type of referenced item
 */
@Experimental
public class FreezableReference<T extends Serializable>
        extends AbstractFreezable
        implements Serializable {

    private T value;

    public T getValue() {
        return value;
    }

    public static <T extends Serializable> FreezableReference<T> of(T value) {
        var ref = new FreezableReference<T>();
        ref.setValue(value);
        return ref;
    }

    public void setValue(T value) {
        checkMutable();
        this.value = value;
    }

    public void setAndFreeze(T value) {
        setValue(value);
        freeze();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FreezableReference<?> that = (FreezableReference<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
