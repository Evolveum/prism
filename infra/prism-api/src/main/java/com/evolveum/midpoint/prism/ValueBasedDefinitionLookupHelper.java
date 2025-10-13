/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.path.ItemPath;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Set;

public interface ValueBasedDefinitionLookupHelper {

    @NotNull QName baseTypeName();

    @NotNull Set<ItemPath> valuePaths();

    @Nullable ComplexTypeDefinition findComplexTypeDefinition(QName typeName, Map<ItemPath, PrismValue> hintValues);
}
