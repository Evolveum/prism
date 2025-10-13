/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.schema.features;

import java.util.Collection;
import java.util.List;

import com.evolveum.midpoint.prism.schema.DefinitionFeatureParser;

/**
 * The {@link DefinitionFeatureParser} works with values of specified type, and that type should not be a parameterized one,
 * like "list of access rights". Hence, we provide wrappers for such situations - in particular, for collections, especially
 * lists.
 */
public interface AbstractValueWrapper<V> {

    class Impl<V> implements AbstractValueWrapper<V> {

        protected final V value;

        Impl(V value) {
            this.value = value;
        }

        public V getValue() {
            return value;
        }
    }

    class ForCollection<T, C extends Collection<T>> extends Impl<C> {

        ForCollection(C value) {
            super(value);
        }

        public boolean isEmpty() {
            return value != null && value.isEmpty();
        }

        public void add(T item) {
            value.add(item);
        }
    }

    class ForList<T>
            extends AbstractValueWrapper.ForCollection<T, List<T>> {

        ForList(List<T> value) {
            super(value);
        }
    }
}
