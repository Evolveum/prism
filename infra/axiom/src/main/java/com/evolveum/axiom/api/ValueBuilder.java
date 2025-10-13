/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api;

import com.evolveum.axiom.concepts.Builder;

public interface ValueBuilder<P extends AxiomValue<?>> extends Builder<P> {




    interface BuilderOrValue<P extends AxiomValue<?>> extends Builder.OrProduct<P, ValueBuilder<P>> {

    }
}
