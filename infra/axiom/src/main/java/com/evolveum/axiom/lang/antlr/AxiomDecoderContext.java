/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.lang.antlr;

import java.util.Optional;

import com.evolveum.axiom.api.AxiomName;
import com.evolveum.axiom.api.schema.AxiomTypeDefinition;
import com.evolveum.axiom.spi.codec.ValueDecoder;

public interface AxiomDecoderContext<I, V> {

    ValueDecoder<I, AxiomName> itemName();

    Optional<? extends ValueDecoder<V, ?>> get(AxiomTypeDefinition typeDefinition);

}
