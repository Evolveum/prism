/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.prism.codegen.binding;

import java.util.HashSet;
import java.util.Set;

public abstract class Binding {

    protected Contract defaultContract;
    private Set<Contract> contracts = new HashSet<>();

    public <T extends Contract> void defaultContract(T contract) {
        addContract(contract);
        defaultContract = contract;
    }

    public Contract getDefaultContract() {
        return defaultContract;
    }

    public <T extends Contract> void  addContract(T contract) {
        contracts.add(contract);
    }

    public Iterable<Contract> getContracts() {
        return contracts;
    }

    public String defaultBindingClass() {
        return defaultContract.fullyQualifiedName();
    }

    public abstract String getNamespaceURI();

}
