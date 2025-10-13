/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api.stream;

import com.evolveum.axiom.api.schema.AxiomItemDefinition;
import com.evolveum.axiom.api.schema.AxiomTypeDefinition;
import com.evolveum.concepts.SourceLocation;

public interface AxiomStreamTarget<N, V> {

    void startItem(N item, SourceLocation loc);
    void endItem(SourceLocation loc);

    void startValue(V value, SourceLocation loc);
    void endValue(SourceLocation loc);

    default void startInfra(N item, SourceLocation loc) {};
    default void endInfra(SourceLocation loc) {};

    interface WithContext<N,V> extends AxiomStreamTarget<N, V> {

        AxiomTypeDefinition currentInfra();

        AxiomTypeDefinition currentType();

    }
}
