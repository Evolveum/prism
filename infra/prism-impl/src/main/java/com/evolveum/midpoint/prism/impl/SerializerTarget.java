/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.SerializationContext;
import com.evolveum.midpoint.prism.impl.xnode.RootXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SerializerTarget<T> {

    @NotNull public final PrismContextImpl prismContext;

    protected SerializerTarget(@NotNull PrismContextImpl prismContext) {
        this.prismContext = prismContext;
    }

    @NotNull abstract public T write(@NotNull RootXNodeImpl xroot, SerializationContext context) throws SchemaException;

    @NotNull public T write(@NotNull XNodeImpl xNode, SerializationContext context) throws SchemaException {
        throw new UnsupportedOperationException("Non-root node serialization is not supported by " + this.getClass().getName());
    }

    @NotNull abstract public T write(@NotNull List<RootXNodeImpl> roots, @Nullable SerializationContext context) throws SchemaException;
}
