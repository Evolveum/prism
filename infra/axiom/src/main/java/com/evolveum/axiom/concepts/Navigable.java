/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.concepts;

import java.util.Optional;

public interface Navigable<K, N extends Navigable<K,N>> {

    Optional<? extends N> resolve(K key);

}
