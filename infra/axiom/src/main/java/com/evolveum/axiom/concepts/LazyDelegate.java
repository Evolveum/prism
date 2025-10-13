/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.concepts;

public abstract class LazyDelegate<T> extends AbstractLazy<T> {

    public LazyDelegate(Lazy.Supplier<T> supplier) {
        super(supplier);
    }

    protected T delegate() {
        return unwrap();
    }
}
