/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.path.ItemName;
import com.google.common.base.CaseFormat;

public class StructuredContract extends Contract {

    static final String GET_PREFIX = "get";
    private static final String SET_PREFIX = "set";

    private final ComplexTypeDefinition typeDefinition;

    private @NotNull Set<ItemBinding> localDefinitions = new HashSet<>();

    public StructuredContract(ComplexTypeDefinition typeDefinition) {
        this.typeDefinition = typeDefinition;

        for (ItemDefinition<?> def : typeDefinition.getDefinitions()) {
            String name = javaFromItemName(def.getItemName());
            ItemBinding mapping = new ItemBinding(name, def);
            if (!def.isInherited()) {
                localDefinitions.add(mapping);
            }
        }
    }

    private String javaFromItemName(@NotNull ItemName itemName) {
        // FIXME: Support for possible namespace conflicts
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, itemName.getLocalPart());
    }

    @Override
    public String fullyQualifiedName() {
        return typeDefinition.getTypeName().getLocalPart();
    }

    public ComplexTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }

    public QName getSuperType() {
        return typeDefinition.getSuperType();
    }

    public Set<ItemBinding> getLocalDefinitions() {
        return localDefinitions;
    }

}
