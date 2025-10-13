/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api.schema;

import java.util.Collection;
import java.util.Optional;

import com.evolveum.axiom.api.AxiomInfraValue;
import com.evolveum.axiom.api.AxiomName;
import com.evolveum.axiom.api.AxiomValue;

public interface AxiomSchemaContext {

    Collection<AxiomItemDefinition> roots();

    Optional<AxiomItemDefinition> getRoot(AxiomName type);

    Optional<AxiomTypeDefinition> getType(AxiomName type);

    Collection<AxiomTypeDefinition> types();

    default AxiomTypeDefinition valueInfraType() {
        return getType(AxiomValue.AXIOM_VALUE).get();
    }

}
