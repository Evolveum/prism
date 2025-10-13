/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.crypto;

import javax.crypto.SecretKey;

/**
 *  TODO add other relevant methods here
 */
public interface KeyStoreBasedProtector extends Protector {

    String getKeyStorePath();

    // TODO consider what to do with this method
    String getSecretKeyDigest(SecretKey key) throws EncryptionException;
}
