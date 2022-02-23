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
