/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConstantBinding extends Binding {

    private final ConstantsContract constants = new ConstantsContract();

    public ConstantBinding() {
        defaultContract(constants);
    }

    @Override
    public String getNamespaceURI() {
        return null;
    }

    void put(String namespace, NamespaceConstantMapping constant) {
        constants.put(namespace, constant);
    }

    public @Nullable NamespaceConstantMapping get(@NotNull String namespace) {
        return constants.getMappings().get(namespace);
    }

    public boolean isEmpty() {
        return constants.getMappings().isEmpty();
    }

    public void put(@NotNull String namespace, ObjectFactoryContract contract) {
        constants.put(namespace, contract);
    }



}
