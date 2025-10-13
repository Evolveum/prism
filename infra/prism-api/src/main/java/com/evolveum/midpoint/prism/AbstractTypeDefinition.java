/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.schema.SchemaLookup;
import com.evolveum.midpoint.util.annotation.Experimental;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

/** Lightweight type definition for non-prism builders. They don't support real {@link TypeDefinition}. */
@Experimental
public interface AbstractTypeDefinition {

    @NotNull QName getTypeName();
}
