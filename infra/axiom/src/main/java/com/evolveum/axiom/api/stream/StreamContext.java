/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api.stream;

import com.evolveum.axiom.api.schema.AxiomItemDefinition;
import com.evolveum.axiom.api.schema.AxiomTypeDefinition;

public interface StreamContext {

    AxiomTypeDefinition currentInfra();

    AxiomTypeDefinition valueType();

    AxiomItemDefinition currentItem();
}
