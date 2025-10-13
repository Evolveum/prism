/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api;

import java.util.Optional;

import com.evolveum.axiom.api.schema.AxiomItemDefinition;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

public abstract class AbstractAxiomItem<V> implements AxiomItem<V> {


    private final AxiomItemDefinition definition;

    public AbstractAxiomItem(AxiomItemDefinition definition) {
        this.definition = definition;
    }

    @Override
    public Optional<AxiomItemDefinition> definition() {
        return Optional.of(definition);
    }

    @Override
    public AxiomName name() {
        return definition.name();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(AxiomItem.class)
                .add("name", name())
                .add("values", values())
                .toString();
    }
}
