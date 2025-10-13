/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * @author Radovan Semancik
 *
 */
@FunctionalInterface
public interface PrismContextFactory {

    /**
     * Returns UNINITIALIZED prism context.
     */
    PrismContext createPrismContext() throws SchemaException, IOException;

}
