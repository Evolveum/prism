/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api;

import java.util.Map;
import java.util.Optional;

public interface AxiomInfraValue {

    Map<AxiomName, AxiomItem<?>> infraItems();

    default Optional<AxiomItem<?>> infraItem(AxiomName name) {
        return Optional.ofNullable(infraItems().get(name));
    }

    interface Factory<V extends AxiomInfraValue> {
        V create(Map<AxiomName,AxiomItem<?>> infraItems);
    }
}
