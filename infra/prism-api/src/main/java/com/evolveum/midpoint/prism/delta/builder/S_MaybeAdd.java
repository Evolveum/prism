/*
 * Copyright (c) 2010-2015 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.delta.builder;

import java.util.Collection;

import com.evolveum.midpoint.prism.PrismValue;

public interface S_MaybeAdd extends S_ItemEntry {

    S_ItemEntry add(Object... realValues);
    S_ItemEntry addRealValues(Collection<?> realValues);
    S_ItemEntry add(PrismValue... values);
    S_ItemEntry add(Collection<? extends PrismValue> values);
}
