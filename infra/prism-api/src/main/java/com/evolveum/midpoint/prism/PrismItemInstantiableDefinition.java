/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.path.ItemPath;

/** Creating values, deltas, and so on. */
public interface PrismItemInstantiableDefinition<
        T,
        V extends PrismValue,
        I extends Item<V, ID>,
        ID extends ItemDefinition<I>,
        D extends ItemDelta<V, ID>> {

    @NotNull I instantiate();

    @NotNull I instantiate(QName name);

    @NotNull D createEmptyDelta(ItemPath path);
}
