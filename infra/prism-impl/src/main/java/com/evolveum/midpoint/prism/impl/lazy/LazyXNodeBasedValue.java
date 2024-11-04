/*
 * Copyright (C) 2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.lazy;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;

import org.jetbrains.annotations.Nullable;

abstract class LazyXNodeBasedValue<M extends XNodeImpl, F> {

    protected static record Source<M extends XNodeImpl>(ParsingContext parsingContext, M value) {

    }

    private Object value;

    public LazyXNodeBasedValue(ParsingContext parsingContext, M value) {
        this.value = new Source<M>(parsingContext,value);
    }

    protected F materialized() {
        if (value instanceof Source<?> source) {
            value = materialize((Source<M>) source);
        }
        return (F) value;
    }

    boolean isMaterialized() {
        return ! (value instanceof LazyXNodeBasedValue.Source<?>);
    }

    protected @Nullable M xnode() {
        if (value instanceof Source<?> source) {
            return (M) source.value;
        }
        return null;
    }

    protected abstract F materialize(Source<M> source);

}
