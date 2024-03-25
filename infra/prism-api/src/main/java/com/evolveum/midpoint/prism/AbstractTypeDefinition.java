/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.annotation.Experimental;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

/** Lightweight type definition for non-prism builders. They don't support real {@link TypeDefinition}. */
@Experimental
public interface AbstractTypeDefinition {

    @NotNull QName getTypeName();
}
