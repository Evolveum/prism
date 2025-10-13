/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ItemPathSerializer;
import com.evolveum.midpoint.prism.impl.marshaller.ItemPathHolder;
import com.evolveum.midpoint.prism.path.ItemPath;

public class ItemPathSerializerImpl implements ItemPathSerializer {

    @Override
    public String serializeStandalone(@NotNull ItemPath itemPath) {
        return ItemPathHolder.serializeWithForcedDeclarations(itemPath);
    }
}
