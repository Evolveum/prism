/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import java.util.Collection;

/**
 * List that freezes its members as well.
 */
public class DeeplyFreezableList<T> extends FreezableList<T> {

    public DeeplyFreezableList() {
    }

    public DeeplyFreezableList(Collection<T> initialContent) {
        super(initialContent);
    }

    @Override
    protected void performFreeze() {
        super.performFreeze();
        for (T item : this) {
            if (item instanceof Freezable) {
                ((Freezable) item).freeze();
            }
        }
    }
}
