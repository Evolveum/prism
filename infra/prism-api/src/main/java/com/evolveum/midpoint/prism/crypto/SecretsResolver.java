/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.crypto;

import java.nio.ByteBuffer;
import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for component that can use initialized secret providers to resolve secrets (e.g. instead of decrypting them).
 */
public interface SecretsResolver {

    /**
     * @param provider Initialized provider to be added to the list of usable providers.
     */
    void addSecretsProvider(@NotNull SecretsProvider<?> provider);

    /**
     * @param provider Initialized provider to be removed from the list of usable providers.
     */
    void removeSecretsProvider(@NotNull SecretsProvider<?> provider);

    /**
     * @return List of usable secret providers.
     */
    @NotNull List<SecretsProvider<?>> getSecretsProviders();

    @NotNull String resolveSecretString(@NotNull String provider, @NotNull String key) throws EncryptionException;

    @NotNull ByteBuffer resolveSecretBinary(@NotNull String provider, @NotNull String key) throws EncryptionException;
}
