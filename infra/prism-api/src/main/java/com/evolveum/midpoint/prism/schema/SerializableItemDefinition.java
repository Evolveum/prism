/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ItemProcessing;
import com.evolveum.midpoint.prism.PrismItemAccessDefinition;
import com.evolveum.midpoint.prism.path.ItemName;

/** Any item definition that can be serialized. */
public interface SerializableItemDefinition
        extends SerializableDefinition, PrismItemAccessDefinition {

    Boolean isIndexed();
    @NotNull ItemName getItemName();
    @NotNull QName getTypeName();
    boolean isOperational();
    int getMinOccurs();
    int getMaxOccurs();
    ItemProcessing getProcessing();
}
