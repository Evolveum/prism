package com.evolveum.axiom.api;

import java.util.Collection;
import java.util.Optional;

abstract class DelegatedItemDefinition implements AxiomItemDefinition {

    protected abstract AxiomItemDefinition delegate();

    @Override
    public boolean operational() {
        return false;
    }

    @Override
    public Optional<AxiomTypeDefinition> type() {
        return delegate().type();
    }

    @Override
    public Collection<AxiomItem<?>> items() {
        return delegate().items();
    }

    @Override
    public AxiomIdentifier name() {
        return delegate().name();
    }

    @Override
    public String documentation() {
        return delegate().documentation();
    }

    @Override
    public Optional<AxiomItem<?>> item(AxiomItemDefinition def) {
        return delegate().item(def);
    }

    @Override
    public <T> Optional<AxiomItem<T>> item(AxiomIdentifier name) {
        return delegate().item(name);
    }

    @Override
    public AxiomItemDefinition get() {
        return this;
    }

    @Override
    public AxiomTypeDefinition typeDefinition() {
        return delegate().typeDefinition();
    }

    @Override
    public boolean required() {
        return delegate().required();
    }

    @Override
    public int minOccurs() {
        return delegate().minOccurs();
    }

    @Override
    public int maxOccurs() {
        return delegate().maxOccurs();
    }

    @Override
    public String toString() {
        return AxiomItemDefinition.toString(this);
    }

    @Override
    public AxiomTypeDefinition definingType() {
        return delegate().definingType();
    }
}