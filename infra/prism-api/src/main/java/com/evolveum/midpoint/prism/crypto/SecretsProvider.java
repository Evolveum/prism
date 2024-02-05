/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.crypto;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.NotNull;

public interface SecretsProvider {

    default void init() {
    }

    default void destroy() {
    }

    @NotNull String getIdentifier();

    String getSecretString(@NotNull String key) throws EncryptionException;

    default ByteBuffer getSecretBinary(@NotNull String key) throws EncryptionException {
        String secretString = getSecretString(key);
        if (secretString == null) {
            return null;
        }
        return ByteBuffer.wrap(secretString.getBytes());
    }
}
