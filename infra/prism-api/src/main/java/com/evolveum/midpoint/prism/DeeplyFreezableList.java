/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
            if (item instanceof Freezable freezable) {
                freezable.freeze();
            }
        }
    }
}
