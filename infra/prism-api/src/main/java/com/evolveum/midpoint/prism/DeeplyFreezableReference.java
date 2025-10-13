/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.annotation.Experimental;

import java.io.Serializable;

/**
 * Reference that freezes the referenced object as well.
 *
 * @see DeeplyFreezableList
 */
@Experimental
public class DeeplyFreezableReference<T extends Serializable> extends FreezableReference<T> {

    @Override
    protected void performFreeze() {
        super.performFreeze();
        T value = getValue();
        if (value instanceof Freezable freezable) {
            freezable.freeze();
        }
    }
}
