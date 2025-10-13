/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api;

import java.util.Optional;

public interface AxiomStructured {

    default Optional<? extends AxiomStructuredValue> asComplex() {
        if(this instanceof AxiomStructuredValue)  {
            return Optional.of((AxiomStructuredValue) this);
        }
        return Optional.empty();
    }
}
