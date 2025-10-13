/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api.schema;

import com.evolveum.axiom.api.AxiomName;

public interface AxiomNamedDefinition {

    AxiomName name();
    String documentation();
}
