/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

/**
 * @author semancik
 *
 */
public interface Recomputable {

    // TODO recompute method

    boolean equalsOriginalValue(Recomputable other);

    /**
     * @throws IllegalStateException
     */
    void checkConsistence();

}
