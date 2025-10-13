/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.concepts;

import java.util.Collection;
import java.util.Optional;

public interface PathNavigable<V,K,P extends Path<K>> {

    Optional<? extends PathNavigable<V,K,P>> resolve(K key);

    Optional<? extends PathNavigable<V, K, P>> resolve(P key);
}

