/*
 * Copyright (C) 2010-2025 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismObjectValue;
import com.evolveum.prism.xml.ns._public.types_3.ObjectType;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

/**
 * A (fake) {@link PrismObject} implementation for object values that are embedded in a {@link PrismContainer} somewhere
 * in the enclosing (real) {@link PrismObject}.
 *
 * @see ItemImpl#isParentForValues()
 */
public class EmbeddedPrismObjectImpl<O extends ObjectType> extends PrismObjectImpl<O> {

    public EmbeddedPrismObjectImpl(QName name, @NotNull Class<O> compileTimeClass, @NotNull PrismObjectValue<O> value) {
        super(name, compileTimeClass, value);
    }

    @Override
    boolean isParentForValues() {
        return false;
    }
}
