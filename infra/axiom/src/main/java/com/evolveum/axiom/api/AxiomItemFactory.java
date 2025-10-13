/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api;

import java.util.Collection;

import com.evolveum.axiom.api.schema.AxiomItemDefinition;

public interface AxiomItemFactory<V> {

    AxiomItem<V> create(AxiomItemDefinition def, Collection<? extends AxiomValue<?>> axiomItem);

}
