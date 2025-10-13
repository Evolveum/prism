/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.lang.impl;

import java.util.Optional;

import com.evolveum.axiom.api.AxiomName;
import com.evolveum.axiom.api.AxiomValue;
import com.evolveum.axiom.api.AxiomValueIdentifier;

public interface AxiomItemContext<T> {

    AxiomValueContext<T> addValue(T value);

    AxiomValueContext<?> parent();

    T onlyValue();

    void addOperationalValue(AxiomValueReference<T> value);

    Optional<? extends AxiomValueContext<T>> value(AxiomValueIdentifier id);

    void addCompletedValue(AxiomValue<?> itemDef);

    AxiomValueContext<T> only();

    void addOperationalValue(AxiomValueContext<?> value);

}
