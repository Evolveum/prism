/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.crypto;

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for component that can use initialized secret providers to resolve secrets (e.g. instead of decrypting them).
 */
public interface SecretsResolver {

    /**
     * @param provider Initialized provider to be added to the list of usable providers.
     */
    void addSecretsProvider(@NotNull SecretsProvider provider);

    /**
     * @param provider Initialized provider to be removed from the list of usable providers.
     */
    void removeSecretsProvider(@NotNull SecretsProvider provider);

    /**
     * @return List of usable secret providers.
     */
    @NotNull List<SecretsProvider> getSecretsProviders();
}
