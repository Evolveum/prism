/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.lang.impl;

import java.util.Map;

import com.evolveum.axiom.api.AxiomName;
import com.evolveum.axiom.api.schema.AxiomIdentifierDefinition.Scope;
import com.evolveum.axiom.api.AxiomValueIdentifier;


interface IdentifierSpaceHolder {

    void register(AxiomName space, Scope scope, AxiomValueIdentifier key, ValueContext<?> context);

    public ValueContext<?> lookup(AxiomName space, AxiomValueIdentifier key);

    Map<AxiomValueIdentifier, ValueContext<?>> space(AxiomName space);
}
