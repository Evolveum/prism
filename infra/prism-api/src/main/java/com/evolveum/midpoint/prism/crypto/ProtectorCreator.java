/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.crypto;

/**
 *  Creates protectors based on corresponding builder objects.
 *  Usually implemented by the PrismContext.
 */
public interface ProtectorCreator {

    /**
     * Creates initialized KeyStoreBasedProtector according to configured KeyStoreBasedProtectorBuilder object.
     */
    KeyStoreBasedProtector createInitializedProtector(KeyStoreBasedProtectorBuilder builder);

    /**
     * Creates uninitialized KeyStoreBasedProtector according to configured KeyStoreBasedProtectorBuilder object.
     */
    KeyStoreBasedProtector createProtector(KeyStoreBasedProtectorBuilder builder);

}
