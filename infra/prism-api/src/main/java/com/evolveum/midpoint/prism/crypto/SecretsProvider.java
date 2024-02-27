/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.crypto;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for component that can resolve secrets from external secrets managers (e.g. instead of decrypting them).
 */
public interface SecretsProvider<C> {

    String[] EMPTY_DEPENDENCIES = new String[0];

    /**
     * Post-construction initialization.
     * Called before the provider is added to the list of usable providers.
     */
    default void initialize() {
    }

    /**
     * This method can be used to clean-up resources of secret provider.
     * Called after provider was removed from the list of usable providers.
     */
    default void destroy() {
    }

    /**
     * Returns unique identifier of the provider.
     */
    @NotNull String getIdentifier();

    /**
     * Returns list of providers that this provider depends on.
     * The provider will be initialized after all dependencies are available and initialized.
     *
     * Default implementation returns an empty array.
     */
    @NotNull
    default String[] getDependencies() {
        return EMPTY_DEPENDENCIES;
    }

    /**
     * Returns configuration of the provider.
     */
    C getConfiguration();

    /**
     * Returns secret {@link String} for given key.
     * Returns null if the secret does not exist.
     *
     * @throws EncryptionException if the secret cannot be resolved (e.g. due to network problems)
     */
    String getSecretString(@NotNull String key) throws EncryptionException;

    /**
     * Returns secret {@link ByteBuffer} for given key.
     * Returns null if the secret does not exist.
     *
     * @throws EncryptionException if the secret cannot be resolved (e.g. due to network problems)
     */
    default ByteBuffer getSecretBinary(@NotNull String key) throws EncryptionException {
        String secretString = getSecretString(key);
        if (secretString == null) {
            return null;
        }
        return ByteBuffer.wrap(secretString.getBytes());
    }
}
