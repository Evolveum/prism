/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
