/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.evolveum.axiom.api.schema.AxiomItemDefinition;
import com.evolveum.axiom.api.schema.AxiomTypeDefinition;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

public class StructuredValueImpl extends AbstractAxiomValue<Collection<AxiomItem<?>>> implements AxiomStructuredValue {

    private final Map<AxiomName, AxiomItem<?>> items;



    protected <X> X require(Optional<X> value) {
        return value.get();
    }

    public StructuredValueImpl(AxiomTypeDefinition type, Map<AxiomName, AxiomItem<?>> items, Map<AxiomName,AxiomItem<?>> infraItems) {
        super(type, infraItems);
        this.items = ImmutableMap.copyOf(items);
    }

    @Override
    public Optional<AxiomItem<?>> item(AxiomItemDefinition def) {
        return AxiomStructuredValue.super.item(def);
    }

    @Override
    public Optional<? extends AxiomItem<?>> item(AxiomName name) {
        return Optional.ofNullable(items.get(name));
    }

    protected AxiomItem<?> requireItem(AxiomName name) {
        return item(name).orElseThrow(() -> new IllegalStateException(Strings.lenientFormat("Required item %s not present.", name)));
    }

    public Collection<AxiomItem<?>> items() {
        return items.values();
    }

    @Override
    public Map<AxiomName, AxiomItem<?>> itemMap() {
        return items;
    }

    @SuppressWarnings("unchecked")
    protected <T> Optional<AxiomItem<T>> as(Class<T> type, Optional<? extends AxiomItem<?>> item) {
        return (Optional<AxiomItem<T>>) item;
    }
}
