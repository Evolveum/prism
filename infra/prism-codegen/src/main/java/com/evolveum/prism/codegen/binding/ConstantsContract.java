/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class ConstantsContract extends Contract {

    private static final String PACKAGE = "com.evolveum.midpoint.schema";
    private static final String CONSTANTS_CLASS = "SchemaConstantsGenerated";

    public ConstantsContract() {
        super(PACKAGE);
    }

    private final Map<String, NamespaceConstantMapping> mappings = new LinkedHashMap<>();
    private final Map<String, ObjectFactoryContract> contracts = new HashMap<>();

    public void put(String namespace, NamespaceConstantMapping constant) {
        mappings.put(namespace, constant);
    }

    public Map<String, NamespaceConstantMapping> getMappings() {
        return mappings;
    }

    @Override
    public String fullyQualifiedName() {
        return packageName +  "." + CONSTANTS_CLASS;
    }

    public void put(@NotNull String namespace, ObjectFactoryContract contract) {
        contracts.put(namespace, contract);
    }

    public ObjectFactoryContract getContract(NamespaceConstantMapping mapping) {
        return contracts.get(mapping.getNamespace());
    }

}
