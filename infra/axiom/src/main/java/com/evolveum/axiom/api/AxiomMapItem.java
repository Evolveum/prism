/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api;

import java.util.Optional;

public interface AxiomMapItem<V> extends AxiomItem<V> {

    Optional<? extends AxiomValue<V>> value(AxiomValueIdentifier key);

}
