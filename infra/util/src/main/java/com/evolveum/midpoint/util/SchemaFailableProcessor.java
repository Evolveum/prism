/*
 * Copyright (c) 2016 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * @author semancik
 *
 */
@FunctionalInterface
public interface SchemaFailableProcessor<T> extends CheckedProcessor<T, SchemaException> {

    void process(T object) throws SchemaException;

}
