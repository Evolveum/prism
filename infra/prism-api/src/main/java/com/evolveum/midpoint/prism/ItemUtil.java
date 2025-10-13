/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.delta.PropertyDelta;

/**
 *
 */
public class ItemUtil {
    public static <T> PropertyDelta<T> diff(PrismProperty<T> a, PrismProperty<T> b) {
            if (a == null) {
                if (b == null) {
                    return null;
                }
                PropertyDelta<T> delta = b.createDelta();
                delta.addValuesToAdd(PrismValueCollectionsUtil.cloneCollection(b.getValues()));
                return delta;
            } else {
                return a.diff(b);
            }
        }

    public static <T> T getRealValue(PrismProperty<T> property) {
        return property != null ? property.getRealValue() : null;
    }
}
