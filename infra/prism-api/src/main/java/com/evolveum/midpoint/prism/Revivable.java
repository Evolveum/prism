/*
 * Copyright (c) 2014 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
