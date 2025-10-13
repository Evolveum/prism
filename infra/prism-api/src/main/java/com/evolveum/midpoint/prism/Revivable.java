/*
 * Copyright (c) 2014 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

/**
 * @author Radovan Semancik
 */
@FunctionalInterface
public interface Revivable {

    /**
     * TODO: Is revive necessary if prism context is static?
     * TODO document (if it's found to be necessary)
     */
    void revive(PrismContext prismContext);

}
