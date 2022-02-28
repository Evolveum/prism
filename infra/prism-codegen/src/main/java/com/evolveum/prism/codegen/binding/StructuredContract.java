/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.google.common.base.CaseFormat;

public class StructuredContract extends Contract {

    static final String GET_PREFIX = "get";
    static final String SET_PREFIX = "set";
    static final String IS_PREFIX = "is";

    private final ComplexTypeDefinition typeDefinition;

    private @NotNull Collection<ItemBinding> attributes = new ArrayList();
    private @NotNull Collection<ItemBinding> localDefinitions = new ArrayList<>();

    public StructuredContract(ComplexTypeDefinition typeDefinition, String packageName) {
        super(packageName);
        this.typeDefinition = typeDefinition;
        for (ItemDefinition<?> def : typeDefinition.getDefinitions()) {
            String name = javaFromItemName(def.getItemName());
            ItemBinding mapping = new ItemBinding(name, def, false);
            if (!def.isInherited()) {
                localDefinitions.add(mapping);
            }
        }

        for (ItemDefinition<?> def : typeDefinition.getXmlAttributeDefinitions()) {
            String name = javaFromItemName(def.getItemName());
            ItemBinding mapping = new ItemBinding(name, def, true);
            if (!def.isInherited()) {
                attributes.add(mapping);
            }
        }

    }

    public static String javaFromItemName(@NotNull QName compositeObjectName) {
        String maybe = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, compositeObjectName.getLocalPart());
        if ("Class".equals(maybe)) {
            return "Clazz";
        }
        return maybe;
    }

    @Override
    public String fullyQualifiedName() {
        return packageName + "." + typeDefinition.getTypeName().getLocalPart();
    }

    public ComplexTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }

    public QName getSuperType() {
        return typeDefinition.getSuperType();
    }

    public Collection<ItemBinding> getLocalDefinitions() {
        return localDefinitions;
    }

    public Collection<ItemBinding> getAttributeDefinitions() {
        return attributes;
    }

    public Optional<String> getDocumentation() {
        return Optional.ofNullable(typeDefinition.getDocumentation());
    }
}
