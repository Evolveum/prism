/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api;

import java.util.Map;
import com.evolveum.axiom.api.schema.AxiomTypeDefinition;

public class SimpleValue<T> extends AbstractAxiomValue<T> implements AxiomSimpleValue<T> {

    private final T value;

    SimpleValue(AxiomTypeDefinition type, T value, Map<AxiomName, AxiomItem<?>> infraItems) {
        super(type, infraItems);
        this.value = value;
    }

    public static final <V> AxiomSimpleValue<V> create(AxiomTypeDefinition def, V value, Map<AxiomName, AxiomItem<?>> infraItems) {
        return new SimpleValue<V>(def, value, infraItems);
    }

    @Override
    public T value() {
        return value;
    }

}
