/*
 * Copyright (C) 2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.lazy;

import com.evolveum.midpoint.prism.AbstractFreezable;
import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.xnode.XNode;

import org.jetbrains.annotations.Nullable;

public abstract class LazyXNodeBasedPrismValue<M extends XNode, F> extends AbstractFreezable {

    protected static record Source<M extends XNode>(ParsingContext parsingContext, M value) {

    }

    private Object value;

    public LazyXNodeBasedPrismValue(ParsingContext parsingContext, M value) {
        this.value = new Source<M>(parsingContext,value);
    }

    protected LazyXNodeBasedPrismValue(LazyXNodeBasedPrismValue<M,F> other) {
        this.value = other.value;
    }

    protected F materialized() {
        if (value instanceof Source<?> source) {
            value = materialize((Source<M>) source);
        }
        return (F) value;
    }

    protected boolean isMaterialized() {
        return ! (value instanceof LazyXNodeBasedPrismValue.Source<?>);
    }

    public @Nullable M xnode() {
        if (value instanceof Source<?> source) {
            return (M) source.value;
        }
        return null;
    }

    protected abstract F materialize(Source<M> source);

    public static boolean isNotMaterialized(PrismValue value) {
        return value instanceof LazyXNodeBasedPrismValue<?,?> xnode && !xnode.isMaterialized();
    }

    protected boolean hasSameSource(LazyXNodeBasedPrismValue other) {
        if (!this.isMaterialized() && !other.isMaterialized()) {
            // TODO: Consider fully comparing XNodes?
            return this.xnode() == other.xnode(); // Copy of save lazy node.
        }
        return false;
    }
}
