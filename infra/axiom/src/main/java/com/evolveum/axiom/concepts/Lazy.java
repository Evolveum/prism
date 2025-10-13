/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.concepts;

public class Lazy<T> extends AbstractLazy<T> implements java.util.function.Supplier<T> {

    private static final Lazy<?> NULL = new Lazy<>(null);

    private Lazy(Object supplier) {
        super(supplier);
    }

    public static <T> Lazy<T> from(Supplier<? extends T> supplier) {
        return new Lazy<>(supplier);
    }

    public static <T> Lazy<T> instant(T value) {
        if (value == null) {
            return nullValue();
        }
        return new Lazy<>(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Lazy<T> nullValue() {
        return Lazy.class.cast(NULL);
    }

    @Override
    public T get() {
        return unwrap();
    }

    public interface Supplier<T> extends java.util.function.Supplier<T> {

    }

}
