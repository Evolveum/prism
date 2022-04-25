/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
        if (value instanceof Freezable) {
            ((Freezable) value).freeze();
        }
    }
}
